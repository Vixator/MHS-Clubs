package com.precon.mhsclubs.validation

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.slf4j.LoggerFactory

/**
 * Validation utilities for request data.
 */
object Validation {
    private val log = LoggerFactory.getLogger(Validation::class.java)
    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Validates that a required parameter is present and not empty.
     *
     * @param value The value to validate
     * @param parameterName The name of the parameter for error messages
     * @return The value if valid
     * @throws BadRequestException if the value is missing or empty
     */
    fun requireNonEmpty(value: String?, parameterName: String): String {
        if (value == null || value.isBlank()) {
            throw BadRequestException("$parameterName is required and cannot be empty")
        }
        return value
    }

    /**
     * Validates that a string is a valid UUID.
     *
     * @param value The value to validate
     * @param parameterName The name of the parameter for error messages
     * @return The value if valid
     * @throws BadRequestException if the value is not a valid UUID
     */
    fun requireValidUuid(value: String?, parameterName: String): String {
        val nonEmpty = requireNonEmpty(value, parameterName)
        val uuidRegex = Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")
        if (!uuidRegex.matches(nonEmpty)) {
            throw BadRequestException("$parameterName must be a valid UUID")
        }
        return nonEmpty
    }

    /**
     * Validates that a string is a valid email address.
     *
     * @param value The value to validate
     * @param parameterName The name of the parameter for error messages
     * @return The value if valid
     * @throws BadRequestException if the value is not a valid email
     */
    fun requireValidEmail(value: String?, parameterName: String): String {
        val nonEmpty = requireNonEmpty(value, parameterName)
        val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$.")
        if (!emailRegex.matches(nonEmpty)) {
            throw BadRequestException("$parameterName must be a valid email address")
        }
        return nonEmpty
    }

    /**
     * Validates that a string is a valid club code.
     * Club codes are alphanumeric, uppercase, 3-10 characters.
     *
     * @param value The value to validate
     * @param parameterName The name of the parameter for error messages
     * @return The value if valid (uppercased)
     * @throws BadRequestException if the value is not a valid club code
     */
    fun requireValidClubCode(value: String?, parameterName: String): String {
        val nonEmpty = requireNonEmpty(value, parameterName)
        val clubCodeRegex = Regex("^[A-Z0-9]{3,10}$")
        if (!clubCodeRegex.matches(nonEmpty.uppercase())) {
            throw BadRequestException("$parameterName must be 3-10 alphanumeric uppercase characters")
        }
        return nonEmpty.uppercase()
    }

    /**
     * Validates that a string is within a specified length range.
     *
     * @param value The value to validate
     * @param parameterName The name of the parameter for error messages
     * @param minLength Minimum length (inclusive)
     * @param maxLength Maximum length (inclusive)
     * @return The value if valid
     * @throws BadRequestException if the value is not within the length range
     */
    fun requireLengthInRange(
        value: String?,
        parameterName: String,
        minLength: Int = 1,
        maxLength: Int = Int.MAX_VALUE
    ): String {
        val nonEmpty = requireNonEmpty(value, parameterName)
        if (nonEmpty.length < minLength || nonEmpty.length > maxLength) {
            throw BadRequestException("$parameterName must be between $minLength and $maxLength characters")
        }
        return nonEmpty
    }

    /**
     * Validates that a value is one of the allowed values.
     *
     * @param value The value to validate
     * @param parameterName The name of the parameter for error messages
     * @param allowedValues Set of allowed values
     * @return The value if valid
     * @throws BadRequestException if the value is not in the allowed set
     */
    fun <T> requireOneOf(
        value: T?,
        parameterName: String,
        allowedValues: Set<T>
    ): T {
        if (value == null || !allowedValues.contains(value)) {
            throw BadRequestException("$parameterName must be one of: ${allowedValues.joinToString(", ")}")
        }
        return value
    }

    /**
     * Parses and validates a JSON body from the request.
     *
     * @param call The application call
     * @param parameterName The name of the parameter for error messages
     * @return The parsed JsonElement
     * @throws BadRequestException if the body is missing or invalid JSON
     */
    suspend fun requireJsonBody(call: ApplicationCall, parameterName: String = "request body"): JsonElement {
        return try {
            val text = call.receiveText()
            if (text.isBlank()) {
                throw BadRequestException("$parameterName is required and cannot be empty")
            }
            json.parseToJsonElement(text)
        } catch (e: Exception) {
            log.debug("Failed to parse JSON body: ${e.message}")
            throw BadRequestException("$parameterName must be valid JSON")
        }
    }

    /**
     * Validates a club creation/update request.
     */
    fun validateClubRequest(
        name: String?,
        code: String?,
        description: String?,
        category: String?
    ): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        
        if (name == null || name.isBlank()) {
            errors["name"] = "Club name is required"
        } else if (name.length > 100) {
            errors["name"] = "Club name must be 100 characters or less"
        }
        
        if (code == null || code.isBlank()) {
            errors["code"] = "Club code is required"
        } else if (!Regex("^[A-Z0-9]{3,10}$").matches(code.uppercase())) {
            errors["code"] = "Club code must be 3-10 alphanumeric uppercase characters"
        }
        
        if (description != null && description.length > 500) {
            errors["description"] = "Description must be 500 characters or less"
        }
        
        if (category != null && category.length > 50) {
            errors["category"] = "Category must be 50 characters or less"
        }
        
        if (errors.isNotEmpty()) {
            throw BadRequestException("Validation failed: ${errors.entries.joinToString(", ") { "${it.key}: ${it.value}" }}")
        }
        
        return mapOf(
            "name" to name!!,
            "code" to code!!.uppercase(),
            "description" to description ?: "",
            "category" to category ?: "General"
        )
    }

    /**
     * Validates an event creation/update request.
     */
    fun validateEventRequest(
        title: String?,
        startTime: String?,
        endTime: String?,
        location: String?,
        description: String?
    ): Map<String, String?> {
        val errors = mutableMapOf<String, String>()
        
        if (title == null || title.isBlank()) {
            errors["title"] = "Event title is required"
        } else if (title.length > 100) {
            errors["title"] = "Event title must be 100 characters or less"
        }
        
        if (startTime == null || startTime.isBlank()) {
            errors["startTime"] = "Start time is required"
        } else {
            // Validate ISO-8601 date-time format
            try {
                // Simple validation - more thorough validation would parse the date
                if (!Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z").matches(startTime)) {
                    errors["startTime"] = "Start time must be in ISO-8601 format (YYYY-MM-DDTHH:MM:SSZ)"
                }
            } catch (e: Exception) {
                errors["startTime"] = "Start time must be in ISO-8601 format"
            }
        }
        
        if (endTime != null && endTime.isNotBlank()) {
            try {
                if (!Regex("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z").matches(endTime)) {
                    errors["endTime"] = "End time must be in ISO-8601 format (YYYY-MM-DDTHH:MM:SSZ)"
                }
            } catch (e: Exception) {
                errors["endTime"] = "End time must be in ISO-8601 format"
            }
        }
        
        if (location != null && location.length > 100) {
            errors["location"] = "Location must be 100 characters or less"
        }
        
        if (description != null && description.length > 500) {
            errors["description"] = "Description must be 500 characters or less"
        }
        
        if (errors.isNotEmpty()) {
            throw BadRequestException("Validation failed: ${errors.entries.joinToString(", ") { "${it.key}: ${it.value}" }}")
        }
        
        return mapOf(
            "title" to title!!,
            "startTime" to startTime!!,
            "endTime" to endTime,
            "location" to location,
            "description" to description
        )
    }

    /**
     * Custom exception for validation errors.
     */
    class BadRequestException(message: String) : Exception(message)

    /**
     * Extension function to convert to HttpStatusCode.BadRequest response.
     */
    fun ApplicationCall.respondBadRequest(message: String) {
        respond(HttpStatusCode.BadRequest, com.precon.mhsclubs.routes.RouteNames.ApiResponse.error(message))
    }
}

/**
 * Extension function on Route to validate a club ID parameter.
 */
fun Route.validateClubId(): String {
    val id = this.parameters["id"]
    return Validation.requireNonEmpty(id, "club id")
}

/**
 * Extension function on Route to validate a user ID parameter.
 */
fun Route.validateUserId(): String {
    val id = this.parameters["id"]
    return Validation.requireNonEmpty(id, "user id")
}

/**
 * Extension function on Route to validate an event ID parameter.
 */
fun Route.validateEventId(): String {
    val id = this.parameters["id"]
    return Validation.requireNonEmpty(id, "event id")
}

/**
 * Extension function on Route to validate a membership ID parameter.
 */
fun Route.validateMembershipId(): String {
    val id = this.parameters["id"]
    return Validation.requireNonEmpty(id, "membership id")
}

/**
 * Extension function on Route to validate an attendance ID parameter.
 */
fun Route.validateAttendanceId(): String {
    val id = this.parameters["id"]
    return Validation.requireNonEmpty(id, "attendance id")
}

/**
 * Extension function on Route to validate an RSVP ID parameter.
 */
fun Route.validateRsvpId(): String {
    val id = this.parameters["id"]
    return Validation.requireNonEmpty(id, "RSVP id")
}

/**
 * Extension function on Route to validate an announcement ID parameter.
 */
fun Route.validateAnnouncementId(): String {
    val id = this.parameters["id"]
    return Validation.requireNonEmpty(id, "announcement id")
}
