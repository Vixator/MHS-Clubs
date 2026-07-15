package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Registers REST endpoints for the Event entity.
 *
 * Event listing is public. RSVP and event management require authentication.
 *
 * GET    /api/events           – list all events (public)
 * GET    /api/events/{id}      – get event by ID (public)
 * POST   /api/events           – create a new event (teacher only)
 * PUT    /api/events/{id}      – update an event (teacher only)
 * DELETE /api/events/{id}      – delete an event (teacher only)
 */
fun Route.eventRoutes() {
    route("api") {
        route(RouteNames.EVENTS) {
            // Public listing and detail
            get {
                // TODO: fetch from database
                call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true, data = emptyList<Any>()))
            }

            get(RouteNames.EVENTS + "/{id}") {
                val id = call.parameters["id"] ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    RouteNames.ApiResponse.error("Missing event id parameter")
                )
                // TODO: fetch from database
                call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true, data = null))
            }

            // Event creation and management require teacher role
            requireAuth(AuthenticationScheme.TeacherOnly) {
                post {
                    val identity = call.requireAuthenticatedUser()
                        ?: return@post
                    // TODO: deserialize and persist
                    call.respond(HttpStatusCode.Created, RouteNames.ApiResponse(success = true))
                }

                put(RouteNames.EVENTS + "/{id}") {
                    val id = call.parameters["id"] ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing event id parameter")
                    )
                    val identity = call.requireAuthenticatedUser()
                        ?: return@put
                    // TODO: deserialize and persist
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true))
                }

                delete(RouteNames.EVENTS + "/{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing event id parameter")
                    )
                    val identity = call.requireAuthenticatedUser()
                        ?: return@delete
                    // TODO: delete from database
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true))
                }
            }

            // RSVP endpoint – requires any authenticated user
            requireAuth(AuthenticationScheme.AnyAuthenticated) {
                post("rsvp") {
                    val identity = call.requireAuthenticatedUser()
                        ?: return@post
                    val eventId = call.parameters["eventId"]
                        ?: return@post call.respond(
                            HttpStatusCode.BadRequest,
                            RouteNames.ApiResponse.error("Missing eventId parameter")
                        )
                    // TODO: deserialize and persist RSVP
                    call.respond(HttpStatusCode.Created, RouteNames.ApiResponse(success = true))
                }
            }
        }
    }
}
