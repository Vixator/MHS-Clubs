package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import com.precon.mhsclubs.validation.Validation
import com.precon.mhsclubs.validation.validateMembershipId
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
 * Data class for membership creation/update requests.
 */
private data class MembershipRequest(
    val userId: String? = null,
    val clubId: String? = null,
    val role: String? = null,  // "member", "officer", "student_leader"
    val status: String? = null  // "pending", "active", "revoked"
)

/**
 * Registers REST endpoints for the Membership entity.
 *
 * All membership operations require an authenticated user.
 * Students can join/leave clubs; teachers can manage memberships.
 *
 * GET    /api/memberships            list memberships (authenticated)
 * GET    /api/memberships/{id}       get membership by ID (authenticated)
 * POST   /api/memberships            create a membership / join a club (authenticated)
 * PUT    /api/memberships/{id}       update membership role/status (teacher only)
 * DELETE /api/memberships/{id}       remove membership (teacher only or self-leave)
 */
fun Route.membershipRoutes() {
    route("api") {
        route(RouteNames.MEMBERSHIPS) {
            // All membership routes require authentication
            requireAuth(AuthenticationScheme.AnyAuthenticated) {
                get {
                    try {
                        val identity = call.requireAuthenticatedUser()
                            ?: return@get
                        // TODO: fetch memberships for this user or all (admin)
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true, data = emptyList<Any>())
                        )
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to fetch memberships: ${e.message}")
                    }
                }

                get("/{id}") {
                    try {
                        val id = validateMembershipId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@get
                        // TODO: fetch from database
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true, data = null)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid membership ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to fetch membership: ${e.message}")
                    }
                }

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
                            json.decodeFromString<MembershipRequest>(requestText)
                        } catch (e: Exception) {
                            return@post call.respondBadRequest("Invalid request body: ${e.message}")
                        }
                        
                        // Validate required fields
                        if (request.clubId.isNullOrBlank()) {
                            return@post call.respondBadRequest("Club ID is required")
                        }
                        
                        // For students, userId must be their own ID or null (defaults to current user)
                        // For teachers, userId can be any user
                        if (request.userId.isNullOrBlank()) {
                            // Default to current user
                            request.copy(userId = identity.uid)
                        }
                        
                        // Validate role if provided
                        if (request.role != null) {
                            val validRoles = setOf("member", "officer", "student_leader")
                            if (request.role !in validRoles) {
                                return@post call.respondBadRequest(
                                    "Role must be one of: ${validRoles.joinToString(", ")}"
                                )
                            }
                        }
                        
                        // Validate status if provided
                        if (request.status != null) {
                            val validStatuses = setOf("pending", "active", "revoked")
                            if (request.status !in validStatuses) {
                                return@post call.respondBadRequest(
                                    "Status must be one of: ${validStatuses.joinToString(", ")}"
                                )
                            }
                        }
                        
                        // TODO: deserialize and persist with validated data
                        call.respond(
                            HttpStatusCode.Created,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to create membership: ${e.message}")
                    }
                }

                put("/{id}") {
                    try {
                        val id = validateMembershipId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@put
                        
                        // Parse and validate request body
                        val requestText = call.receiveText()
                        if (requestText.isBlank()) {
                            return@put call.respondBadRequest("Request body is required")
                        }
                        
                        val request = try {
                            json.decodeFromString<MembershipRequest>(requestText)
                        } catch (e: Exception) {
                            return@put call.respondBadRequest("Invalid request body: ${e.message}")
                        }
                        
                        // At least one field must be provided for update
                        if (request.userId == null && request.clubId == null && 
                            request.role == null && request.status == null) {
                            return@put call.respondBadRequest("At least one field must be provided for update")
                        }
                        
                        // Validate role if provided
                        if (request.role != null) {
                            val validRoles = setOf("member", "officer", "student_leader")
                            if (request.role !in validRoles) {
                                return@put call.respondBadRequest(
                                    "Role must be one of: ${validRoles.joinToString(", ")}"
                                )
                            }
                        }
                        
                        // Validate status if provided
                        if (request.status != null) {
                            val validStatuses = setOf("pending", "active", "revoked")
                            if (request.status !in validStatuses) {
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
                        call.respondBadRequest(e.message ?: "Invalid membership ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to update membership: ${e.message}")
                    }
                }

                delete("/{id}") {
                    try {
                        val id = validateMembershipId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@delete
                        
                        // TODO: delete from database
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid membership ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to delete membership: ${e.message}")
                    }
                }
            }
        }
    }
}
