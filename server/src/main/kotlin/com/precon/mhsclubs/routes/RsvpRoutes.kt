package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import com.precon.mhsclubs.validation.Validation
import com.precon.mhsclubs.validation.validateRsvpId
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
 * Data class for RSVP creation/update requests.
 */
private data class RsvpRequest(
    val eventId: String? = null,
    val userId: String? = null,
    val status: String? = null  // "going", "maybe", "not_going"
)

/**
 * Registers REST endpoints for the RSVP entity.
 *
 * RSVP listing is public. RSVP creation/update/deletion requires authentication.
 *
 * GET    /api/rsvp            list all RSVPs (public)
 * GET    /api/rsvp/{id}       get RSVP by ID (public)
 * POST   /api/rsvp            create a new RSVP (authenticated)
 * PUT    /api/rsvp/{id}       update an RSVP (authenticated)
 * DELETE /api/rsvp/{id}       delete an RSVP (authenticated)
 */
fun Route.rsvpRoutes() {
    route("api") {
        route(RouteNames.RSVP) {
            // RSVP listing is public
            get {
                try {
                    // TODO: fetch from database
                    call.respond(
                        HttpStatusCode.OK,
                        RouteNames.ApiResponse(success = true, data = emptyList<Any>())
                    )
                } catch (e: Exception) {
                    call.respondBadRequest("Failed to fetch RSVPs: ${e.message}")
                }
            }

            get("/{id}") {
                try {
                    val id = validateRsvpId()
                    // TODO: fetch from database
                    call.respond(
                        HttpStatusCode.OK,
                        RouteNames.ApiResponse(success = true, data = null)
                    )
                } catch (e: Validation.BadRequestException) {
                    call.respondBadRequest(e.message ?: "Invalid RSVP ID")
                } catch (e: Exception) {
                    call.respondBadRequest("Failed to fetch RSVP: ${e.message}")
                }
            }

            // RSVP management requires authentication
            requireAuth(AuthenticationScheme.AnyAuthenticated) {
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
                            json.decodeFromString<RsvpRequest>(requestText)
                        } catch (e: Exception) {
                            return@post call.respondBadRequest("Invalid request body: ${e.message}")
                        }
                        
                        // Validate required fields
                        if (request.eventId.isNullOrBlank()) {
                            return@post call.respondBadRequest("Event ID is required")
                        }
                        
                        // Default userId to current user if not provided
                        if (request.userId.isNullOrBlank()) {
                            // Will use identity.uid when persisting
                        }
                        
                        if (request.status.isNullOrBlank()) {
                            return@post call.respondBadRequest("Status is required")
                        }
                        
                        // Validate status
                        val validStatuses = setOf("going", "maybe", "not_going")
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
                        call.respondBadRequest("Failed to create RSVP: ${e.message}")
                    }
                }

                put("/{id}") {
                    try {
                        val id = validateRsvpId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@put
                        
                        // Parse and validate request body
                        val requestText = call.receiveText()
                        if (requestText.isBlank()) {
                            return@put call.respondBadRequest("Request body is required")
                        }
                        
                        val request = try {
                            json.decodeFromString<RsvpRequest>(requestText)
                        } catch (e: Exception) {
                            return@put call.respondBadRequest("Invalid request body: ${e.message}")
                        }
                        
                        // At least one field must be provided for update
                        if (request.eventId == null && request.userId == null && request.status == null) {
                            return@put call.respondBadRequest("At least one field must be provided for update")
                        }
                        
                        // Validate status if provided
                        if (request.status != null && request.status.isNotBlank()) {
                            val validStatuses = setOf("going", "maybe", "not_going")
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
                        call.respondBadRequest(e.message ?: "Invalid RSVP ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to update RSVP: ${e.message}")
                    }
                }

                delete("/{id}") {
                    try {
                        val id = validateRsvpId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@delete
                        
                        // TODO: delete from database
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid RSVP ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to delete RSVP: ${e.message}")
                    }
                }
            }
        }
    }
}
