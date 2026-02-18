package com.edugo.kmp.dynamicui.action.handlers

import com.edugo.kmp.dynamicui.action.ActionContext
import com.edugo.kmp.dynamicui.action.ActionHandler
import com.edugo.kmp.dynamicui.action.ActionResult
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class NavigateHandler : ActionHandler {

    override suspend fun execute(context: ActionContext): ActionResult {
        val screenKey = context.config["screenKey"]?.jsonPrimitive?.contentOrNull
            ?: return ActionResult.Error("Missing screenKey in navigate config")

        val params = context.config["params"]?.jsonObject
            ?.mapValues { it.value.jsonPrimitive.content }
            ?: emptyMap()

        return ActionResult.NavigateTo(
            screenKey = screenKey,
            params = params
        )
    }
}
