package com.edugo.kmp.dynamicui.contract

interface CustomEventHandler {
    val eventId: String
    val requiredPermission: String?
    suspend fun execute(context: EventContext): EventResult
}
