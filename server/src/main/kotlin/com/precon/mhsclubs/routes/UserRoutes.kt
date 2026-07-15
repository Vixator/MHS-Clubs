package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.getAuthenticatedUser
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import com.precon.mhsclubs.auth.UserIdentity
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import com.precon.mhsclubs.model.User

/**
 * Registers REST endpoints for the User entity.
 *
 * All endpoints under this route require an authenticated user.
 * Users can only read/update their own profile unless they have admin claims.
 *
 * GET    /api/users           – list all users (admin only)
 * GET    /api/users/{id}      – get user by ID
 * POST   /api/users           – create a new user (Firebase signup callback)
 * PUT    /api/users/{id}      – update a user
 * DELETE /api/users/{id}      – delete a user (admin only)
 */
fun Route.userRoutes() {
    val api = route("api") {
        route(RouteNames.USERS) {
            // Require teacher or admin role for user management
            requireAuth(AuthenticationScheme.TeacherOnly) {
                get {
                    val admin = call.getAuthenticatedUser()
                    // TODO: fetch from database, filter by admin scope
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true, data = emptyList<Any>()))
                }

                get(RouteNames.USERS + "/{id}") {
                    val id = call.parameters["id"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing user id parameter")
                    )
                    val identity = call.requireAuthenticatedUser()
                        ?: return@get
                    // TODO: fetch from database, check ownership or admin
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true, data = null))
                }

                post {
                    val identity = call.requireAuthenticatedUser()
                        ?: return@post
                    // TODO: deserialize and persist
                    call.respond(HttpStatusCode.Created, RouteNames.ApiResponse(success = true))
                }

                put(RouteNames.USERS + "/{id}") {
                    val id = call.parameters["id"] ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing user id parameter")
                    )
                    val identity = call.requireAuthenticatedUser()
                        ?: return@put
                    // TODO: deserialize and persist, check ownership
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true))
                }

                delete(RouteNames.USERS + "/{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing user id parameter")
                    )
                    val identity = call.requireAuthenticatedUser()
                        ?: return@delete
                    // TODO: delete from database, check admin
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true))
                }
            }
        }
    }
}
