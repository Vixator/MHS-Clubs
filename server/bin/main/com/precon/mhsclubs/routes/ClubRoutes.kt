package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.getAuthenticatedUser
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import com.precon.mhsclubs.auth.UserIdentity
import com.precon.mhsclubs.validation.Validation
import com.precon.mhsclubs.validation.validateClubId
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * JSON parser for request bodies.
 */
private val json = Json { ignoreUnknownKeys = true }

/**
 * Data class for club creation/update requests.
 */
private data class ClubRequest(
    val name: String? = null,
    val description: String? = null,
    val logoUrl: String? = null,
    val category: String? = null,
    val meetingDay: String? = null,
    val meetingTime: String? = null,
    val meetingLocation: String? = null,
    val code: String? = null,
    val isActive: Boolean? = true
)

/**
 * Registers REST endpoints for the Club entity.
 *
 * Club directory listing is public. Club creation/update/deletion requires teacher role.
 *
 * GET    /api/clubs            list all clubs (public)
 * GET    /api/clubs/{id}       get club by ID (public)
 * POST   /api/clubs            create a new club (teacher only)
 * PUT    /api/clubs/{id}       update a club (teacher only)
 * DELETE /api/clubs/{id}       delete a club (teacher only)
 */
fun Route.clubRoutes() {
    route("api") {
        route(RouteNames.CLUBS) {
            // Public listing and detail
            get {
                try {
                    // TODO: fetch from database with pagination
                    // For now, return empty list
                    call.respond(
                        HttpStatusCode.OK,
                        RouteNames.ApiResponse(success = true, data = emptyList<Any>())
                    )
                } catch (e: Exception) {
                    call.respondBadRequest("Failed to fetch clubs: ${e.message}")
                }
            }

            get("/{id}") {
                try {
                    val id = validateClubId()
                    // TODO: fetch from database by ID
                    call.respond(
                        HttpStatusCode.OK,
                        RouteNames.ApiResponse(success = true, data = null)
                    )
                } catch (e: Validation.BadRequestException) {
                    call.respondBadRequest(e.message ?: "Invalid club ID")
                } catch (e: Exception) {
                    call.respondBadRequest("Failed to fetch club: ${e.message}")
                }
            }

            // Require teacher role for club management
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
                            json.decodeFromString<ClubRequest>(requestText)
                        } catch (e: Exception) {
                            return@post call.respondBadRequest("Invalid request body: ${e.message}")
                        }
                        
                        // Validate required fields
                        if (request.name.isNullOrBlank()) {
                            return@post call.respondBadRequest("Club name is required")
                        }
                        
                        if (request.code.isNullOrBlank()) {
                            return@post call.respondBadRequest("Club code is required")
                        }
                        
                        // Validate club code format (3-10 uppercase alphanumeric)
                        val clubCode = request.code.uppercase()
                        if (!Regex("^[A-Z0-9]{3,10}$").matches(clubCode)) {
                            return@post call.respondBadRequest(
                                "Club code must be 3-10 alphanumeric uppercase characters"
                            )
                        }
                        
                        // Validate name length
                        if (request.name.length > 100) {
                            return@post call.respondBadRequest("Club name must be 100 characters or less")
                        }
                        
                        // Validate description length
                        if (request.description != null && request.description.length > 500) {
                            return@post call.respondBadRequest("Description must be 500 characters or less")
                        }
                        
                        // TODO: deserialize and persist with validated data
                        call.respond(
                            HttpStatusCode.Created,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to create club: ${e.message}")
                    }
                }

                put("/{id}") {
                    try {
                        val id = validateClubId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@put
                        
                        // Parse and validate request body
                        val requestText = call.receiveText()
                        if (requestText.isBlank()) {
                            return@put call.respondBadRequest("Request body is required")
                        }
                        
                        val request = try {
                            json.decodeFromString<ClubRequest>(requestText)
                        } catch (e: Exception) {
                            return@put call.respondBadRequest("Invalid request body: ${e.message}")
                        }
                        
                        // At least one field must be provided for update
                        if (request.name == null && request.description == null && 
                            request.code == null && request.category == null &&
                            request.meetingDay == null && request.meetingTime == null &&
                            request.meetingLocation == null && request.isActive == null) {
                            return@put call.respondBadRequest("At least one field must be provided for update")
                        }
                        
                        // Validate fields if provided
                        if (request.name != null && request.name.length > 100) {
                            return@put call.respondBadRequest("Club name must be 100 characters or less")
                        }
                        
                        if (request.code != null) {
                            val clubCode = request.code.uppercase()
                            if (!Regex("^[A-Z0-9]{3,10}$").matches(clubCode)) {
                                return@put call.respondBadRequest(
                                    "Club code must be 3-10 alphanumeric uppercase characters"
                                )
                            }
                        }
                        
                        // TODO: deserialize and persist with validated data
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid club ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to update club: ${e.message}")
                    }
                }

                delete("/{id}") {
                    try {
                        val id = validateClubId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@delete
                        
                        // TODO: delete from database
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid club ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to delete club: ${e.message}")
                    }
                }
            }
        }
    }
}
