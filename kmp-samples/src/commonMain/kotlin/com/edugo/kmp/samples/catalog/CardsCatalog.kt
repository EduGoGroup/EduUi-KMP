@file:Suppress("DEPRECATION")

package com.edugo.kmp.samples.catalog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.edugo.kmp.design.Spacing
import com.edugo.kmp.design.components.cards.DSElevatedCard
import com.edugo.kmp.design.components.cards.DSFilledCard
import com.edugo.kmp.design.components.cards.DSOutlinedCard
import com.edugo.kmp.samples.SamplePreview
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
private fun SampleCardContent(title: String, body: String) {
    Text(title, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(Spacing.spacing2))
    Text(body, style = MaterialTheme.typography.bodyMedium)
}

@Composable
fun CardsCatalog() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.spacing4),
    ) {
        Text("Cards Catalog", style = MaterialTheme.typography.headlineSmall)

        // --- DSFilledCard ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSFilledCard", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With text content", style = MaterialTheme.typography.labelMedium)
        DSFilledCard(modifier = Modifier.fillMaxWidth()) {
            SampleCardContent(
                title = "Curso de Matematicas",
                body = "Aprende los fundamentos de algebra y calculo con ejercicios practicos.",
            )
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Clickable", style = MaterialTheme.typography.labelMedium)
        DSFilledCard(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        ) {
            SampleCardContent(
                title = "Card clickable",
                body = "Esta card responde al toque del usuario.",
            )
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled", style = MaterialTheme.typography.labelMedium)
        DSFilledCard(
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SampleCardContent(
                title = "Card deshabilitada",
                body = "Esta card no puede ser interactuada.",
            )
        }

        // --- DSElevatedCard ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSElevatedCard", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With text content", style = MaterialTheme.typography.labelMedium)
        DSElevatedCard(modifier = Modifier.fillMaxWidth()) {
            SampleCardContent(
                title = "Curso de Historia",
                body = "Explora las civilizaciones antiguas y eventos que marcaron la humanidad.",
            )
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Clickable", style = MaterialTheme.typography.labelMedium)
        DSElevatedCard(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        ) {
            SampleCardContent(
                title = "Card con elevacion",
                body = "Card con sombra que responde al toque.",
            )
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled", style = MaterialTheme.typography.labelMedium)
        DSElevatedCard(
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SampleCardContent(
                title = "Card deshabilitada",
                body = "Card con elevacion en estado deshabilitado.",
            )
        }

        // --- DSOutlinedCard ---
        Spacer(Modifier.height(Spacing.spacing6))
        Text("DSOutlinedCard", style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(Spacing.spacing4))
        Text("With text content", style = MaterialTheme.typography.labelMedium)
        DSOutlinedCard(modifier = Modifier.fillMaxWidth()) {
            SampleCardContent(
                title = "Curso de Ciencias",
                body = "Descubre los principios basicos de la fisica y la quimica.",
            )
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Clickable", style = MaterialTheme.typography.labelMedium)
        DSOutlinedCard(
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        ) {
            SampleCardContent(
                title = "Card con borde",
                body = "Card con borde que responde al toque.",
            )
        }

        Spacer(Modifier.height(Spacing.spacing4))
        Text("Disabled", style = MaterialTheme.typography.labelMedium)
        DSOutlinedCard(
            onClick = {},
            enabled = false,
            modifier = Modifier.fillMaxWidth(),
        ) {
            SampleCardContent(
                title = "Card deshabilitada",
                body = "Card con borde en estado deshabilitado.",
            )
        }

        Spacer(Modifier.height(Spacing.spacing6))
    }
}

@Preview
@Composable
fun CardsCatalogPreview() {
    SamplePreview {
        Surface {
            CardsCatalog()
        }
    }
}

@Preview
@Composable
fun CardsCatalogDarkPreview() {
    SamplePreview(darkTheme = true) {
        Surface {
            CardsCatalog()
        }
    }
}
