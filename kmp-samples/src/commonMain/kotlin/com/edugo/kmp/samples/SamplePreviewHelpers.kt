package com.edugo.kmp.samples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.edugo.kmp.design.EduGoTheme
import com.edugo.kmp.resources.InitStringsForPreview

/**
 * Wrapper basico para previews con tema EduGo.
 */
@Composable
fun SamplePreview(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    InitStringsForPreview()
    EduGoTheme(darkTheme = darkTheme) {
        content()
    }
}

/**
 * Tamanios de dispositivo para previews multi-pantalla.
 */
enum class DeviceSize(val widthDp: Int, val heightDp: Int, val label: String) {
    PHONE(360, 800, "Phone"),
    PHONE_LANDSCAPE(800, 360, "Phone Landscape"),
    TABLET(800, 1280, "Tablet"),
    TABLET_LANDSCAPE(1280, 800, "Tablet Landscape"),
    DESKTOP(1400, 900, "Desktop"),
}

/**
 * Preview con tamanio de dispositivo especifico.
 * Usa esto para simular como se ve el contenido en diferentes pantallas.
 *
 * Ejemplo:
 * ```
 * @Preview
 * @Composable
 * fun MyScreenTabletPreview() {
 *     SampleDevicePreview(DeviceSize.TABLET) {
 *         MyScreenContent()
 *     }
 * }
 * ```
 */
@Composable
fun SampleDevicePreview(
    device: DeviceSize,
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    InitStringsForPreview()
    EduGoTheme(darkTheme = darkTheme) {
        Box(
            modifier = Modifier
                .width(device.widthDp.dp)
                .height(device.heightDp.dp),
        ) {
            content()
        }
    }
}
