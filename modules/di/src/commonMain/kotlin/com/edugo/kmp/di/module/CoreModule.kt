package com.edugo.kmp.di.module

import org.koin.dsl.module

/**
 * Core Module - Dependency injection configuration for kmp-core module.
 *
 * This module is intentionally empty for now, as the kmp-core module provides
 * utility functions and types that don't require dependency injection.
 *
 * However, it's defined as a placeholder for future needs, such as:
 * - Custom Result transformers
 * - Error handling strategies
 * - Core service interfaces
 *
 * @see com.edugo.kmp.core.Result
 * @see com.edugo.kmp.core.AppError
 */
public val coreModule = module {
    // Currently empty - kmp-core provides utility functions that don't need DI
    // Future enhancements may include:
    // - single<ErrorHandler> { DefaultErrorHandler() }
    // - single<ResultTransformer> { DefaultResultTransformer() }
}
