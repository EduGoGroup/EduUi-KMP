package com.edugo.kmp.samples.patterns.dynamic

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Tipos de control soportados para formularios dinamicos.
 */
@Serializable
enum class FieldType {
    @SerialName("text") TEXT,
    @SerialName("email") EMAIL,
    @SerialName("password") PASSWORD,
    @SerialName("number") NUMBER,
    @SerialName("textarea") TEXTAREA,
    @SerialName("checkbox") CHECKBOX,
    @SerialName("switch") SWITCH,
    @SerialName("radio") RADIO,
    @SerialName("date") DATE,
    @SerialName("select") SELECT,
}

/**
 * Definicion de un campo dentro de un formulario dinamico.
 * Esta clase se deserializa desde JSON y contiene toda la metadata
 * necesaria para renderizar el control correspondiente.
 */
@Serializable
data class DynamicField(
    val id: String,
    val label: String,
    val type: FieldType,
    val placeholder: String = "",
    val required: Boolean = false,
    val options: List<String> = emptyList(),
    val defaultValue: String = "",
)

/**
 * Seccion agrupadora de campos (equivale a un fieldset/seccion visual).
 */
@Serializable
data class DynamicSection(
    val title: String,
    val fields: List<DynamicField>,
)

/**
 * Definicion completa de un formulario dinamico.
 * Esto es lo que viene desde el servidor/base de datos.
 */
@Serializable
data class DynamicFormDefinition(
    val title: String,
    val submitLabel: String = "Enviar",
    val sections: List<DynamicSection>,
)

/**
 * Parser de JSON hacia definicion de formulario.
 */
object DynamicFormParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(jsonString: String): DynamicFormDefinition =
        json.decodeFromString<DynamicFormDefinition>(jsonString)
}
