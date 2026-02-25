package com.edugo.kmp.dynamicui.contract

data class RequestConfig(
    val url: String,
    val method: String = "GET",
    val params: Map<String, String> = emptyMap()
)
