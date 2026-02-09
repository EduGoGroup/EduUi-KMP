package com.edugo.kmp.validation

import com.edugo.kmp.foundation.result.Result
import com.edugo.kmp.foundation.entity.ValidatableModel
import com.edugo.kmp.foundation.result.failure
import com.edugo.kmp.foundation.result.success
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Suite de tests para AccumulativeValidation.
 *
 * Verifica:
 * - ValidationErrorAccumulator
 * - accumulateValidationErrors DSL
 * - validateWith extension
 * - validateAllAccumulative para ValidatableModel
 * - combineValidations
 * - validateIf condicional
 * - validateAtLeastOne (OR logic)
 * - validateAll (AND logic)
 */
class AccumulativeValidationTest {

    // ========== Tests de ValidationErrorAccumulator ==========

    @Test
    fun `accumulator sin errores retorna Success`() {
        val accumulator = ValidationErrorAccumulator()
        val result = accumulator.build()

        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `accumulator con un error retorna Failure`() {
        val accumulator = ValidationErrorAccumulator()
        accumulator.add("Error 1")
        val result = accumulator.build()

        assertIs<Result.Failure>(result)
        assertEquals("Error 1", result.error)
    }

    @Test
    fun `accumulator con multiples errores los concatena`() {
        val accumulator = ValidationErrorAccumulator()
        accumulator.add("Error 1")
        accumulator.add("Error 2")
        accumulator.add("Error 3")
        val result = accumulator.build()

        assertIs<Result.Failure>(result)
        assertEquals("Error 1; Error 2; Error 3", result.error)
    }

    @Test
    fun `accumulator ignora errores null`() {
        val accumulator = ValidationErrorAccumulator()
        accumulator.add(null)
        accumulator.add("Real error")
        accumulator.add(null)
        val result = accumulator.build()

        assertIs<Result.Failure>(result)
        assertEquals("Real error", result.error)
    }

    @Test
    fun `accumulator con separador custom`() {
        val accumulator = ValidationErrorAccumulator()
        accumulator.add("Error 1")
        accumulator.add("Error 2")
        val result = accumulator.build(separator = " | ")

        assertIs<Result.Failure>(result)
        assertEquals("Error 1 | Error 2", result.error)
    }

    @Test
    fun `accumulator puede agregar lista de errores`() {
        val accumulator = ValidationErrorAccumulator()
        accumulator.add(listOf("Error 1", null, "Error 2"))
        val result = accumulator.build()

        assertIs<Result.Failure>(result)
        assertEquals("Error 1; Error 2", result.error)
    }

    @Test
    fun `accumulator getErrors retorna lista inmutable`() {
        val accumulator = ValidationErrorAccumulator()
        accumulator.add("Error 1")
        accumulator.add("Error 2")

        val errors = accumulator.getErrors()
        assertEquals(2, errors.size)
        assertEquals("Error 1", errors[0])
        assertEquals("Error 2", errors[1])
    }

    @Test
    fun `accumulator hasErrors detecta errores`() {
        val accumulator = ValidationErrorAccumulator()
        assertFalse(accumulator.hasErrors())

        accumulator.add("Error")
        assertTrue(accumulator.hasErrors())
    }

    @Test
    fun `accumulator errorCount cuenta errores`() {
        val accumulator = ValidationErrorAccumulator()
        assertEquals(0, accumulator.errorCount())

        accumulator.add("Error 1")
        assertEquals(1, accumulator.errorCount())

        accumulator.add("Error 2")
        assertEquals(2, accumulator.errorCount())
    }

    @Test
    fun `accumulator clear limpia errores`() {
        val accumulator = ValidationErrorAccumulator()
        accumulator.add("Error 1")
        accumulator.add("Error 2")
        assertEquals(2, accumulator.errorCount())

        accumulator.clear()
        assertEquals(0, accumulator.errorCount())
        assertFalse(accumulator.hasErrors())
    }

    // ========== Tests de accumulateValidationErrors DSL ==========

    @Test
    fun `accumulateValidationErrors sin errores retorna Success`() {
        val result = accumulateValidationErrors {
            add(validateEmail("test@example.com"))
            add(validateRange(25, 18, 120, "Age"))
        }

        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `accumulateValidationErrors con errores retorna Failure`() {
        val result = accumulateValidationErrors {
            add(validateEmail("invalid"))
            add(validateRange(15, 18, 120, "Age"))
        }

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Invalid email format"))
        assertTrue(result.error.contains("Age must be between 18 and 120"))
    }

    @Test
    fun `accumulateValidationErrors con un solo error`() {
        val result = accumulateValidationErrors {
            add(validateEmail("test@example.com"))
            add(validateRange(15, 18, 120, "Age"))
        }

        assertIs<Result.Failure>(result)
        assertEquals("Age must be between 18 and 120", result.error)
    }

    @Test
    fun `accumulateValidationErrors con separador custom`() {
        val result = accumulateValidationErrors(separator = " AND ") {
            add(validateEmail("invalid"))
            add(validateRange(15, 18, 120, "Age"))
        }

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains(" AND "))
    }

    @Test
    fun `accumulateValidationErrors con validaciones condicionales`() {
        val requireEmail = true
        val requirePhone = false

        val result = accumulateValidationErrors {
            if (requireEmail) {
                add(validateEmail("invalid"))
            }
            if (requirePhone) {
                add(validateNotBlank(null, "Phone"))
            }
        }

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("email"))
        assertFalse(result.error.contains("Phone"))
    }

    // ========== Tests de validateWith Extension ==========

    @Test
    fun `validateWith ejecuta validaciones en contexto del objeto`() {
        data class TestData(val email: String, val age: Int)

        val data = TestData("test@example.com", 25)
        val result = data.validateWith {
            add(validateEmail(it.email))
            add(validateRange(it.age, 18, 120, "Age"))
        }

        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `validateWith detecta errores en el objeto`() {
        data class TestData(val email: String, val age: Int)

        val data = TestData("invalid", 15)
        val result = data.validateWith {
            add(validateEmail(it.email))
            add(validateRange(it.age, 18, 120, "Age"))
        }

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Invalid email format"))
        assertTrue(result.error.contains("Age must be between 18 and 120"))
    }

    // ========== Tests de validateAllAccumulative ==========

    @Test
    fun `validateAllAccumulative retorna Success si todos son validos`() {
        val models = listOf(
            SimpleValidModel("valid1"),
            SimpleValidModel("valid2"),
            SimpleValidModel("valid3")
        )

        val result = models.validateAllAccumulative()
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `validateAllAccumulative acumula todos los errores`() {
        val models = listOf(
            SimpleValidModel(""),
            SimpleValidModel("valid"),
            SimpleValidModel("")
        )

        val result = models.validateAllAccumulative()
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Item 1"))
        assertTrue(result.error.contains("Item 3"))
    }

    @Test
    fun `validateAllAccumulative con lista vacia retorna Success`() {
        val models = emptyList<ValidatableModel>()
        val result = models.validateAllAccumulative()
        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `validateAllAccumulative con custom prefix`() {
        val models = listOf(
            SimpleValidModel(""),
            SimpleValidModel("")
        )

        val result = models.validateAllAccumulative(itemPrefix = "User")
        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("User 1"))
        assertTrue(result.error.contains("User 2"))
    }

    // ========== Tests de combineValidations ==========

    @Test
    fun `combineValidations retorna Success si todas las validaciones pasan`() {
        val result = combineValidations(
            success(Unit),
            success(Unit),
            success(Unit)
        )

        assertIs<Result.Success<Unit>>(result)
    }

    @Test
    fun `combineValidations acumula errores`() {
        val result = combineValidations(
            Result.Failure("Error 1"),
            success(Unit),
            Result.Failure("Error 2")
        )

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Error 1"))
        assertTrue(result.error.contains("Error 2"))
    }

    @Test
    fun `combineValidations con separador custom`() {
        val result = combineValidations(
            Result.Failure("Error 1"),
            Result.Failure("Error 2"),
            separator = " + "
        )

        assertIs<Result.Failure>(result)
        assertEquals("Error 1 + Error 2", result.error)
    }

    // ========== Tests de validateIf ==========

    @Test
    fun `validateIf ejecuta validacion si condicion es true`() {
        val result = validateIf(true) {
            validateEmail("invalid")
        }

        assertEquals("Invalid email format", result)
    }

    @Test
    fun `validateIf no ejecuta validacion si condicion es false`() {
        val result = validateIf(false) {
            validateEmail("invalid")
        }

        assertEquals(null, result)
    }

    @Test
    fun `validateIf en accumulateValidationErrors`() {
        val requiresEmail = true
        val requiresPhone = false

        val result = accumulateValidationErrors {
            add(validateIf(requiresEmail) {
                validateEmail("invalid")
            })
            add(validateIf(requiresPhone) {
                validateNotBlank(null, "Phone")
            })
        }

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("email"))
        assertFalse(result.error.contains("Phone"))
    }

    // ========== Tests de validateAtLeastOne ==========

    @Test
    fun `validateAtLeastOne retorna null si una validacion pasa`() {
        val result = validateAtLeastOne(
            "Must have email or phone",
            { validateEmail("test@example.com") },
            { validateNotBlank(null, "Phone") }
        )

        assertEquals(null, result)
    }

    @Test
    fun `validateAtLeastOne retorna error si todas las validaciones fallan`() {
        val result = validateAtLeastOne(
            "Must have email or phone",
            { validateEmail("invalid") },
            { validateNotBlank(null, "Phone") }
        )

        assertEquals("Must have email or phone", result)
    }

    @Test
    fun `validateAtLeastOne con todas las validaciones pasando`() {
        val result = validateAtLeastOne(
            "Error",
            { validateEmail("test1@example.com") },
            { validateEmail("test2@example.com") }
        )

        assertEquals(null, result)
    }

    // ========== Tests de validateAll ==========

    @Test
    fun `validateAll retorna null si todas las validaciones pasan`() {
        val result = validateAll(
            { validateEmail("test@example.com") },
            { validateRange(25, 18, 120, "Age") },
            { validateNotBlank("username", "Username") }
        )

        assertEquals(null, result)
    }

    @Test
    fun `validateAll retorna primer error si alguna validacion falla`() {
        val result = validateAll(
            { validateEmail("test@example.com") },
            { validateRange(15, 18, 120, "Age") },
            { validateNotBlank("username", "Username") }
        )

        assertEquals("Age must be between 18 and 120", result)
    }

    @Test
    fun `validateAll con multiples errores retorna el primero`() {
        val result = validateAll(
            { validateEmail("invalid") },
            { validateRange(15, 18, 120, "Age") }
        )

        assertEquals("Invalid email format", result)
    }

    // ========== Tests de Casos Complejos ==========

    @Test
    fun `validacion acumulativa compleja con multiples tipos de validaciones`() {
        data class ComplexDto(
            val email: String,
            val age: Int,
            val username: String,
            val tags: List<String>
        )

        val dto = ComplexDto(
            email = "invalid",
            age = 15,
            username = "ab",
            tags = emptyList()
        )

        val result = dto.validateWith {
            add(validateEmail(it.email))
            add(validateRange(it.age, 18, 120, "Age"))
            add(validateLengthRange(it.username, 3, 30, "Username"))
            add(validateNotEmpty(it.tags, "Tags"))
        }

        assertIs<Result.Failure>(result)
        val errorParts = result.error.split("; ")
        assertEquals(4, errorParts.size)
    }

    @Test
    fun `validacion con logica condicional compleja`() {
        data class User(val type: String, val email: String?, val phone: String?)

        val user = User(type = "email", email = null, phone = "123-456")

        val result = user.validateWith {
            when (it.type) {
                "email" -> add(validateEmail(it.email))
                "phone" -> add(validateNotBlank(it.phone, "Phone"))
            }
        }

        assertIs<Result.Failure>(result)
        assertTrue(result.error.contains("Invalid email format"))
    }

    @Test
    fun `combinacion de validateIf y validateAtLeastOne`() {
        val isPremiumUser = true
        val hasEmail = false
        val hasPhone = false

        val result = accumulateValidationErrors {
            add(validateIf(isPremiumUser) {
                validateAtLeastOne(
                    "Premium users must have email or phone",
                    { if (hasEmail) null else "No email" },
                    { if (hasPhone) null else "No phone" }
                )
            })
        }

        assertIs<Result.Failure>(result)
        assertEquals("Premium users must have email or phone", result.error)
    }

    @Test
    fun `validacion anidada con multiples niveles`() {
        val result = accumulateValidationErrors {
            add(validateEmail("test@example.com"))

            // Validacion condicional nivel 1
            val hasAddress = true
            if (hasAddress) {
                add(validateNotBlank("Street", "Street"))

                // Validacion condicional nivel 2
                val requiresZipCode = true
                if (requiresZipCode) {
                    add(validatePattern("12345", Regex("\\d{5}"), "ZipCode"))
                }
            }
        }

        assertIs<Result.Success<Unit>>(result)
    }

    // ========== Test Models ==========

    data class SimpleValidModel(val value: String) : ValidatableModel {
        override fun validate(): Result<Unit> {
            return if (value.isBlank()) {
                failure("Value cannot be blank")
            } else {
                success(Unit)
            }
        }
    }
}
