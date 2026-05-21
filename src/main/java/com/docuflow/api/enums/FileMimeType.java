package com.docuflow.api.enums;

/**
 * Supported file MIME types in the system.
 *
 * Why the enum names are short codes (PDF, DOCX) rather than full MIME strings:
 * PostgreSQL enforces a 63-byte maximum on enum label values. Full MIME type
 * strings like "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
 * exceed that limit and cause a migration failure.
 *
 * The solution is to store short codes in the database (PDF, DOCX, etc.) and
 * keep the full MIME type string as a Java field. The mimeType field is used
 * when setting Content-Type headers or validating uploaded files.
 *
 * The enum name() — e.g. "DOCX" — is what JPA stores in the DB via
 * @Enumerated(EnumType.STRING). It matches the PostgreSQL enum labels exactly.
 */
public enum FileMimeType {

    PDF("application/pdf"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
    DOC("application/msword");
   

    private final String mimeType;

    FileMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * Returns the full MIME type string (e.g. "application/pdf").
     * Use this when setting Content-Type headers or validating file uploads.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * Looks up a FileMimeType by its full MIME string.
     * Used when receiving a file upload — the browser sends the full MIME type,
     * we convert it to our enum to store in the database.
     *
     * @param mimeType the full MIME type string from the browser/client
     * @return the matching FileMimeType enum constant
     * @throws IllegalArgumentException if the MIME type is not supported
     */
    public static FileMimeType fromMimeType(String mimeType) {
        for (FileMimeType type : values()) {
            if (type.mimeType.equalsIgnoreCase(mimeType)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unsupported MIME type: " + mimeType);
    }
}