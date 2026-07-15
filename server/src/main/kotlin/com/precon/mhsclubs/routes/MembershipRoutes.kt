package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Registers REST endpoints for the Membership entity.
 *
 * All membership operations require an authenticated user.
 * Students can join/leave clubs; teachers can manage memberships.
 *
 * GET    /api/memberships           – list memberships (authenticated)
 * GET    /api/memberships/{id}      – get membership by ID (authenticated)
 * POST   /api/memberships           – create a membership / join a club (authenticated)
 * PUT    /api/memberships/{id}      – update membership role/status (teacher only)
 * DELETE /api/memberships/{id}      – remove membership (teacher only or self-leave)
 */
fun Route.membershipRoutes() {
    route("api") {
        route(RouteNames.MEMBERSHIPS) {
            // All membership routes require authentication
            requireAuth(AuthenticationScheme.AnyAuthenticated) {
                get {
                    val identity = call.requireAuthenticatedUser()
                        ?: return@get
                    // TODO: fetch memberships for this user or all (admin)
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true, data = emptyList<Any>()))
                }

                get(RouteNames.MEMBERSHIPS + "/{id}") {
                    val id = call.parameters["id"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing membership id parameter")
                    )
                    val identity = call.requireAuthenticatedUser()
                        ?: return@get
                    // TODO: fetch from database
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true, data = null))
                }

                post {
                    val identity = call.requireAuthenticatedUser()
                        ?: return@post
                    // TODO: deserialize and persist
                    call.respond(HttpStatusCode.Created, RouteNames.ApiResponse(success = true))
                }

                put(RouteNames.MEMBERSHIPS + "/{id}") {
                    val id = call.parameters["id"] ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing membership id parameter")
                    )
                    val identity = call.requireAuthenticatedUser()
                        ?: return@put
                    // TODO: deserialize and persist
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true))
                }

                delete(RouteNames.MEMBERSHIPS + "/{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing membership id parameter")
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
