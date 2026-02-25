package com.edugo.kmp.screens.dynamic.renderer

object DisplayValueFormatter {

    fun formatBoolean(value: String): String = when (value.lowercase()) {
        "true" -> "Activo"
        "false" -> "Inactivo"
        else -> value
    }

    fun formatDate(value: String): String {
        // Parse ISO 8601 date like "2026-02-15T10:30:00Z" or "2026-02-15"
        val datePart = value.substringBefore("T")
        val parts = datePart.split("-")
        if (parts.size != 3) return value
        val year = parts[0]
        val month = parts[1]
        val day = parts[2]
        if (year.length != 4 || month.length != 2 || day.length != 2) return value
        return "$day/$month/$year"
    }

    private val isoDateRegex = Regex("""\d{4}-\d{2}-\d{2}(T\d{2}:\d{2}.*)?""")

    fun autoFormat(value: String): String = when {
        value.lowercase() == "true" || value.lowercase() == "false" -> formatBoolean(value)
        isoDateRegex.matches(value) -> formatDate(value)
        else -> value
    }
}
