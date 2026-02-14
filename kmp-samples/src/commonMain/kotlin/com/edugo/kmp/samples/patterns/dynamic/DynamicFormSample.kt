@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.patterns.dynamic

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.samples.DeviceSize
import com.edugo.kmp.samples.SampleDevicePreview
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

// ============================================================================
// JSON de ejemplo: Formulario de Registro de Estudiante
// Este JSON podria venir de un backend, base de datos, o API.
// ============================================================================

private val registroEstudianteJson = """
{
    "title": "Registro de Estudiante",
    "submitLabel": "Registrar",
    "sections": [
        {
            "title": "Datos Personales",
            "fields": [
                {
                    "id": "nombre",
                    "label": "Nombre completo",
                    "type": "text",
                    "placeholder": "Ej: Juan Perez",
                    "required": true
                },
                {
                    "id": "email",
                    "label": "Correo electronico",
                    "type": "email",
                    "placeholder": "usuario@ejemplo.com",
                    "required": true
                },
                {
                    "id": "password",
                    "label": "Contrasena",
                    "type": "password",
                    "placeholder": "Minimo 8 caracteres",
                    "required": true
                },
                {
                    "id": "fecha_nacimiento",
                    "label": "Fecha de nacimiento",
                    "type": "date",
                    "placeholder": "DD/MM/AAAA"
                },
                {
                    "id": "edad",
                    "label": "Edad",
                    "type": "number",
                    "placeholder": "Ej: 25"
                }
            ]
        },
        {
            "title": "Informacion Academica",
            "fields": [
                {
                    "id": "nivel",
                    "label": "Nivel educativo",
                    "type": "radio",
                    "required": true,
                    "options": ["Bachillerato", "Pregrado", "Posgrado"]
                },
                {
                    "id": "carrera",
                    "label": "Carrera o programa",
                    "type": "text",
                    "placeholder": "Ej: Ingenieria de Sistemas"
                },
                {
                    "id": "semestre",
                    "label": "Semestre",
                    "type": "select",
                    "options": ["1-2", "3-4", "5-6", "7+"]
                },
                {
                    "id": "biografia",
                    "label": "Acerca de ti",
                    "type": "textarea",
                    "placeholder": "Cuentanos sobre tus intereses academicos..."
                }
            ]
        },
        {
            "title": "Preferencias",
            "fields": [
                {
                    "id": "notificaciones",
                    "label": "Recibir notificaciones de cursos",
                    "type": "switch",
                    "defaultValue": "true"
                },
                {
                    "id": "newsletter",
                    "label": "Suscribirse al boletin semanal",
                    "type": "checkbox"
                },
                {
                    "id": "terminos",
                    "label": "Acepto los terminos y condiciones",
                    "type": "checkbox",
                    "required": true
                }
            ]
        }
    ]
}
""".trimIndent()

// ============================================================================
// Segundo ejemplo: Formulario de Contacto (mas sencillo)
// Demuestra que el mismo renderer sirve para cualquier formulario.
// ============================================================================

private val contactoJson = """
{
    "title": "Contacto",
    "submitLabel": "Enviar mensaje",
    "sections": [
        {
            "title": "Tus datos",
            "fields": [
                {
                    "id": "nombre",
                    "label": "Nombre",
                    "type": "text",
                    "required": true
                },
                {
                    "id": "email",
                    "label": "Email",
                    "type": "email",
                    "required": true
                }
            ]
        },
        {
            "title": "Mensaje",
            "fields": [
                {
                    "id": "asunto",
                    "label": "Asunto",
                    "type": "text",
                    "placeholder": "Asunto del mensaje"
                },
                {
                    "id": "mensaje",
                    "label": "Mensaje",
                    "type": "textarea",
                    "placeholder": "Escribe tu mensaje aqui...",
                    "required": true
                }
            ]
        }
    ]
}
""".trimIndent()

// ============================================================================
// Previews: Formulario de Registro de Estudiante
// ============================================================================

@Preview
@Composable
fun DynamicFormRegistroLightPreview() {
    SamplePreview {
        val definition = DynamicFormParser.parse(registroEstudianteJson)
        DynamicFormRenderer(
            definition = definition,
            onSubmit = {},
        )
    }
}

@Preview
@Composable
fun DynamicFormRegistroDarkPreview() {
    SamplePreview(darkTheme = true) {
        val definition = DynamicFormParser.parse(registroEstudianteJson)
        DynamicFormRenderer(
            definition = definition,
            onSubmit = {},
        )
    }
}

@Preview
@Composable
fun DynamicFormRegistroLandscapePreview() {
    SampleDevicePreview(device = DeviceSize.PHONE_LANDSCAPE) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val definition = DynamicFormParser.parse(registroEstudianteJson)
            DynamicFormRenderer(
                definition = definition,
                onSubmit = {},
            )
        }
    }
}

@Preview
@Composable
fun DynamicFormRegistroTabletPreview() {
    SampleDevicePreview(device = DeviceSize.TABLET) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val definition = DynamicFormParser.parse(registroEstudianteJson)
            DynamicFormRenderer(
                definition = definition,
                onSubmit = {},
            )
        }
    }
}

// ============================================================================
// Previews: Formulario de Contacto (demuestra reutilizacion)
// ============================================================================

@Preview
@Composable
fun DynamicFormContactoLightPreview() {
    SamplePreview {
        val definition = DynamicFormParser.parse(contactoJson)
        DynamicFormRenderer(
            definition = definition,
            onSubmit = {},
        )
    }
}

@Preview
@Composable
fun DynamicFormContactoDarkPreview() {
    SamplePreview(darkTheme = true) {
        val definition = DynamicFormParser.parse(contactoJson)
        DynamicFormRenderer(
            definition = definition,
            onSubmit = {},
        )
    }
}
