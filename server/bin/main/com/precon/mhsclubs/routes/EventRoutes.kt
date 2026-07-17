package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import com.precon.mhsclubs.validation.Validation
import com.precon.mhsclubs.validation.validateEventId
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
 * Data class for event creation/update requests.
 */
private data class EventRequest(
    val clubId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val location: String? = null,
    val startTime: String? = null,
    val endTime: String? = null,
    val googleCalendarSynced: Boolean? = null
)

/**
 * Registers REST endpoints for the Event entity.
 *
 * Event listing is public. RSVP and event management require authentication.
 *
 * GET    /api/events            list all events (public)
 * GET    /api/events/{id}       get event by ID (public)
 * POST   /api/events            create a new event (teacher only)
 * PUT    /api/events/{id}       update an event (teacher only)
 * DELETE /api/events/{id}       delete an event (teacher only)
 */
fun Route.eventRoutes() {
    route("api") {
        route(RouteNames.EVENTS) {
            // Public listing and detail
            get {
                try {
                    // TODO: fetch from database
                    call.respond(
                        HttpStatusCode.OK,
                        RouteNames.ApiResponse(success = true, data = emptyList<Any>())
                    )
                } catch (e: Exception) {
                    call.respondBadRequest("Failed to fetch events: ${e.message}")
                }
            }

            get("/{id}") {
                try {
                    val id = validateEventId()
                    // TODO: fetch from database
                    call.respond(
                        HttpStatusCode.OK,
                        RouteNames.ApiResponse(success = true, data = null)
                    )
                } catch (e: Validation.BadRequestException) {
                    call.respondBadRequest(e.message ?: "Invalid event ID")
                } catch (e: Exception) {
                    call.respondBadRequest("Failed to fetch event: ${e.message}")
                }
            }

            // Event creation and management require teacher role
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
                            json.decodeFromString<EventRequest>(requestText)
                        } catch (e: Exception) {
                            return@post call.respondBadRequest("Invalid request body: ${e.message}")
                        }
                        
                        // Validate required fields
                        if (request.clubId.isNullOrBlank()) {
                            return@post call.respondBadRequest("Club ID is required")
                        }
                        
                        if (request.title.isNullOrBlank()) {
                            return@post call.respondBadRequest("Event title is required")
                        }
                        
                        if (request.title.length > 100) {
                            return@post call.respondBadRequest("Event title must be 100 characters or less")
                        }
                        
                        if (request.startTime.isNullOrBlank()) {
                            return@post call.respondBadRequest("Start time is required")
                        }
                        
                        // Validate ISO-8601 date-time format
                        val isoDateRegex = Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")
                        if (!isoDateRegex.matches(request.startTime)) {
                            return@post call.respondBadRequest(
                                "Start time must be in ISO-8601 format (YYYY-MM-DDTHH:MM:SSZ)"
                            )
                        }
                        
                        if (request.endTime != null && request.endTime.isNotBlank()) {
                            if (!isoDateRegex.matches(request.endTime)) {
                                return@post call.respondBadRequest(
                                    "End time must be in ISO-8601 format (YYYY-MM-DDTHH:MM:SSZ)"
                                )
                            }
                        }
                        
                        if (request.location != null && request.location.length > 100) {
                            return@post call.respondBadRequest("Location must be 100 characters or less")
                        }
                        
                        if (request.description != null && request.description.length > 500) {
                            return@post call.respondBadRequest("Description must be 500 characters or less")
                        }
                        
                        // TODO: deserialize and persist with validated data
                        call.respond(
                            HttpStatusCode.Created,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to create event: ${e.message}")
                    }
                }

                put("/{id}") {
                    try {
                        val id = validateEventId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@put
                        
                        // Parse and validate request body
                        val requestText = call.receiveText()
                        if (requestText.isBlank()) {
                            return@put call.respondBadRequest("Request body is required")
                        }
                        
                        val request = try {
                            json.decodeFromString<EventRequest>(requestText)
                        } catch (e: Exception) {
                            return@put call.respondBadRequest("Invalid request body: ${e.message}")
                        }
                        
                        // At least one field must be provided for update
                        if (request.clubId == null && request.title == null && 
                            request.description == null && request.location == null &&
                            request.startTime == null && request.endTime == null &&
                            request.googleCalendarSynced == null) {
                            return@put call.respondBadRequest("At least one field must be provided for update")
                        }
                        
                        // Validate fields if provided
                        if (request.title != null) {
                            if (request.title.isBlank()) {
                                return@put call.respondBadRequest("Event title cannot be empty")
                            }
                            if (request.title.length > 100) {
                                return@put call.respondBadRequest("Event title must be 100 characters or less")
                            }
                        }
                        
                        if (request.startTime != null && request.startTime.isNotBlank()) {
                            val isoDateRegex = Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")
                            if (!isoDateRegex.matches(request.startTime)) {
                                return@put call.respondBadRequest(
                                    "Start time must be in ISO-8601 format (YYYY-MM-DDTHH:MM:SSZ)"
                                )
                            }
                        }
                        
                        if (request.endTime != null && request.endTime.isNotBlank()) {
                            val isoDateRegex = Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z")
                            if (!isoDateRegex.matches(request.endTime)) {
                                return@put call.respondBadRequest(
                                    "End time must be in ISO-8601 format (YYYY-MM-DDTHH:MM:SSZ)"
                                )
                            }
                        }
                        
                        // TODO: deserialize and persist with validated data
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid event ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to update event: ${e.message}")
                    }
                }

                delete("/{id}") {
                    try {
                        val id = validateEventId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@delete
                        
                        // TODO: delete from database
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid event ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to delete event: ${e.message}")
                    }
                }
            }

            // RSVP endpoint  requires any authenticated user
            requireAuth(AuthenticationScheme.AnyAuthenticated) {
                post("rsvp") {
                    try {
                        val identity = call.requireAuthenticatedUser()
                            ?: return@post
                        val eventId = call.parameters["eventId"]
                            ?: return@post call.respondBadRequest("Event ID is required")
                        
                        // TODO: deserialize and persist RSVP
                        call.respond(
                            HttpStatusCode.Created,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to create RSVP: ${e.message}")
                    }
                }
            }
        }
    }
}
