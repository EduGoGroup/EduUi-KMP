package com.edugo.kmp.config

import android.content.Context

/**
 * Holds the Android application context for accessing resources.
 * Must be initialized before loading config.
 *
 * Usage:
 * ```
 * // In MainActivity.onCreate() or Application.onCreate()
 * AndroidContextHolder.init(applicationContext)
 * ```
 */
public object AndroidContextHolder {
    private var context: Context? = null

    /**
     * Initializes the holder with the application context.
     * This should be called as early as possible in the app lifecycle.
     *
     * @param appContext The application context (use applicationContext, not activity context)
     */
    public fun init(appContext: Context) {
        context = appContext.applicationContext
    }

    /**
     * Gets the stored context.
     * Returns null if not initialized yet.
     */
    internal fun get(): Context? = context
}
