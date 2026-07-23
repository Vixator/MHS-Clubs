package com.precon.mhsclubs.auth

import kotlinx.serialization.Serializable

/**
 * Authenticated user identity extracted from a verified Firebase ID token.
 *
 * Carries the minimal identity data needed for authorization decisions.
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
) {
    /**
     * Returns true if the user's email has been verified via Firebase.
     */
    val isEmailVerified: Boolean
        get() = emailVerified
}

/**
 * Minimal route authorization markers. Staff authorization is based solely on
 * the verified staff email suffix, never Firebase custom claims.
 */
enum class AuthenticationScheme {
    None,
    AnyAuthenticated,
    StaffOnly
}
