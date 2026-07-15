package com.precon.mhsclubs.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import io.ktor.server.application.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.string
import kotlinx.serialization.json.decodeFromJsonElement
import org.slf4j.LoggerFactory

/**
 * Verifies Firebase ID tokens and extracts [UserIdentity] claims.
 *
 * Uses the Firebase Admin SDK's [FirebaseAuth] for server-side token verification.
 * Tokens are expected in the Authorization header as "Bearer <token>".
 *
 * @param projectId Firebase project ID (used for custom claims lookup).
 *                  Set via the [AuthPlugin] or the FIREBASE_PROJECT_ID env var.
 */
class FirebaseTokenVerifier(
    private val projectId: String,
    private val firebaseAuth: FirebaseAuth? = null
) {
    companion object {
        private val log = LoggerFactory.getLogger(FirebaseTokenVerifier::class.java)

        /**
         * Keys used to look up the project ID from ApplicationCall attributes.
         */
        val ProjectIdKey: AttributeKey<String> = AttributeKey<>("firebase-project-id")
    }

    /**
     * Verifies a Firebase ID token and returns the decoded [UserIdentity].
     *
     * @param token The raw ID token string from the Authorization header.
     * @throws IllegalArgumentException if the token is invalid or expired.
     * @throws SecurityException if the email domain is not an allowed school domain.
     */
    suspend fun verify(token: String): UserIdentity = withContext(Dispatchers.IO) {
        val decoded = getFirebaseAuth().verifyIdToken(token)
        toUserIdentity(decoded)
    }

    /**
     * Verifies a token from the Authorization header of [call].
     *
     * @throws IllegalArgumentException if no Bearer token is present.
     * @throws IllegalArgumentException if the token is invalid or expired.
     * @throws SecurityException if the email domain is not an allowed school domain.
     */
    suspend fun verifyFromHeader(call: ApplicationCall): UserIdentity {
        val authHeader = call.request.headers["Authorization"]
            ?: throw IllegalArgumentException("Missing Authorization header")

        val schemeAndToken = authHeader.split(" ", limit = 2)
        if (schemeAndToken.size != 2 || schemeAndToken[0].lowercase() != "bearer") {
            throw IllegalArgumentException("Authorization header must use Bearer scheme")
        }

        return verify(schemeAndToken[1])
    }

    /**
     * Checks whether the given [UserIdentity] satisfies the required [scheme].
     *
     * - [AuthenticationScheme.AnyAuthenticated] – always passes if user is non-null.
     * - [AuthenticationScheme.TeacherOnly] – requires email verified AND school domain.
     * - [AuthenticationScheme.AdminOnly] – requires admin custom claim.
     * - [AuthenticationScheme.StudentLeader] – requires the "student_leader" custom claim
     *   for a specific club. Delegates to [checkScheme(identity, scheme, clubId)].
     */
    fun checkScheme(identity: UserIdentity, scheme: AuthenticationScheme): Boolean {
        return when (scheme) {
            AuthenticationScheme.None -> true
            AuthenticationScheme.AnyAuthenticated -> true
            AuthenticationScheme.TeacherOnly -> {
                identity.emailVerified && (
                    identity.email.endsWith("@students.mcpasd.k12.wi.us") ||
                    identity.email.endsWith("@mcpasd.k12.wi.us")
                    )
            }
            AuthenticationScheme.AdminOnly -> identity.claims["admin"] == true
            AuthenticationScheme.StudentLeader -> {
                // StudentLeader requires a clubId context; delegate to the club-scoped overload.
                // Without a clubId, fall back to checking the base role (Teacher).
                checkScheme(identity, AuthenticationScheme.TeacherOnly)
            }
        }
    }

    /**
     * Club-scoped variant of [checkScheme].
     *
     * For [AuthenticationScheme.StudentLeader], checks whether the user has the
     * "student_leader" custom claim for the given [clubId] in Firebase custom claims.
     * This allows per-club admin authorization: a student leader can perform
     * teacher-level actions only for their assigned club.
     *
     * @param clubId The club ID to check student leader status for.
     */
    fun checkScheme(identity: UserIdentity, scheme: AuthenticationScheme, clubId: String): Boolean {
        return when (scheme) {
            AuthenticationScheme.None -> true
            AuthenticationScheme.AnyAuthenticated -> true
            AuthenticationScheme.TeacherOnly -> {
                identity.emailVerified && (
                    identity.email.endsWith("@students.mcpasd.k12.wi.us") ||
                    identity.email.endsWith("@mcpasd.k12.wi.us")
                    )
            }
            AuthenticationScheme.AdminOnly -> identity.claims["admin"] == true
            AuthenticationScheme.StudentLeader -> {
                // Check Firebase custom claims for student_leader scoped to this club.
                // Firebase custom claims store per-club roles as: "student_leader": { "clubId": true }
                val claims = identity.claims["student_leader"] as? Map<*, *>
                claims?.get(clubId) != null
            }
        }
    }

    private fun getFirebaseAuth(): FirebaseAuth {
        return firebaseAuth ?: FirebaseAuth.getInstance()
    }

    private fun toUserIdentity(token: FirebaseToken): UserIdentity {
        val email = token.email ?: throw IllegalArgumentException("ID token missing email claim")
        val uid = token.uid
        val displayName = token.displayName
        val emailVerified = token.isEmailVerified

        // Check email domain restriction
        val isStudent = email.endsWith("@students.mcpasd.k12.wi.us")
        val isTeacher = email.endsWith("@mcpasd.k12.wi.us")
        if (!isStudent && !isTeacher) {
            throw SecurityException(
                "Sign-in rejected: email domain '$email' is not an allowed school domain"
            )
        }

        // Look up custom claims (admin, etc.)
        val claims = try {
            getFirebaseAuth().getUser(uid).customClaims as? Map<String, Any>
                ?: emptyMap<String, Any>()
        } catch (e: Exception) {
            log.warn("Could not load custom claims for uid=$uid: ${e.message}")
            emptyMap<String, Any>()
        }

        return UserIdentity(
            uid = uid,
            email = email,
            displayName = displayName,
            emailVerified = emailVerified,
            claims = claims
        )
    }
}
