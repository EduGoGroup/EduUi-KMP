package com.edugo.kmp.di.module

import org.koin.dsl.module

/**
 * Validation Module - Dependency injection configuration for kmp-validation module.
 *
 * This module is intentionally empty for now, as the kmp-validation module provides
 * validator functions that don't require dependency injection.
 *
 * However, it's defined as a placeholder for future needs, such as:
 * - Custom validator registries
 * - Validation rule configurations
 * - Localized error messages
 *
 * @see com.edugo.kmp.validation.validators
 */
public val validationModule = module {
    // Currently empty - kmp-validation provides validator functions that don't need DI
    // Future enhancements may include:
    // - single<ValidatorRegistry> { DefaultValidatorRegistry() }
    // - single<ValidationConfig> { ValidationConfig.Default }
    // - single<ErrorMessageProvider> { LocalizedErrorMessageProvider() }
}
