package com.edugo.kmp.samples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
    DESKTOP_WIDE(1920, 1080, "Desktop Wide"),
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

/**
 * Breakpoints responsivos para layouts adaptativos.
 * Sigue las guias de Material Design 3:
 * - Compact: < 600dp (phones)
 * - Medium: 600dp - 840dp (tablets, foldables)
 * - Expanded: > 840dp (desktops, large tablets)
 */
object ResponsiveBreakpoints {
    val COMPACT = 600.dp
    val MEDIUM = 840.dp
}

/**
 * Layout adaptativo que cambia el contenido segun el ancho disponible.
 * Usa breakpoints de Material Design 3 para decidir que layout mostrar.
 *
 * Ejemplo:
 * ```
 * AdaptiveLayout(
 *     compact = { MobileLayout() },
 *     medium = { TabletLayout() },
 *     expanded = { DesktopLayout() },
 * )
 * ```
 */
@Composable
fun AdaptiveLayout(
    compact: @Composable () -> Unit,
    medium: @Composable () -> Unit = compact,
    expanded: @Composable () -> Unit = medium,
) {
    BoxWithConstraints {
        when {
            maxWidth < ResponsiveBreakpoints.COMPACT -> compact()
            maxWidth < ResponsiveBreakpoints.MEDIUM -> medium()
            else -> expanded()
        }
    }
}
