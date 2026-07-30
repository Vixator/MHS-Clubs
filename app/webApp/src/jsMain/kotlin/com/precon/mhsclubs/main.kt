package com.precon.mhsclubs

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.precon.mhsclubs.data.JsClubContentApi

private val apiBaseUrl: String = js("window.mhsClubsApiBaseUrl")

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        App(clubContentApi = JsClubContentApi(apiBaseUrl))
    }
}
