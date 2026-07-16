package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import com.precon.mhsclubs.services.GoogleSheetsService
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Registers endpoints for data synchronization.
 *
 * POST /api/sync/clubs  triggers a sync of club data from Google Sheets (admin only)
 * GET  /api/sync/clubs/status  returns the current sync status
 */
fun Route.syncRoutes(sheetsService: GoogleSheetsService) {
    route("api") {
        route("sync") {
            // Sync clubs from Google Sheets (admin only)
            requireAuth(AuthenticationScheme.AdminOnly) {
                post("clubs") {
                    val identity = call.requireAuthenticatedUser()
                        ?: return@post

                    if (!sheetsService.isConfigured()) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            RouteNames.ApiResponse.error(
                                "Google Sheets integration not configured"
                            )
                        )
                        return@post
                    }

                    try {
                        val result = sheetsService.syncClubs()
                        if (result.success) {
                            call.respond(
                                HttpStatusCode.OK,
                                RouteNames.ApiResponse.ok(result)
                            )
                        } else {
                            call.respond(
                                HttpStatusCode.InternalServerError,
                                RouteNames.ApiResponse.error(result.error ?: "Sync failed")
                            )
                        }
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            RouteNames.ApiResponse.error(e.message ?: "Sync failed")
                        )
                    }
                }
            }

            // Get sync status (public, but returns limited info)
            get("clubs/status") {
                val status = mapOf(
                    "configured" to sheetsService.isConfigured(),
                    "sheetId" to if (sheetsService.isConfigured()) "configured" else null
                )
                call.respond(HttpStatusCode.OK, RouteNames.ApiResponse.ok(status))
            }
        }
    }
}
