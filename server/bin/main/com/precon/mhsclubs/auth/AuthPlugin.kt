package com.precon.mhsclubs.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory

/**
 * Ktor application plugin that installs authentication middleware.
 *
 * Installs via [Application.install(AuthPlugin)] and configures the [FirebaseTokenVerifier]
 * with the Firebase project ID. The project ID is also stored as an [Application] attribute
 * so individual route files can access it if needed.
 *
 * Usage in [Application.module]:
 * ```kotlin
 * install(AuthPlugin) {
 *     projectId = environment.config.property("ktor.deploy.env").getString()
 * }
 * ```
 */
object AuthPlugin {
    private val log = LoggerFactory.getLogger(AuthPlugin::class.java)

    /**
     * Configuration for the auth plugin.
     */
    class Config {
        /** Firebase project ID for custom claims lookup. */
        var projectId: String = ""
    }

    /**
     * Installs the auth plugin into the application.
     */
    val Plugin = ApplicationPlugin<Config>(
        name = "MhsClubsAuth",
        createConfig = ::Config,
        pluginConfigurer = { apply(it) }
    )

    private fun apply(config: Config) {
        val verifier = FirebaseTokenVerifier(config.projectId)

        application.attributes[FirebaseTokenVerifier.ProjectIdKey] = config.projectId

        // Store the verifier in application attributes for route access
        application.attributes[VerifierKey] = verifier

        // Install a fallback interceptor that catches unauthenticated requests
        // to any route that requires auth but has no token.
        intercept(ApplicationCallPipeline.Call) {
            val requiredScheme = call.attributes.getOrNull(AuthSchemeKey)
                ?: return@intercept // route doesn't require auth

            val identity = call.attributes.getOrNull(UserIdentityKey)
            if (identity == null) {
                log.debug("No authenticated identity for route requiring $requiredScheme")
                call.respond(HttpStatusCode.Unauthorized,
                    RouteNames.ApiResponse.error("Authentication required"))
                return@intercept
            }

            // For club-scoped schemes (e.g. StudentLeader), pass the club ID from route
            // parameters so the verifier can check per-club custom claims.
            val clubId = call.attributes.getOrNull(ClubIdKey)
            val schemeOk = if (clubId != null) {
                verifier.checkScheme(identity, requiredScheme, clubId)
            } else {
                verifier.checkScheme(identity, requiredScheme)
            }
            if (!schemeOk) {
                log.debug("Identity ${identity.uid} does not satisfy $requiredScheme")
                call.respond(HttpStatusCode.Forbidden,
                    RouteNames.ApiResponse.error("Insufficient permissions"))
                return@intercept
            }
        }
    }

    /**
     * Extension function on [Route] that installs the auth plugin with the given config.
     *
     * This is the recommended way to install auth in a module() function:
     * ```kotlin
     * route("api") {
     *     authenticate(projectId = "my-firebase-project") {
     *         // routes inside require auth
     *     }
     * }
     * ```
     */
    fun Route.authenticate(
        projectId: String,
        block: Route.() -> Unit
    ) {
        val route = this
        route.install(AuthPlugin.Plugin) {
            this.projectId = projectId
        }
        route.block()
    }

    /**
     * Extension function on [Route] that marks a route group as requiring the given
     * [AuthenticationScheme].
     *
     * Usage within a route block:
     * ```kotlin
     * requireAuth(AuthenticationScheme.TeacherOnly) {
     *     get("admin-only") { ... }
     * }
     * ```
     */
    fun Route.requireAuth(scheme: AuthenticationScheme, block: Route.() -> Unit) {
        val route = this
        route.intercept(ApplicationCallPipeline.Call) {
            call.attributes[AuthSchemeKey] = scheme
            block()
        }
    }

    /**
     * Extension function on [Route] that requires the caller to be a teacher OR a
     * student leader for the club identified by the `{clubId}` route parameter.
     *
     * Extracts the club ID from the route parameters and sets it in call attributes
     * so downstream interceptors can use it for authorization decisions.
     * Falls back to [AuthenticationScheme.TeacherOnly] if the club ID is not present.
     *
     * Usage within a route block:
     * ```kotlin
     * requireAuthForClub {
     *     put("{clubId}") {
     *         // Only teachers or student leaders for this club can reach here
     *     }
     * }
     * ```
     */
    fun Route.requireAuthForClub(block: Route.() -> Unit) {
        val route = this
        route.intercept(ApplicationCallPipeline.Call) {
            // Extract club_id from route parameters and store it so the auth interceptor
            // can use it for per-club authorization checks (e.g. student leader lookup).
            val clubId = call.parameters["clubId"]
            if (clubId != null) {
                call.attributes[ClubIdKey] = clubId
            }
            call.attributes[AuthSchemeKey] = AuthenticationScheme.StudentLeader
            block()
        }
    }

    /**
     * Extension function on [ApplicationCall] that returns the currently authenticated
     * [UserIdentity], or null if no token was provided.
     *
     * If a token is present, it is verified and the identity is cached in call attributes.
     */
    suspend fun ApplicationCall.getAuthenticatedUser(): UserIdentity? {
        val verifier = attributes.getOrNull(VerifierKey)
            ?: return null // auth plugin not installed

        // Return cached identity if already verified
        val cached = attributes.getOrNull(UserIdentityKey)
        if (cached != null) return cached

        val authHeader = request.headers["Authorization"]
        if (authHeader == null) return null

        val schemeAndToken = authHeader.split(" ", limit = 2)
        if (schemeAndToken.size != 2 || schemeAndToken[0].lowercase() != "bearer") {
            return null
        }

        val identity = try {
            verifier.verify(schemeAndToken[1])
        } catch (e: Exception) {
            log.debug("Token verification failed: ${e.message}")
            return null
        }

        attributes[UserIdentityKey] = identity
        return identity
    }

    /**
     * Extension function on [ApplicationCall] that returns the authenticated [UserIdentity],
     * or responds with 401 and returns null (convenience for route handlers).
     */
    suspend fun ApplicationCall.requireAuthenticatedUser(): UserIdentity? {
        val authHeader = request.headers["Authorization"]
        if (authHeader == null) {
            respond(HttpStatusCode.Unauthorized,
                RouteNames.ApiResponse.error("Authentication required"))
            return null
        }

        val schemeAndToken = authHeader.split(" ", limit = 2)
        if (schemeAndToken.size != 2 || schemeAndToken[0].lowercase() != "bearer") {
            respond(HttpStatusCode.Unauthorized,
                RouteNames.ApiResponse.error("Authorization header must use Bearer scheme"))
            return null
        }

        val verifier = attributes.getOrNull(VerifierKey)
            ?: return null

        val identity = try {
            verifier.verify(schemeAndToken[1])
        } catch (e: Exception) {
            respond(HttpStatusCode.Unauthorized,
                RouteNames.ApiResponse.error("Invalid or expired token"))
            return null
        }

        attributes[UserIdentityKey] = identity
        return identity
    }

    private val VerifierKey: AttributeKey<FirebaseTokenVerifier> = AttributeKey<>("auth-verifier")
    private val UserIdentityKey: AttributeKey<UserIdentity> = AttributeKey<>("auth-identity")
    private val AuthSchemeKey: AttributeKey<AuthenticationScheme> = AttributeKey<>("auth-scheme")
    private val ClubIdKey: AttributeKey<String> = AttributeKey<>("auth-club-id")
}
