package com.docuflow.api.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Configures the thread pool used by @Async methods.
 *
 * All file processing operations run on this pool so they never
 * block the Tomcat request threads.
 *
 * Pool sizing:
 * - corePoolSize 4   — always-alive threads ready for jobs
 * - maxPoolSize 8    — burst capacity for concurrent operations
 * - queueCapacity 50 — jobs waiting when all 8 threads are busy
 *
 * If the queue fills up (50 jobs waiting + 8 processing = 58 concurrent
 * requests), new submissions are rejected with a log warning.
 * Adjust these numbers based on your server's CPU and memory.
 */
@Configuration
public class AsyncConfig {

    @Bean(name = "processingExecutor")
    public Executor processingExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("processing-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}
