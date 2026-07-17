package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import com.precon.mhsclubs.validation.Validation
import com.precon.mhsclubs.validation.validateAnnouncementId
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
 * Data class for announcement creation/update requests.
 */
private data class AnnouncementRequest(
    val clubId: String? = null,
    val title: String? = null,
    val content: String? = null,
    val authorId: String? = null,
    val isActive: Boolean? = true
)

/**
 * Registers REST endpoints for the Announcement entity.
 *
 * Announcement listing is public. Creation/update/deletion requires teacher role.
 *
 * GET    /api/announcements            list all announcements (public)
 * GET    /api/announcements/{id}       get announcement by ID (public)
 * POST   /api/announcements            create a new announcement (teacher only)
 * PUT    /api/announcements/{id}       update an announcement (teacher only)
 * DELETE /api/announcements/{id}       delete an announcement (teacher only)
 */
fun Route.announcementRoutes() {
    route("api") {
        route(RouteNames.ANNOUNCEMENTS) {
            // Public listing and detail
            get {
                try {
                    // TODO: fetch from database
                    call.respond(
                        HttpStatusCode.OK,
                        RouteNames.ApiResponse(success = true, data = emptyList<Any>())
                    )
                } catch (e: Exception) {
                    call.respondBadRequest("Failed to fetch announcements: ${e.message}")
                }
            }

            get("/{id}") {
                try {
                    val id = validateAnnouncementId()
                    // TODO: fetch from database
                    call.respond(
                        HttpStatusCode.OK,
                        RouteNames.ApiResponse(success = true, data = null)
                    )
                } catch (e: Validation.BadRequestException) {
                    call.respondBadRequest(e.message ?: "Invalid announcement ID")
                } catch (e: Exception) {
                    call.respondBadRequest("Failed to fetch announcement: ${e.message}")
                }
            }

            // Announcement management requires teacher role
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
                            json.decodeFromString<AnnouncementRequest>(requestText)
                        } catch (e: Exception) {
                            return@post call.respondBadRequest("Invalid request body: ${e.message}")
                        }
                        
                        // Validate required fields
                        if (request.clubId.isNullOrBlank()) {
                            return@post call.respondBadRequest("Club ID is required")
                        }
                        
                        if (request.title.isNullOrBlank()) {
                            return@post call.respondBadRequest("Announcement title is required")
                        }
                        
                        if (request.title.length > 100) {
                            return@post call.respondBadRequest("Announcement title must be 100 characters or less")
                        }
                        
                        if (request.content.isNullOrBlank()) {
                            return@post call.respondBadRequest("Announcement content is required")
                        }
                        
                        if (request.content.length > 2000) {
                            return@post call.respondBadRequest("Announcement content must be 2000 characters or less")
                        }
                        
                        // Default authorId to current user if not provided
                        if (request.authorId.isNullOrBlank()) {
                            // Will use identity.uid when persisting
                        }
                        
                        // TODO: deserialize and persist with validated data
                        call.respond(
                            HttpStatusCode.Created,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to create announcement: ${e.message}")
                    }
                }

                put("/{id}") {
                    try {
                        val id = validateAnnouncementId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@put
                        
                        // Parse and validate request body
                        val requestText = call.receiveText()
                        if (requestText.isBlank()) {
                            return@put call.respondBadRequest("Request body is required")
                        }
                        
                        val request = try {
                            json.decodeFromString<AnnouncementRequest>(requestText)
                        } catch (e: Exception) {
                            return@put call.respondBadRequest("Invalid request body: ${e.message}")
                        }
                        
                        // At least one field must be provided for update
                        if (request.clubId == null && request.title == null && 
                            request.content == null && request.authorId == null &&
                            request.isActive == null) {
                            return@put call.respondBadRequest("At least one field must be provided for update")
                        }
                        
                        // Validate fields if provided
                        if (request.title != null) {
                            if (request.title.isBlank()) {
                                return@put call.respondBadRequest("Announcement title cannot be empty")
                            }
                            if (request.title.length > 100) {
                                return@put call.respondBadRequest("Announcement title must be 100 characters or less")
                            }
                        }
                        
                        if (request.content != null) {
                            if (request.content.isBlank()) {
                                return@put call.respondBadRequest("Announcement content cannot be empty")
                            }
                            if (request.content.length > 2000) {
                                return@put call.respondBadRequest("Announcement content must be 2000 characters or less")
                            }
                        }
                        
                        // TODO: deserialize and persist with validated data
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid announcement ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to update announcement: ${e.message}")
                    }
                }

                delete("/{id}") {
                    try {
                        val id = validateAnnouncementId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@delete
                        
                        // TODO: delete from database
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid announcement ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to delete announcement: ${e.message}")
                    }
                }
            }
        }
    }
}
