package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Registers REST endpoints for the RSVP entity.
 *
 * RSVP listing is public. RSVP creation/update/deletion requires authentication.
 *
 * GET    /api/rsvp           – list all RSVPs (public)
 * GET    /api/rsvp/{id}      – get RSVP by ID (public)
 * POST   /api/rsvp           – create a new RSVP (authenticated)
 * PUT    /api/rsvp/{id}      – update an RSVP (authenticated)
 * DELETE /api/rsvp/{id}      – delete an RSVP (authenticated)
 */
fun Route.rsvpRoutes() {
    route("api") {
        route(RouteNames.RSVP) {
            // RSVP listing is public
            get {
                // TODO: fetch from database
                call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true, data = emptyList<Any>()))
            }

            get(RouteNames.RSVP + "/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    RouteNames.ApiResponse.error("Missing RSVP id parameter")
                )
                // TODO: fetch from database
                call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true, data = null))
            }

            // RSVP management requires authentication
            requireAuth(AuthenticationScheme.AnyAuthenticated) {
                post {
                    val identity = call.requireAuthenticatedUser()
                        ?: return@post
                    // TODO: deserialize and persist
                    call.respond(HttpStatusCode.Created, RouteNames.ApiResponse(success = true))
                }

                put(RouteNames.RSVP + "/{id}") {
                    val id = call.parameters["id"] ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing RSVP id parameter")
                    )
                    val identity = call.requireAuthenticatedUser()
                        ?: return@put
                    // TODO: deserialize and persist
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true))
                }

                delete(RouteNames.RSVP + "/{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing RSVP id parameter")
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
