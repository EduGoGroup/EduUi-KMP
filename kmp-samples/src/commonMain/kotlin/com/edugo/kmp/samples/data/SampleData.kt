package com.edugo.kmp.samples.data

data class Contact(
    val name: String,
    val email: String,
    val initials: String,
)

data class Metric(
    val label: String,
    val value: String,
    val change: String,
)

data class NavItem(
    val label: String,
    val iconName: String,
)

data class SearchResult(
    val title: String,
    val description: String,
    val rating: Float,
)

enum class SettingsItemType {
    TOGGLE,
    NAVIGATION,
    TEXT,
}

data class SettingsItem(
    val label: String,
    val type: SettingsItemType,
    val value: String = "",
)

data class SettingsGroup(
    val title: String,
    val items: List<SettingsItem>,
)

object SampleData {

    // Login
    val emailPlaceholder = "usuario@ejemplo.com"
    val passwordPlaceholder = "********"

    // Contacts
    val contacts = listOf(
        Contact("Ana Garcia", "ana.garcia@email.com", "AG"),
        Contact("Bruno Lopez", "bruno.lopez@email.com", "BL"),
        Contact("Carmen Rodriguez", "carmen.rod@email.com", "CR"),
        Contact("Diego Martinez", "diego.martinez@email.com", "DM"),
        Contact("Elena Fernandez", "elena.fernandez@email.com", "EF"),
        Contact("Felipe Gonzalez", "felipe.gonzalez@email.com", "FG"),
        Contact("Gabriela Diaz", "gabriela.diaz@email.com", "GD"),
        Contact("Hugo Sanchez", "hugo.sanchez@email.com", "HS"),
        Contact("Isabel Torres", "isabel.torres@email.com", "IT"),
        Contact("Jorge Ramirez", "jorge.ramirez@email.com", "JR"),
        Contact("Karla Vargas", "karla.vargas@email.com", "KV"),
        Contact("Luis Morales", "luis.morales@email.com", "LM"),
        Contact("Maria Herrera", "maria.herrera@email.com", "MH"),
        Contact("Nicolas Castillo", "nicolas.castillo@email.com", "NC"),
        Contact("Olivia Jimenez", "olivia.jimenez@email.com", "OJ"),
    )

    // Dashboard metrics
    val metrics = listOf(
        Metric("Usuarios Activos", "1,234", "+12%"),
        Metric("Cursos Completados", "456", "+5%"),
        Metric("Tasa de Aprobacion", "89%", "+3%"),
        Metric("Horas de Estudio", "2,890", "+18%"),
        Metric("Certificados Emitidos", "78", "+8%"),
    )

    // Navigation
    val navigationItems = listOf(
        NavItem("Inicio", "Home"),
        NavItem("Cursos", "School"),
        NavItem("Buscar", "Search"),
        NavItem("Perfil", "Person"),
        NavItem("Configuracion", "Settings"),
    )

    // Search
    val searchResults = listOf(
        SearchResult("Matematicas I", "Fundamentos de algebra y aritmetica", 4.5f),
        SearchResult("Matematicas II", "Calculo diferencial e integral", 4.2f),
        SearchResult("Estadistica", "Probabilidad y analisis de datos", 4.8f),
        SearchResult("Fisica Mecanica", "Cinematica y dinamica clasica", 4.3f),
        SearchResult("Quimica General", "Estructura atomica y enlaces", 4.1f),
        SearchResult("Biologia Celular", "Celula, organelos y metabolismo", 4.6f),
        SearchResult("Historia Universal", "Civilizaciones antiguas y modernas", 4.0f),
        SearchResult("Literatura", "Analisis literario y composicion", 3.9f),
        SearchResult("Programacion Basica", "Introduccion a la logica computacional", 4.7f),
        SearchResult("Ingles Intermedio", "Gramatica y conversacion nivel B1", 4.4f),
    )

    val recentSearches = listOf(
        "Matematicas",
        "Historia",
        "Fisica",
        "Programacion",
    )

    // Settings
    val settingsGroups = listOf(
        SettingsGroup(
            title = "Cuenta",
            items = listOf(
                SettingsItem("Editar perfil", SettingsItemType.NAVIGATION),
                SettingsItem("Cambiar contrasena", SettingsItemType.NAVIGATION),
            ),
        ),
        SettingsGroup(
            title = "Apariencia",
            items = listOf(
                SettingsItem("Tema oscuro", SettingsItemType.TOGGLE),
                SettingsItem("Tamano de texto", SettingsItemType.NAVIGATION, "Mediano"),
            ),
        ),
        SettingsGroup(
            title = "Notificaciones",
            items = listOf(
                SettingsItem("Notificaciones push", SettingsItemType.TOGGLE),
                SettingsItem("Notificaciones por email", SettingsItemType.TOGGLE),
                SettingsItem("Sonido", SettingsItemType.TOGGLE),
            ),
        ),
        SettingsGroup(
            title = "General",
            items = listOf(
                SettingsItem("Privacidad", SettingsItemType.NAVIGATION),
                SettingsItem("Idioma", SettingsItemType.NAVIGATION, "Espanol"),
                SettingsItem("Acerca de", SettingsItemType.NAVIGATION, "v1.0.0"),
            ),
        ),
    )
}
