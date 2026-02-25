package com.edugo.kmp.screens.dynamic.contracts

import com.edugo.kmp.dynamicui.contract.EventContext
import com.edugo.kmp.dynamicui.contract.ScreenContract
import com.edugo.kmp.dynamicui.contract.ScreenEvent

class MaterialDetailContract : ScreenContract {
    override val screenKey = "material-detail"
    override val resource = "materials"

    override fun endpointFor(event: ScreenEvent, context: EventContext): String? = when (event) {
        ScreenEvent.LOAD_DATA, ScreenEvent.REFRESH -> {
            val id = context.params["id"]
            if (id != null) "/api/v1/materials/$id" else null
        }
        else -> null
    }
}
