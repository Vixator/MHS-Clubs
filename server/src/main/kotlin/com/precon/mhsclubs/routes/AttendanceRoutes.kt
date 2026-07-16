package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import com.precon.mhsclubs.validation.Validation
import com.precon.mhsclubs.validation.validateAttendanceId
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json

/**
 * JSON parser for request bodies.
 */
private val json = Json { ignoreUnknownKeys = true }

/**
 * Data class for attendance creation/update requests.
 */
private data class AttendanceRequest(
    val eventId: String? = null,
    val userId: String? = null,
    val status: String? = null  // "present", "absent", "late"
)

/**
 * Registers REST endpoints for the Attendance entity.
 *
 * Attendance records are readable by any authenticated user.
 * Creating/updating attendance requires teacher role (for marking attendance).
 * Students can view their own attendance.
 *
 * GET    /api/attendance            list attendance records (authenticated)
 * GET    /api/attendance/{id}       get attendance record (authenticated)
 * POST   /api/attendance            mark attendance (teacher only)
 * PUT    /api/attendance/{id}       update attendance record (teacher only)
 * DELETE /api/attendance/{id}       delete attendance record (teacher only)
 */
fun Route.attendanceRoutes() {
    route("api") {
        route(RouteNames.ATTENDANCE) {
            // Attendance listing requires authentication
            requireAuth(AuthenticationScheme.AnyAuthenticated) {
                get {
                    try {
                        val identity = call.requireAuthenticatedUser()
                            ?: return@get
                        // TODO: fetch from database
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true, data = emptyList<Any>())
                        )
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to fetch attendance: ${e.message}")
                    }
                }

                get("/{id}") {
                    try {
                        val id = validateAttendanceId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@get
                        // TODO: fetch from database
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true, data = null)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid attendance ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to fetch attendance record: ${e.message}")
                    }
                }
            }

            // Attendance management requires teacher role
            requireAuth(AuthenticationScheme.TeacherOnly) {
                post {
                    try {
                        val identity = call.requireAuthenticatedUser()
                            ?: return@post
                        
                        // Parse and validate request body
                        val requestText = call.receiveText()
                        if (requestText.isBlank()) {
                            return@post call.respondBadRequest("Request body is required")
                        }
                        
                        val request = try {
                            json.decodeFromString<AttendanceRequest>(requestText)
                        } catch (e: Exception) {
                            return@post call.respondBadRequest("Invalid request body: ${e.message}")
                        }
                        
                        // Validate required fields
                        if (request.eventId.isNullOrBlank()) {
                            return@post call.respondBadRequest("Event ID is required")
                        }
                        
                        if (request.userId.isNullOrBlank()) {
                            return@post call.respondBadRequest("User ID is required")
                        }
                        
                        if (request.status.isNullOrBlank()) {
                            return@post call.respondBadRequest("Status is required")
                        }
                        
                        // Validate status
                        val validStatuses = setOf("present", "absent", "late")
                        if (request.status.lowercase() !in validStatuses) {
                            return@post call.respondBadRequest(
                                "Status must be one of: ${validStatuses.joinToString(", ")}"
                            )
                        }
                        
                        // TODO: deserialize and persist with validated data
                        call.respond(
                            HttpStatusCode.Created,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to create attendance: ${e.message}")
                    }
                }

                put("/{id}") {
                    try {
                        val id = validateAttendanceId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@put
                        
                        // Parse and validate request body
                        val requestText = call.receiveText()
                        if (requestText.isBlank()) {
                            return@put call.respondBadRequest("Request body is required")
                        }
                        
                        val request = try {
                            json.decodeFromString<AttendanceRequest>(requestText)
                        } catch (e: Exception) {
                            return@put call.respondBadRequest("Invalid request body: ${e.message}")
                        }
                        
                        // At least one field must be provided for update
                        if (request.eventId == null && request.userId == null && request.status == null) {
                            return@put call.respondBadRequest("At least one field must be provided for update")
                        }
                        
                        // Validate status if provided
                        if (request.status != null && request.status.isNotBlank()) {
                            val validStatuses = setOf("present", "absent", "late")
                            if (request.status.lowercase() !in validStatuses) {
                                return@put call.respondBadRequest(
                                    "Status must be one of: ${validStatuses.joinToString(", ")}"
                                )
                            }
                        }
                        
                        // TODO: deserialize and persist with validated data
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid attendance ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to update attendance: ${e.message}")
                    }
                }

                delete("/{id}") {
                    try {
                        val id = validateAttendanceId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@delete
                        
                        // TODO: delete from database
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid attendance ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to delete attendance: ${e.message}")
                    }
                }
            }
        }
    }
}
