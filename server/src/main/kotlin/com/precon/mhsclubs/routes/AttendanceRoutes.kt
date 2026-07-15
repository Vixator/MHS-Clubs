package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

/**
 * Registers REST endpoints for the Attendance entity.
 *
 * Attendance records are readable by any authenticated user.
 * Creating/updating attendance requires teacher role (for marking attendance).
 * Students can view their own attendance.
 *
 * GET    /api/attendance           – list attendance records (authenticated)
 * GET    /api/attendance/{id}      – get attendance record (authenticated)
 * POST   /api/attendance           – mark attendance (teacher only)
 * PUT    /api/attendance/{id}      – update attendance record (teacher only)
 * DELETE /api/attendance/{id}      – delete attendance record (teacher only)
 */
fun Route.attendanceRoutes() {
    route("api") {
        route(RouteNames.ATTENDANCE) {
            // Attendance listing requires authentication
            requireAuth(AuthenticationScheme.AnyAuthenticated) {
                get {
                    val identity = call.requireAuthenticatedUser()
                        ?: return@get
                    // TODO: fetch from database
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true, data = emptyList<Any>()))
                }

                get(RouteNames.ATTENDANCE + "/{id}") {
                    val id = call.parameters["id"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing attendance id parameter")
                    )
                    val identity = call.requireAuthenticatedUser()
                        ?: return@get
                    // TODO: fetch from database
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true, data = null))
                }
            }

            // Attendance management requires teacher role
            requireAuth(AuthenticationScheme.TeacherOnly) {
                post {
                    val identity = call.requireAuthenticatedUser()
                        ?: return@post
                    // TODO: deserialize and persist
                    call.respond(HttpStatusCode.Created, RouteNames.ApiResponse(success = true))
                }

                put(RouteNames.ATTENDANCE + "/{id}") {
                    val id = call.parameters["id"] ?: return@put call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing attendance id parameter")
                    )
                    val identity = call.requireAuthenticatedUser()
                        ?: return@put
                    // TODO: deserialize and persist
                    call.respond(HttpStatusCode.OK, RouteNames.ApiResponse(success = true))
                }

                delete(RouteNames.ATTENDANCE + "/{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        RouteNames.ApiResponse.error("Missing attendance id parameter")
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
