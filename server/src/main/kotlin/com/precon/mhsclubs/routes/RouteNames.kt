package com.precon.mhsclubs.routes

/**
 * Common route path constants and response wrapper for all entity endpoints.
 *
 * All routes follow REST conventions:
 * - GET /{entity}      – list all
 * - GET /{entity}/{id} – get one
 * - POST /{entity}     – create one
 * - PUT /{entity}/{id} – update one
 * - DELETE /{entity}/{id} – delete one
 */
object RouteNames {
    const val USERS = "users"
    const val CLUBS = "clubs"
    const val MEMBERSHIPS = "memberships"
    const val EVENTS = "events"
    const val ATTENDANCE = "attendance"
    const val RSVP = "rsvp"
    const val ANNOUNCEMENTS = "announcements"

    /**
     * Standard API response envelope for all endpoints.
     */
    data class ApiResponse<T>(
        val success: Boolean,
        val data: T? = null,
        val error: String? = null
    ) {
        companion object {
            fun <T> ok(data: T?) = ApiResponse(success = true, data = data)
            fun error(message: String): ApiResponse<Nothing> = ApiResponse(success = false, error = message)
        }
    }
}
