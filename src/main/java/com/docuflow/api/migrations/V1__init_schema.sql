-- ============================================================
-- V1__init_schema.sql
-- DocuFlow initial schema
-- ============================================================

-- ── Enums ─────────────────────────────────────────────────────────────────────

CREATE TYPE operation_status AS ENUM (
    'QUEUED', 'PROCESSING', 'COMPLETED', 'FAILED'
);

CREATE TYPE file_status AS ENUM (
    'UPLOADING', 'READY', 'PROCESSING', 'DELETED'
);

CREATE TYPE subscription_status AS ENUM (
    'TRIALING', 'ACTIVE', 'PAST_DUE', 'CANCELLED'
);

CREATE TYPE payment_status AS ENUM (
    'PENDING', 'COMPLETED', 'FAILED', 'REFUNDED'
);

CREATE TYPE operation_type AS ENUM (
    'MERGE',
    'SPLIT',
    'COMPRESS',
    'ROTATE',
    'WATERMARK',
    'PROTECT',
    'REMOVE_PASSWORD',
    'REORDER_PAGES',
    'DELETE_PAGES',
    'EXTRACT_PAGES',
    'CONVERT_PDF_TO_DOCX',
    'CONVERT_DOCX_TO_PDF'
);

CREATE TYPE operation_file_role AS ENUM ('INPUT', 'OUTPUT');

-- ── users ─────────────────────────────────────────────────────────────────────

CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email               VARCHAR(255) NOT NULL,
    password_hash       TEXT         NOT NULL,
    full_name           VARCHAR(150),
    email_verified      BOOLEAN      NOT NULL DEFAULT FALSE,
    verification_token  VARCHAR(64),
    reset_token         VARCHAR(64),
    reset_token_expires TIMESTAMPTZ,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    deleted_at          TIMESTAMPTZ
);

-- Partial unique index — allows multiple soft-deleted rows with same email
-- but only one active account per email.
CREATE UNIQUE INDEX idx_users_email_active
    ON users (email)
    WHERE deleted_at IS NULL;

-- ── plans ─────────────────────────────────────────────────────────────────────
-- Static seed table. Rows are inserted once by this migration.
-- Prices are in GHS kobo (1 GHS = 100 kobo).

CREATE TABLE plans (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name                VARCHAR(50)  NOT NULL,
    slug                VARCHAR(50)  NOT NULL UNIQUE,
    price_monthly_kobo  INTEGER      NOT NULL DEFAULT 0,
    max_file_size_mb    INTEGER      NOT NULL DEFAULT 5,
    max_ops_per_day     INTEGER      NOT NULL DEFAULT 3,  -- -1 means unlimited
    storage_limit_mb    INTEGER      NOT NULL DEFAULT 500,
    file_expiry_days    INTEGER      NOT NULL DEFAULT 7,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

INSERT INTO plans (name, slug, price_monthly_kobo, max_file_size_mb,
                   max_ops_per_day, storage_limit_mb, file_expiry_days)
VALUES
    ('Free', 'free', 0,      5,  3,  500,   7),
    ('Pro',  'pro',  490000, 50, -1, 10240, 365);

-- Note: 490000 kobo = GHS 49.00 / month

-- ── subscriptions ─────────────────────────────────────────────────────────────

CREATE TABLE subscriptions (
    id                  UUID                PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID                NOT NULL REFERENCES users(id),
    plan_id             UUID                NOT NULL REFERENCES plans(id),
    status              subscription_status NOT NULL,
    paystack_sub_code   VARCHAR(100),
    paystack_email_token VARCHAR(100),
    current_period_start TIMESTAMPTZ        NOT NULL,
    current_period_end   TIMESTAMPTZ        NOT NULL,
    cancelled_at         TIMESTAMPTZ,
    created_at           TIMESTAMPTZ        NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ        NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_subscriptions_user_id ON subscriptions (user_id);
CREATE INDEX idx_subscriptions_status  ON subscriptions (status);

-- ── payments ──────────────────────────────────────────────────────────────────

CREATE TABLE payments (
    id                  UUID           PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id             UUID           NOT NULL REFERENCES users(id),
    subscription_id     UUID           REFERENCES subscriptions(id),
    amount_kobo         INTEGER        NOT NULL,
    currency            CHAR(3)        NOT NULL DEFAULT 'GHS',
    status              payment_status NOT NULL,
    paystack_reference  VARCHAR(255),
    paystack_tx_id      VARCHAR(255),
    gateway_metadata    JSONB,
    paid_at             TIMESTAMPTZ,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_payments_user_id          ON payments (user_id);
CREATE INDEX idx_payments_paystack_ref     ON payments (paystack_reference);

-- ── files ─────────────────────────────────────────────────────────────────────

CREATE TABLE files (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID        NOT NULL REFERENCES users(id),
    original_name   VARCHAR(500) NOT NULL,
    r2_key          VARCHAR(1000) NOT NULL UNIQUE,
    r2_bucket       VARCHAR(100) NOT NULL,
    mime_type       VARCHAR(100) NOT NULL,
    size_bytes      BIGINT      NOT NULL,
    page_count      INTEGER,
    status          file_status NOT NULL DEFAULT 'UPLOADING',
    is_output       BOOLEAN     NOT NULL DEFAULT FALSE,
    source_file_id  UUID        REFERENCES files(id),
    expires_at      TIMESTAMPTZ,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    deleted_at      TIMESTAMPTZ
);

CREATE INDEX idx_files_user_id    ON files (user_id);
CREATE INDEX idx_files_status     ON files (status);
CREATE INDEX idx_files_expires_at ON files (expires_at) WHERE expires_at IS NOT NULL;

-- ── operations ────────────────────────────────────────────────────────────────

CREATE TABLE operations (
    id              UUID             PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id         UUID             NOT NULL REFERENCES users(id),
    type            operation_type   NOT NULL,
    status          operation_status NOT NULL DEFAULT 'QUEUED',
    input_params    JSONB,
    error_message   TEXT,
    duration_ms     INTEGER,
    created_at      TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMPTZ
);


-- ── refresh_tokens ────────────────────────────────────────────────────────────

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token       VARCHAR(255) NOT NULL UNIQUE,
    user_id     UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expiry_date TIMESTAMPTZ NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);

CREATE INDEX idx_operations_user_id ON operations (user_id);
CREATE INDEX idx_operations_status  ON operations (status);

-- ── operation_files ───────────────────────────────────────────────────────────

CREATE TABLE operation_files (
    id              UUID                 PRIMARY KEY DEFAULT gen_random_uuid(),
    operation_id    UUID                 NOT NULL REFERENCES operations(id) ON DELETE CASCADE,
    file_id         UUID                 NOT NULL REFERENCES files(id),
    role            operation_file_role  NOT NULL,
    sort_order      INTEGER
);

CREATE INDEX idx_opfiles_operation_id ON operation_files (operation_id);
CREATE INDEX idx_opfiles_file_id      ON operation_files (file_id);

-- ── usage ─────────────────────────────────────────────────────────────────────
-- One row per user per day. Tracks daily operation count for quota enforcement.

CREATE TABLE usage (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID        NOT NULL REFERENCES users(id),
    date        DATE        NOT NULL,
    ops_count   INTEGER     NOT NULL DEFAULT 0,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_usage_user_date UNIQUE (user_id, date)
);

CREATE INDEX idx_usage_user_date ON usage (user_id, date);

-- ── webhook_events ────────────────────────────────────────────────────────────
-- Stores every Paystack webhook for idempotency and audit.

CREATE TABLE webhook_events (
    id               UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    event_type       VARCHAR(100) NOT NULL,
    idempotency_key  VARCHAR(255) NOT NULL UNIQUE,
    status           VARCHAR(20)  NOT NULL DEFAULT 'RECEIVED',
    payload          JSONB        NOT NULL,
    processed_at     TIMESTAMPTZ,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);




CREATE UNIQUE INDEX idx_webhook_idempotency ON webhook_events (idempotency_key);

-- ── Auto-update updated_at ────────────────────────────────────────────────────

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_subscriptions_updated_at
    BEFORE UPDATE ON subscriptions
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();

CREATE TRIGGER trg_usage_updated_at
    BEFORE UPDATE ON usage
    FOR EACH ROW EXECUTE FUNCTION set_updated_at();