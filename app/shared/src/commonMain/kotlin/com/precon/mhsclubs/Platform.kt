package com.precon.mhsclubs

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform