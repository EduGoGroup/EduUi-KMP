package com.edugo.kmp.dynamicui.action

import com.edugo.kmp.dynamicui.action.handlers.ApiCallHandler
import com.edugo.kmp.dynamicui.action.handlers.ConfirmHandler
import com.edugo.kmp.dynamicui.action.handlers.NavigateHandler
import com.edugo.kmp.dynamicui.action.handlers.RefreshHandler
import com.edugo.kmp.dynamicui.action.handlers.SubmitFormHandler
import com.edugo.kmp.dynamicui.model.ActionType

class ActionRegistry(
    private val navigateHandler: NavigateHandler,
    private val apiCallHandler: ApiCallHandler,
    private val refreshHandler: RefreshHandler,
    private val submitFormHandler: SubmitFormHandler,
    private val confirmHandler: ConfirmHandler
) {
    fun resolve(actionType: ActionType): ActionHandler = when (actionType) {
        ActionType.NAVIGATE -> navigateHandler
        ActionType.NAVIGATE_BACK -> navigateHandler
        ActionType.API_CALL -> apiCallHandler
        ActionType.SUBMIT_FORM -> submitFormHandler
        ActionType.REFRESH -> refreshHandler
        ActionType.CONFIRM -> confirmHandler
        ActionType.LOGOUT -> navigateHandler
    }
}
