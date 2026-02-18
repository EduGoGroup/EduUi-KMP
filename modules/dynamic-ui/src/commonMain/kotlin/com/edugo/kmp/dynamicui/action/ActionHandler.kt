package com.edugo.kmp.dynamicui.action

interface ActionHandler {
    suspend fun execute(context: ActionContext): ActionResult
}
