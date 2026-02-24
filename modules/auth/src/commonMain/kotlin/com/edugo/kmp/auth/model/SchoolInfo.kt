package com.edugo.kmp.auth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Información básica de una escuela.
 * Retornada como parte del LoginResponse para indicar
 * las escuelas a las que el usuario tiene acceso.
 */
@Serializable
public data class SchoolInfo(
    @SerialName("id")
    val id: String,

    @SerialName("name")
    val name: String
)
