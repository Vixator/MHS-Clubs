package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.getAuthenticatedUser
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import com.precon.mhsclubs.auth.UserIdentity
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Registers REST endpoints for the Club entity.
 *
 * Club directory listing is public. Club creation/update/deletion requires teacher role.
 *
 * GET    /api/clubs           – list all clubs (public)
 * GET    /api/clubs/{id}      – get club by ID (public)
 * POST   /api/clubs           – create a new club (teacher only)
 * PUT    /api/clubs/{id}      – update a club (teacher only)
 * DELETE /api/clubs/{id}      – delete a club (teacher only)
 */
fun Route.clubRoutes() {
    route("api") {
        route(RouteNames.CLUBS) {
            // Public listing and detail
            get {
                // TODO: fetch from database
                call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true, data = emptyList<Any>()))
            }

            get(RouteNames.CLUBS + "/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    RouteNames.ApiResponse.error("Missing club id parameter")
                )
                // TODO: fetch from database
                call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true, data = null))
            }

            // Require teacher role for club management
            requireAuth(AuthenticationScheme.TeacherOnly) {
                post {
                    val identity = call.requireAuthenticatedUser()
                        ?: return@post
                    // TODO: deserialize and persist
                    call.respond(HttpStatusCode.Created, RouteNames.ApiResponse(success = true))
                }

                put(RouteNames.CLUBS + "/{id}") {
                    val id = call.parameters["id"] ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing club id parameter")
                    )
                    val identity = call.requireAuthenticatedUser()
                        ?: return@put
                    // TODO: deserialize and persist
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true))
                }

                delete(RouteNames.CLUBS + "/{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing club id parameter")
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
