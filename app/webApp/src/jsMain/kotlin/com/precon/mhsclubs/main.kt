package com.precon.mhsclubs

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.precon.mhsclubs.data.JsClubContentApi

private const val LOCAL_API_BASE_URL = "http://localhost:8080"

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        App(clubContentApi = JsClubContentApi(LOCAL_API_BASE_URL))
    }
}
