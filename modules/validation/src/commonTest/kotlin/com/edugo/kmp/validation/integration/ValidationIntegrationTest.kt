package com.edugo.kmp.validation.integration

import com.edugo.kmp.foundation.entity.ValidatableModel
import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.foundation.result.success
import com.edugo.kmp.foundation.result.map
import com.edugo.kmp.validation.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Suite de tests de integracion para el modulo de validacion.
 *
 * Prueba escenarios reales combinando multiples validadores y patrones:
 * - Formularios de registro de usuarios
 * - Validacion de perfiles
 * - Validacion de productos en e-commerce
 * - Validacion de datos financieros
 * - Validacion de configuraciones
 * - Validacion de entidades complejas
 *
 * Estos tests verifican que los validadores funcionen correctamente
 * cuando se usan en conjunto, simulando casos de uso reales.
 */
class ValidationIntegrationTest {

    // ========== User Registration Form Tests ==========

    @Test
    fun `user registration form with all valid data`() {
        val form = UserRegistrationForm(
            email = "user@example.com",
            password = "SecurePass123",
            passwordConfirmation = "SecurePass123",
            username = "johndoe",
            age = 25,
            termsAccepted = true
        )

        val result = form.validate()
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `user registration form with multiple validation errors`() {
        val form = UserRegistrationForm(
            email = "invalid-email",
            password = "short",
            passwordConfirmation = "different",
            username = "ab",
            age = 15,
            termsAccepted = false
        )

        val result = form.validate()
        assertIs<Result.Failure>(result)

        // Should accumulate all errors
        val error = result.error
        assertTrue(error.contains("email"), "Should contain email error")
        assertTrue(error.contains("Password must be"), "Should contain password length error")
        assertTrue(error.contains("do not match"), "Should contain password mismatch error")
        assertTrue(error.contains("Username must be"), "Should contain username error")
        assertTrue(error.contains("Age must be"), "Should contain age error")
        assertTrue(error.contains("terms"), "Should contain terms acceptance error")
    }

    @Test
    fun `user registration form with partial errors`() {
        val form = UserRegistrationForm(
            email = "user@example.com",
            password = "ValidPassword123",
            passwordConfirmation = "ValidPassword123",
            username = "ab", // Too short
            age = 15, // Too young
            termsAccepted = true
        )

        val result = form.validate()
        assertIs<Result.Failure>(result)

        val error = result.error
        assertTrue(error.contains("Username must be between 3 and 30 characters"))
        assertTrue(error.contains("Age must be between 18 and 120"))
    }

    // ========== User Profile Update Tests ==========

    @Test
    fun `update profile with valid data`() {
        val profile = UserProfileUpdate(
            firstName = "John",
            lastName = "Doe",
            bio = "Software developer",
            age = 30,
            website = "https://johndoe.com",
            tags = listOf("kotlin", "multiplatform", "mobile")
        )

        val result = profile.validate()
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `update profile with bio too long`() {
        val longBio = "a".repeat(501)
        val profile = UserProfileUpdate(
            firstName = "John",
            lastName = "Doe",
            bio = longBio,
            age = 30,
            website = "https://johndoe.com",
            tags = listOf("kotlin")
        )

        val result = profile.validate()
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Bio must be at most 500 characters"))
    }

    @Test
    fun `update profile with too many tags`() {
        val profile = UserProfileUpdate(
            firstName = "John",
            lastName = "Doe",
            bio = "Developer",
            age = 30,
            website = "https://johndoe.com",
            tags = List(11) { "tag$it" } // More than 10
        )

        val result = profile.validate()
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Tags cannot contain more than 10 items"))
    }

    @Test
    fun `update profile with empty tags`() {
        val profile = UserProfileUpdate(
            firstName = "John",
            lastName = "Doe",
            bio = "Developer",
            age = 30,
            website = "https://johndoe.com",
            tags = emptyList()
        )

        val result = profile.validate()
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Tags must contain at least 1 items"))
    }

    // ========== E-commerce Product Validation Tests ==========

    @Test
    fun `product with valid data`() {
        val product = ProductDto(
            name = "Kotlin T-Shirt",
            description = "Comfortable cotton t-shirt",
            price = 29.99,
            stock = 100,
            category = "clothing",
            tags = listOf("kotlin", "programming", "fashion"),
            onSale = false,
            discount = null
        )

        val result = product.validate()
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `product on sale with valid discount`() {
        val product = ProductDto(
            name = "Kotlin Mug",
            description = "Ceramic mug with Kotlin logo",
            price = 15.99,
            stock = 50,
            category = "accessories",
            tags = listOf("kotlin", "coffee"),
            onSale = true,
            discount = 20
        )

        val result = product.validate()
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `product on sale without discount should fail`() {
        val product = ProductDto(
            name = "Kotlin Mug",
            description = "Ceramic mug",
            price = 15.99,
            stock = 50,
            category = "accessories",
            tags = listOf("kotlin"),
            onSale = true,
            discount = null // Missing discount
        )

        val result = product.validate()
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Discount is required when product is on sale"))
    }

    @Test
    fun `product with invalid discount range`() {
        val product = ProductDto(
            name = "Kotlin Book",
            description = "Learn Kotlin",
            price = 49.99,
            stock = 25,
            category = "books",
            tags = listOf("kotlin"),
            onSale = true,
            discount = 150 // Invalid: > 100
        )

        val result = product.validate()
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Discount must be between 0 and 100"))
    }

    @Test
    fun `product with invalid category`() {
        val product = ProductDto(
            name = "Unknown Item",
            description = "Some item",
            price = 10.0,
            stock = 10,
            category = "invalid_category",
            tags = listOf("misc"),
            onSale = false,
            discount = null
        )

        val result = product.validate()
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Category must be one of"))
    }

    // ========== Financial Transaction Tests ==========

    @Test
    fun `valid financial transaction`() {
        val transaction = FinancialTransaction(
            id = "550e8400-e29b-41d4-a716-446655440000",
            amount = 100.50,
            currency = "USD",
            description = "Payment for services",
            category = "services"
        )

        val result = transaction.validate()
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `financial transaction with invalid UUID`() {
        val transaction = FinancialTransaction(
            id = "not-a-uuid",
            amount = 100.50,
            currency = "USD",
            description = "Payment",
            category = "services"
        )

        val result = transaction.validate()
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("UUID"))
    }

    @Test
    fun `financial transaction with negative amount`() {
        val transaction = FinancialTransaction(
            id = "550e8400-e29b-41d4-a716-446655440000",
            amount = -50.0,
            currency = "USD",
            description = "Payment",
            category = "services"
        )

        val result = transaction.validate()
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Amount must be positive"))
    }

    @Test
    fun `financial transaction with invalid currency`() {
        val transaction = FinancialTransaction(
            id = "550e8400-e29b-41d4-a716-446655440000",
            amount = 100.0,
            currency = "INVALID",
            description = "Payment",
            category = "services"
        )

        val result = transaction.validate()
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Currency must be one of"))
    }

    // ========== Configuration Object Tests ==========

    @Test
    fun `valid app configuration`() {
        val config = AppConfiguration(
            apiUrl = "https://api.example.com",
            timeout = 30,
            maxRetries = 3,
            enableLogging = true,
            logLevel = "INFO",
            apiKeys = listOf("key1", "key2")
        )

        val result = config.validate()
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `app configuration with invalid URL pattern`() {
        val config = AppConfiguration(
            apiUrl = "not-a-url",
            timeout = 30,
            maxRetries = 3,
            enableLogging = true,
            logLevel = "INFO",
            apiKeys = listOf("key1")
        )

        val result = config.validate()
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("API URL"))
    }

    @Test
    fun `app configuration with invalid timeout`() {
        val config = AppConfiguration(
            apiUrl = "https://api.example.com",
            timeout = -5, // Invalid
            maxRetries = 3,
            enableLogging = true,
            logLevel = "INFO",
            apiKeys = listOf("key1")
        )

        val result = config.validate()
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Timeout must be positive"))
    }

    @Test
    fun `app configuration with empty api keys when logging enabled`() {
        val config = AppConfiguration(
            apiUrl = "https://api.example.com",
            timeout = 30,
            maxRetries = 3,
            enableLogging = true,
            logLevel = "INFO",
            apiKeys = emptyList()
        )

        val result = config.validate()
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("API keys cannot be empty"))
    }

    // ========== Complex Entity Tests ==========

    @Test
    fun `validate list of users accumulates all errors`() {
        val users = listOf(
            SimpleUser(email = "valid@example.com", age = 25),
            SimpleUser(email = "invalid", age = 15), // 2 errors
            SimpleUser(email = "another@example.com", age = 30),
            SimpleUser(email = "", age = -5) // 2 errors
        )

        val result = users.validateAllAccumulative()
        assertIs<Result.Failure>(result)

        val error = result.error
        assertTrue(error.contains("Item 2"))
        assertTrue(error.contains("Item 4"))
    }

    @Test
    fun `validate list of valid users succeeds`() {
        val users = listOf(
            SimpleUser(email = "user1@example.com", age = 25),
            SimpleUser(email = "user2@example.com", age = 30),
            SimpleUser(email = "user3@example.com", age = 35)
        )

        val result = users.validateAllAccumulative()
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `nested validation with conditional requirements`() {
        val orderWithShipping = OrderForm(
            customerEmail = "customer@example.com",
            items = listOf("item1", "item2"),
            total = 100.0,
            requiresShipping = true,
            shippingAddress = "123 Main St, City, Country"
        )

        val result = orderWithShipping.validate()
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `nested validation fails when shipping required but address missing`() {
        val orderWithoutAddress = OrderForm(
            customerEmail = "customer@example.com",
            items = listOf("item1"),
            total = 50.0,
            requiresShipping = true,
            shippingAddress = null // Missing required address
        )

        val result = orderWithoutAddress.validate()
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Shipping address"))
    }

    @Test
    fun `combine multiple validation results`() {
        val emailResult = "user@example.com".validateEmail()
        val uuidResult = "550e8400-e29b-41d4-a716-446655440000".validateUUID()

        val combined = combineValidations(
            emailResult.map { Unit },
            uuidResult.map { Unit }
        )

        assertIs<Result.Success<Unit>>(combined)
    }

    @Test
    fun `combine validation results with failures`() {
        val emailResult = "invalid-email".validateEmail()
        val uuidResult = "invalid-uuid".validateUUID()

        val combined = combineValidations(
            emailResult.map { Unit },
            uuidResult.map { Unit }
        )

        assertIs<Result.Failure>(combined)
        val error = combined.error
        assertTrue(error.contains("email"))
        assertTrue(error.contains("UUID"))
    }

    // ========== Extension Functions Integration Tests ==========

    @Test
    fun `chain extension functions with Result operations`() {
        val result = "user@example.com"
            .validateEmail()
            .map { it.lowercase() }

        assertIs<Result.Success<String>>(result)
        assertEquals("user@example.com", result.data)
    }

    @Test
    fun `use isValid functions in complex validations`() {
        data class Contact(val email: String?, val id: String?)

        val contact = Contact(
            email = "test@example.com",
            id = "550e8400-e29b-41d4-a716-446655440000"
        )

        val result = accumulateValidationErrors {
            if (contact.email != null && !contact.email.isValidEmail()) {
                add("Invalid email format")
            }
            if (contact.id != null && !contact.id.isValidUUID()) {
                add("Invalid UUID format")
            }
        }

        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `validateAtLeastOne with email or phone requirement`() {
        data class ContactInfo(val email: String?, val phone: String?)

        // Valid: has email
        val withEmail = ContactInfo(email = "user@example.com", phone = null)
        val result1 = accumulateValidationErrors {
            add(validateAtLeastOne(
                "Must provide email or phone",
                { validateEmail(withEmail.email) },
                { validateNotBlank(withEmail.phone, "Phone") }
            ))
        }
        assertIs<Result.Success<Unit>>(result1)

        // Valid: has phone
        val withPhone = ContactInfo(email = null, phone = "123-456-7890")
        val result2 = accumulateValidationErrors {
            add(validateAtLeastOne(
                "Must provide email or phone",
                { validateEmail(withPhone.email) },
                { validateNotBlank(withPhone.phone, "Phone") }
            ))
        }
        assertIs<Result.Success<Unit>>(result2)

        // Invalid: has neither
        val withNeither = ContactInfo(email = null, phone = null)
        val result3 = accumulateValidationErrors {
            add(validateAtLeastOne(
                "Must provide email or phone",
                { validateEmail(withNeither.email) },
                { validateNotBlank(withNeither.phone, "Phone") }
            ))
        }
        assertIs<Result.Failure>(result3)
        assertTrue(result3.error.contains("Must provide email or phone"))
    }

    // ========== Test Models ==========

    data class UserRegistrationForm(
        val email: String,
        val password: String,
        val passwordConfirmation: String,
        val username: String,
        val age: Int,
        val termsAccepted: Boolean
    ) : ValidatableModel {
        override fun validate(): Result<Unit> = accumulateValidationErrors {
            add(validateEmail(email))
            add(validateLengthRange(password, 8, 100, "Password"))
            if (!password.matchesPassword(passwordConfirmation)) {
                add("Passwords do not match")
            }
            add(validateLengthRange(username, 3, 30, "Username"))
            add(validateRange(age, 18, 120, "Age"))
            if (!termsAccepted) {
                add("You must accept the terms and conditions")
            }
        }
    }

    data class UserProfileUpdate(
        val firstName: String,
        val lastName: String,
        val bio: String,
        val age: Int,
        val website: String,
        val tags: List<String>
    ) : ValidatableModel {
        override fun validate(): Result<Unit> = accumulateValidationErrors {
            add(validateNotBlank(firstName, "First name"))
            add(validateNotBlank(lastName, "Last name"))
            add(validateMaxLength(bio, 500, "Bio"))
            add(validateRange(age, 13, 150, "Age"))
            add(validatePattern(website, Regex("https?://.*"), "Website"))
            add(validateMinSize(tags, 1, "Tags"))
            add(validateMaxSize(tags, 10, "Tags"))
        }
    }

    data class ProductDto(
        val name: String,
        val description: String,
        val price: Double,
        val stock: Int,
        val category: String,
        val tags: List<String>,
        val onSale: Boolean,
        val discount: Int?
    ) : ValidatableModel {
        override fun validate(): Result<Unit> = accumulateValidationErrors {
            add(validateNotBlank(name, "Product name"))
            add(validateLengthRange(description, 10, 500, "Description"))
            add(validatePositive(price, "Price"))
            add(validateNonNegative(stock, "Stock"))
            add(validateIn(
                category,
                listOf("electronics", "clothing", "books", "accessories"),
                "Category"
            ))
            add(validateNotEmpty(tags, "Tags"))

            // Conditional validation
            if (onSale) {
                if (discount == null) {
                    add("Discount is required when product is on sale")
                } else {
                    add(validateRange(discount, 0, 100, "Discount"))
                }
            }
        }
    }

    data class FinancialTransaction(
        val id: String,
        val amount: Double,
        val currency: String,
        val description: String,
        val category: String
    ) : ValidatableModel {
        override fun validate(): Result<Unit> = accumulateValidationErrors {
            if (!id.isValidUUID()) {
                add("Transaction ID must be a valid UUID")
            }
            add(validatePositive(amount, "Amount"))
            add(validateIn(
                currency,
                listOf("USD", "EUR", "GBP", "JPY", "MXN"),
                "Currency"
            ))
            add(validateNotBlank(description, "Description"))
            add(validateIn(
                category,
                listOf("income", "expense", "transfer", "services"),
                "Category"
            ))
        }
    }

    data class AppConfiguration(
        val apiUrl: String,
        val timeout: Int,
        val maxRetries: Int,
        val enableLogging: Boolean,
        val logLevel: String,
        val apiKeys: List<String>
    ) : ValidatableModel {
        override fun validate(): Result<Unit> = accumulateValidationErrors {
            add(validatePattern(apiUrl, Regex("https?://.*"), "API URL"))
            add(validatePositive(timeout, "Timeout"))
            add(validateRange(maxRetries, 0, 10, "Max retries"))
            add(validateIn(
                logLevel,
                listOf("DEBUG", "INFO", "WARN", "ERROR"),
                "Log level"
            ))

            if (enableLogging) {
                add(validateNotEmpty(apiKeys, "API keys"))
            }
        }
    }

    data class SimpleUser(
        val email: String,
        val age: Int
    ) : ValidatableModel {
        override fun validate(): Result<Unit> = accumulateValidationErrors {
            add(validateEmail(email))
            add(validateRange(age, 18, 120, "Age"))
        }
    }

    data class OrderForm(
        val customerEmail: String,
        val items: List<String>,
        val total: Double,
        val requiresShipping: Boolean,
        val shippingAddress: String?
    ) : ValidatableModel {
        override fun validate(): Result<Unit> = accumulateValidationErrors {
            add(validateEmail(customerEmail))
            add(validateNotEmpty(items, "Items"))
            add(validatePositive(total, "Total"))

            // Conditional validation
            add(validateIf(requiresShipping) {
                validateNotBlank(shippingAddress, "Shipping address")
            })
        }
    }
}
