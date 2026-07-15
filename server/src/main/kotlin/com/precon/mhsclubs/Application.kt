package com.precon.mhsclubs

import com.precon.mhsclubs.auth.*
import com.precon.mhsclubs.routes.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.cors.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private val log = LoggerFactory.getLogger(Application::class.java)

/**
 * Firebase project ID resolved from environment or system properties.
 * Falls back to a placeholder that will produce a runtime warning.
 */
private val FirebaseProjectId: String
    get() = System.getenv("FIREBASE_PROJECT_ID")
        ?: System.getProperty("firebase.projectId")
        ?: run {
            log.warn(
                "FIREBASE_PROJECT_ID not set – auth will be in development mode " +
                "(tokens accepted without verification). Set FIREBASE_PROJECT_ID " +
                "to a valid Firebase project ID for production."
            )
            "dev-project-placeholder"
        }

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

/**
 * Application module that configures CORS, authentication, and route registration.
 *
 * Authentication is installed as an application-level plugin so that every route
 * can access the [UserIdentity] via [ApplicationCall.getAuthenticatedUser].
 *
 * Routes are grouped by their authentication requirement:
 * - Public: club directory, announcements
 * - Authenticated: user profile, memberships, events, RSVP, attendance
 */
fun Application.module() {
    // CORS configuration: allows cross-origin requests from localhost and the school domain.
    install(CORS) {
        allowHost("localhost:*")
        allowHost("*.mcpasd.k12.wi.us")
        allowSameOrigin = false
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("Accept")
        allowHeader("Origin")
        allowHeader("Content-Type")
        allowNonSimpleContentTypes = true
    }

    // Install auth plugin with the Firebase project ID.
    // In development mode (no project ID), tokens are accepted without verification.
    install(AuthPlugin.Plugin) {
        projectId = FirebaseProjectId
    }

    routing {
        get("/") {
            call.respondText(sayHello("Ktor"))
        }

        // ── Public routes (no authentication required) ──────────────────
        clubRoutes()
        announcementRoutes()

        // ── Authenticated routes (any verified Firebase ID token) ───────
        authenticate(FirebaseProjectId) {
            userRoutes()
            membershipRoutes()
            eventRoutes()
            attendanceRoutes()
            rsvpRoutes()
        }
    }
}