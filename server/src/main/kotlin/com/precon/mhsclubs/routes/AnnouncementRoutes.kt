package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Registers REST endpoints for the Announcement entity.
 *
 * Announcement listing is public. Creation/update/deletion requires teacher role.
 *
 * GET    /api/announcements           – list all announcements (public)
 * GET    /api/announcements/{id}      – get announcement by ID (public)
 * POST   /api/announcements           – create a new announcement (teacher only)
 * PUT    /api/announcements/{id}      – update an announcement (teacher only)
 * DELETE /api/announcements/{id}      – delete an announcement (teacher only)
 */
fun Route.announcementRoutes() {
    route("api") {
        route(RouteNames.ANNOUNCEMENTS) {
            // Public listing and detail
            get {
                // TODO: fetch from database
                call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true, data = emptyList<Any>()))
            }

            get(RouteNames.ANNOUNCEMENTS + "/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    RouteNames.ApiResponse.error("Missing announcement id parameter")
                )
                // TODO: fetch from database
                call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true, data = null))
            }

            // Announcement management requires teacher role
            requireAuth(AuthenticationScheme.TeacherOnly) {
                post {
                    val identity = call.requireAuthenticatedUser()
                        ?: return@post
                    // TODO: deserialize and persist
                    call.respond(HttpStatusCode.Created, RouteNames.ApiResponse(success = true))
                }

                put(RouteNames.ANNOUNCEMENTS + "/{id}") {
                    val id = call.parameters["id"] ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing announcement id parameter")
                    )
                    val identity = call.requireAuthenticatedUser()
                        ?: return@put
                    // TODO: deserialize and persist
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true))
                }

                delete(RouteNames.ANNOUNCEMENTS + "/{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing announcement id parameter")
                    )
                    val identity = call.requireAuthenticatedUser()
                        ?: return@delete
                    // TODO: delete from database
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true))
                }
            }
        }
    }
}
