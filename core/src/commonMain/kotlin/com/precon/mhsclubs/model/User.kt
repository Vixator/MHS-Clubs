package com.precon.mhsclubs.model

import kotlin.time.Clock
import kotlinx.serialization.Serializable

/**
 * User profile entity mapped to the `users` PostgreSQL table.
 *
 * Represents an authenticated student or teacher whose role is determined
 * at sign-in by email domain (see UserRole). Student Leader status is a
 * per-club flag on Membership, not stored here.
 *
 * All timestamp fields are ISO-8601 strings matching SQLDelight's TEXT columns.
 */
@Serializable
data class User(
    val id: String = newId(),
    val firebaseUid: String,
    val email: String,
    val displayName: String,
    val role: UserRole,
    val avatarUrl: String? = null,
    val createdAt: String = Clock.System.now().toString(),
    val updatedAt: String = Clock.System.now().toString()
) {
    /**
     * Returns true if this user is a teacher (auto-admin of all clubs).
     */
    val isTeacher: Boolean
        get() = role == UserRole.Teacher

    /**
     * Returns true if this user is a student.
     */
    val isStudent: Boolean
        get() = role == UserRole.Student

    companion object {
        /**
         * Constructs a User from a Firebase Auth UID and email,
         * deriving the role from the email domain.
         *
         * @throws IllegalArgumentException if the email domain is not recognized
         */
        fun fromFirebase(
            firebaseUid: String,
            email: String,
            displayName: String,
            avatarUrl: String? = null
        ): User {
            val role = fromEmailDomain(email)
            return User(
                firebaseUid = firebaseUid,
                email = email,
                displayName = displayName,
                role = role,
                avatarUrl = avatarUrl
            )
        }

        /**
         * Derives UserRole from the email domain per PRD Section 5.1:
         * - @students.mcpasd.k12.wi.us → Student
         * - @mcpasd.k12.wi.us → Teacher
         * - Any other domain → throws
         */
        private fun fromEmailDomain(email: String): UserRole {
            return when {
                email.endsWith("@students.mcpasd.k12.wi.us") -> UserRole.Student
                email.endsWith("@mcpasd.k12.wi.us") -> UserRole.Teacher
                else -> throw IllegalArgumentException(
                    "Sign-in rejected: email domain '$email' is not an allowed school domain"
                )
            }
        }
    }
}
