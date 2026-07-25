package com.precon.mhsclubs.auth

import com.precon.mhsclubs.environment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import com.precon.mhsclubs.firebase.FirebaseConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Verifies Firebase ID tokens and applies the server-side school email policy. */
class FirebaseTokenVerifier(
    @Suppress("UNUSED_PARAMETER") private val projectId: String,
    private val firebaseAuth: FirebaseAuth? = null
) {
    suspend fun verify(token: String): UserIdentity = withContext(Dispatchers.IO) {
        check(FirebaseConfig.isInitialized()) {
            "Firebase Admin is not initialized; protected requests are unavailable"
        }
        toIdentity((firebaseAuth ?: FirebaseAuth.getInstance()).verifyIdToken(token))
    }

    fun isStaff(identity: UserIdentity): Boolean =
        identity.emailVerified && normalizedEmail(identity.email).endsWith(staffDomain)

    fun checkScheme(identity: UserIdentity, scheme: AuthenticationScheme): Boolean = when (scheme) {
        AuthenticationScheme.None, AuthenticationScheme.AnyAuthenticated -> identity.emailVerified
        AuthenticationScheme.StaffOnly -> isStaff(identity)
    }

    private fun toIdentity(token: FirebaseToken): UserIdentity {
        val email = token.email?.trim()?.lowercase()
            ?: throw SecurityException("Firebase token does not contain an email")
        if (!token.isEmailVerified || !isAllowedEmail(email)) {
            throw SecurityException("This verified school email is required to use MHS Clubs")
        }
        return UserIdentity(
            uid = token.uid,
            email = email,
            displayName = token.name,
            emailVerified = true
        )
    }

    private fun isAllowedEmail(email: String) =
        normalizedEmail(email).endsWith(studentDomain) || normalizedEmail(email).endsWith(staffDomain)

    private val studentDomain get() = domainFromEnvironment("STUDENT_EMAIL_DOMAIN", "students.mcpasd.k12.wi.us")
    private val staffDomain get() = domainFromEnvironment("STAFF_EMAIL_DOMAIN", "mcpasd.k12.wi.us")

    private fun domainFromEnvironment(name: String, fallback: String) =
        "@${(environment(name) ?: fallback).trim().lowercase().removePrefix("@")}"

    private fun normalizedEmail(email: String) = email.trim().lowercase()
}
