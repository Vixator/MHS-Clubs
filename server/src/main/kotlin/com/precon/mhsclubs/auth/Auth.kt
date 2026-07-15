package com.precon.mhsclubs.auth

import kotlinx.serialization.Serializable

/**
 * Authenticated user identity extracted from a verified Firebase ID token.
 *
 * Carries the minimal claims needed for authorization decisions.
 * Populated by [FirebaseTokenVerifier] after successful token verification.
 */
@Serializable
data class UserIdentity(
    /** Firebase Auth UID (unique per provider account) */
    val uid: String,
    /** Email address from the ID token */
    val email: String,
    /** Display name from the ID token (may be null for provider-only accounts) */
    val displayName: String? = null,
    /** Email verified flag from the ID token */
    val emailVerified: Boolean = false,
    /** Custom claims (e.g. admin role) – populated from Firebase custom claims */
    val claims: Map<String, Any> = emptyMap()
) {
    /**
     * Returns true if this user has the "admin" custom claim.
     * Admins are auto-granted teacher privileges across all clubs.
     */
    val isAdmin: Boolean
        get() = claims["admin"] == true

    /**
     * Returns true if the user's email has been verified via Firebase.
     */
    val isEmailVerified: Boolean
        get() = emailVerified
}

/**
 * Authentication scheme used for route protection.
 *
 * - [None] – no auth required (public route).
 * - [AnyAuthenticated] – any verified Firebase ID token is sufficient.
 * - [TeacherOnly] – requires email verified AND domain-restricted to @mcpasd.k12.wi.us.
 * - [AdminOnly] – requires the "admin" custom claim in Firebase custom claims.
 * - [StudentLeader] – requires the user to have the "student_leader" custom claim for a
 *   specific club (passed as [clubId] to [FirebaseTokenVerifier.checkScheme]).
 *   A student leader has teacher-level permissions for their club only.
 */
enum class AuthenticationScheme {
    None,
    AnyAuthenticated,
    TeacherOnly,
    AdminOnly,
    StudentLeader
}
