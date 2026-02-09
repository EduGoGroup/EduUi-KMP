package com.edugo.kmp.storage

import com.edugo.kmp.logger.Logger

/**
 * Logger implementation that discards all messages.
 *
 * Used in unit tests to avoid depending on platform-specific loggers
 * (e.g., android.util.Log) that are not available in JVM unit test environments.
 */
internal class NoOpLogger : Logger {
    override fun d(tag: String, message: String) {}
    override fun d(tag: String, message: String, throwable: Throwable) {}
    override fun i(tag: String, message: String) {}
    override fun i(tag: String, message: String, throwable: Throwable) {}
    override fun w(tag: String, message: String) {}
    override fun w(tag: String, message: String, throwable: Throwable) {}
    override fun e(tag: String, message: String) {}
    override fun e(tag: String, message: String, throwable: Throwable) {}
}
