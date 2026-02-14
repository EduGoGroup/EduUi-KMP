package com.edugo.kmp.design.tokens

/**
 * Z-index layering values for stacking order of overlapping UI elements.
 *
 * Use these constants with [Modifier.zIndex] to ensure consistent
 * layering across the application.
 */
object ZIndex {
    /** Default layer for regular content. */
    const val base = 0f

    /** Persistent navigation elements (bottom bar, rail, drawer). */
    const val navigation = 10f

    /** Dropdown menus and popups. */
    const val dropdown = 100f

    /** Sticky headers and pinned elements. */
    const val sticky = 200f

    /** Modal dialogs and bottom sheets. */
    const val modal = 1000f

    /** Tooltips floating above modals. */
    const val tooltip = 1100f

    /** Full-screen overlays and scrims. */
    const val overlay = 2000f
}
