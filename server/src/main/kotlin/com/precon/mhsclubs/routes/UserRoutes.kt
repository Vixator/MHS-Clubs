package com.precon.mhsclubs.routes

import com.precon.mhsclubs.auth.AuthenticationScheme
import com.precon.mhsclubs.auth.AuthPlugin.getAuthenticatedUser
import com.precon.mhsclubs.auth.AuthPlugin.requireAuthenticatedUser
import com.precon.mhsclubs.auth.UserIdentity
import com.precon.mhsclubs.validation.Validation
import com.precon.mhsclubs.validation.validateUserId
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
 * Data class for user creation/update requests.
 */
private data class UserRequest(
    val firebaseUid: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null
)

/**
 * Registers REST endpoints for the User entity.
 *
 * All endpoints under this route require an authenticated user.
 * Users can only read/update their own profile unless they have admin claims.
 *
 * GET    /api/users            list all users (admin only)
 * GET    /api/users/{id}       get user by ID
 * POST   /api/users            create a new user (Firebase signup callback)
 * PUT    /api/users/{id}       update a user
 * DELETE /api/users/{id}       delete a user (admin only)
 */
fun Route.userRoutes() {
    route("api") {
        route(RouteNames.USERS) {
            // Require teacher or admin role for user management
            requireAuth(AuthenticationScheme.TeacherOnly) {
                get {
                    try {
                        val identity = call.getAuthenticatedUser()
                        // TODO: fetch from database, filter by admin scope
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true, data = emptyList<Any>())
                        )
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to fetch users: ${e.message}")
                    }
                }

                get("/{id}") {
                    try {
                        val id = validateUserId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@get
                        
                        // Check if user is admin or requesting their own profile
                        if (identity.uid != id && identity.claims["admin"] != true) {
                            return@get call.respond(
                                HttpStatusCode.Forbidden,
                                RouteNames.ApiResponse.error("You can only view your own profile")
                            )
                        }
                        
                        // TODO: fetch from database
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true, data = null)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid user ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to fetch user: ${e.message}")
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
                            json.decodeFromString<UserRequest>(requestText)
                        } catch (e: Exception) {
                            return@post call.respondBadRequest("Invalid request body: ${e.message}")
                        }
                        
                        // Validate required fields
                        if (request.firebaseUid.isNullOrBlank()) {
                            return@post call.respondBadRequest("Firebase UID is required")
                        }
                        
                        if (request.email.isNullOrBlank()) {
                            return@post call.respondBadRequest("Email is required")
                        }
                        
                        // Validate email format
                        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\$.")
                        if (!emailRegex.matches(request.email)) {
                            return@post call.respondBadRequest("Invalid email format")
                        }
                        
                        // Validate email domain
                        val email = request.email.lowercase()
                        if (!email.endsWith("@students.mcpasd.k12.wi.us") && 
                            !email.endsWith("@mcpasd.k12.wi.us")) {
                            return@post call.respondBadRequest(
                                "Email must be from @students.mcpasd.k12.wi.us or @mcpasd.k12.wi.us domain"
                            )
                        }
                        
                        if (request.displayName.isNullOrBlank()) {
                            return@post call.respondBadRequest("Display name is required")
                        }
                        
                        if (request.displayName.length > 100) {
                            return@post call.respondBadRequest("Display name must be 100 characters or less")
                        }
                        
                        // TODO: deserialize and persist with validated data
                        call.respond(
                            HttpStatusCode.Created,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to create user: ${e.message}")
                    }
                }

                put("/{id}") {
                    try {
                        val id = validateUserId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@put
                        
                        // Check if user is admin or updating their own profile
                        if (identity.uid != id && identity.claims["admin"] != true) {
                            return@put call.respond(
                                HttpStatusCode.Forbidden,
                                RouteNames.ApiResponse.error("You can only update your own profile")
                            )
                        }
                        
                        // Parse and validate request body
                        val requestText = call.receiveText()
                        if (requestText.isBlank()) {
                            return@put call.respondBadRequest("Request body is required")
                        }
                        
                        val request = try {
                            json.decodeFromString<UserRequest>(requestText)
                        } catch (e: Exception) {
                            return@put call.respondBadRequest("Invalid request body: ${e.message}")
                        }
                        
                        // At least one field must be provided for update
                        if (request.firebaseUid == null && request.email == null && 
                            request.displayName == null && request.avatarUrl == null) {
                            return@put call.respondBadRequest("At least one field must be provided for update")
                        }
                        
                        // Validate fields if provided
                        if (request.email != null) {
                            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\$.")
                            if (!emailRegex.matches(request.email)) {
                                return@put call.respondBadRequest("Invalid email format")
                            }
                            
                            val email = request.email.lowercase()
                            if (!email.endsWith("@students.mcpasd.k12.wi.us") && 
                                !email.endsWith("@mcpasd.k12.wi.us")) {
                                return@put call.respondBadRequest(
                                    "Email must be from @students.mcpasd.k12.wi.us or @mcpasd.k12.wi.us domain"
                                )
                            }
                        }
                        
                        if (request.displayName != null && request.displayName.length > 100) {
                            return@put call.respondBadRequest("Display name must be 100 characters or less")
                        }
                        
                        // TODO: deserialize and persist with validated data
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid user ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to update user: ${e.message}")
                    }
                }

                delete("/{id}") {
                    try {
                        val id = validateUserId()
                        val identity = call.requireAuthenticatedUser()
                            ?: return@delete
                        
                        // Only admin can delete users
                        if (identity.claims["admin"] != true) {
                            return@delete call.respond(
                                HttpStatusCode.Forbidden,
                                RouteNames.ApiResponse.error("Only admin can delete users")
                            )
                        }
                        
                        // TODO: delete from database
                        call.respond(
                            HttpStatusCode.OK,
                            RouteNames.ApiResponse(success = true)
                        )
                    } catch (e: Validation.BadRequestException) {
                        call.respondBadRequest(e.message ?: "Invalid user ID")
                    } catch (e: Exception) {
                        call.respondBadRequest("Failed to delete user: ${e.message}")
                    }
                }
            }
        }
    }
}
