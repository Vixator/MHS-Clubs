package com.precon.mhsclubs

import com.precon.mhsclubs.auth.FirebaseTokenVerifier
import com.precon.mhsclubs.auth.UserIdentity
import com.precon.mhsclubs.firebase.FirebaseConfig
import com.precon.mhsclubs.services.NocoDbClient
import com.precon.mhsclubs.services.NocoDbException
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    val port = System.getenv("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    FirebaseConfig.initialize()
    val verifier = FirebaseTokenVerifier(FirebaseConfig.projectId)
    val nocoDb = NocoDbClient()

    install(CORS) {
        allowHost("localhost:*", schemes = listOf("http", "https"))
        allowHost("*.mcpasd.k12.wi.us", schemes = listOf("https"))
        System.getenv("WEB_ALLOWED_HOST")
            ?.takeIf { it.isNotBlank() }
            ?.let { allowHost(it, schemes = listOf("https")) }
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
    }

    routing {
        get("/") { call.respondText("MHS Clubs API") }
        get("/health") {
            call.respondText(
                """{"status":"ok","firebaseInitialized":${FirebaseConfig.isInitialized()},"nocoDbConfigured":${nocoDb.isConfigured()}}""",
                ContentType.Application.Json
            )
        }
        route("/api") {
            post("/memberships/join") {
                val identity = call.requireIdentity(verifier) ?: return@post
                val clubId = call.extractRequiredId("clubId") ?: return@post
                call.respondNoco(HttpStatusCode.Created) {
                    nocoDb.createRecord("memberships", """{"firebase_uid":"${identity.uid}","club_id":"$clubId","status":"active"}""")
                }
            }
            post("/rsvps/respond") {
                val identity = call.requireIdentity(verifier) ?: return@post
                val body = call.receiveText()
                val eventId = extractIdentifier(body, "eventId")
                val status = extractIdentifier(body, "status")
                if (eventId == null || status !in setOf("yes", "no", "maybe")) {
                    return@post call.respondText("""{"success":false,"error":"eventId and RSVP status (yes, no, or maybe) are required"}""", ContentType.Application.Json, HttpStatusCode.BadRequest)
                }
                call.respondNoco(HttpStatusCode.Created) {
                    nocoDb.createRecord("rsvps", """{"firebase_uid":"${identity.uid}","event_id":"$eventId","status":"$status"}""")
                }
            }
            put("/memberships/{membershipId}/club-admin") {
                val identity = call.requireIdentity(verifier) ?: return@put
                if (!verifier.isStaff(identity)) return@put call.respondForbidden()
                val enabled = extractBoolean(call.receiveText(), "enabled")
                    ?: return@put call.respondBadRequest("enabled must be true or false")
                call.respondNoco {
                    nocoDb.updateRecord("memberships", call.parameters["membershipId"].orEmpty(), """{"is_club_admin":$enabled}""")
                }
            }
            post("/clubs/{clubId}/{resource}") {
                val identity = call.requireIdentity(verifier) ?: return@post
                val clubId = call.parameters["clubId"].orEmpty()
                val resource = call.parameters["resource"].orEmpty()
                if (resource !in clubManagedResources) return@post call.respondBadRequest("Unsupported club-managed resource")
                if (!call.canManageClub(verifier, nocoDb, identity, clubId)) return@post
                val body = call.receiveText()
                val record = when (resource) {
                    "memberships" -> {
                        val firebaseUid = extractIdentifier(body, "firebaseUid")
                            ?: return@post call.respondBadRequest("firebaseUid is required")
                        """{"firebase_uid":"$firebaseUid","club_id":"$clubId","status":"active","is_club_admin":false}"""
                    }
                    else -> body.withClubId(clubId) ?: return@post call.respondBadRequest("Do not supply club_id; it is taken from the route")
                }
                call.respondNoco(HttpStatusCode.Created) { nocoDb.createRecord(resource, record) }
            }
            put("/clubs/{clubId}/{resource}/{id}") {
                val identity = call.requireIdentity(verifier) ?: return@put
                val clubId = call.parameters["clubId"].orEmpty()
                val resource = call.parameters["resource"].orEmpty()
                if (resource !in clubManagedResources) return@put call.respondBadRequest("Unsupported club-managed resource")
                if (!call.canManageClub(verifier, nocoDb, identity, clubId)) return@put
                val existing = nocoDb.recordById(resource, call.parameters["id"].orEmpty())
                    ?: return@put call.respondText("""{"success":false,"error":"Record not found"}""", ContentType.Application.Json, HttpStatusCode.NotFound)
                if (extractIdentifier(existing, "club_id") != clubId) return@put call.respondForbidden()
                call.respondNoco { nocoDb.updateRecord(resource, call.parameters["id"].orEmpty(), call.receiveText()) }
            }
            get("/{resource}") {
                val identity = call.requireIdentity(verifier) ?: return@get
                val resource = call.parameters["resource"].orEmpty()
                if (resource !in studentReadableResources && !verifier.isStaff(identity)) return@get call.respondForbidden()
                call.respondNoco { nocoDb.listRecords(resource) }
            }
            get("/{resource}/{id}") {
                val identity = call.requireIdentity(verifier) ?: return@get
                val resource = call.parameters["resource"].orEmpty()
                if (resource !in studentReadableResources && !verifier.isStaff(identity)) return@get call.respondForbidden()
                call.respondNoco { nocoDb.recordById(resource, call.parameters["id"].orEmpty()) }
            }
            post("/{resource}") {
                val identity = call.requireIdentity(verifier) ?: return@post
                if (!verifier.isStaff(identity)) return@post call.respondForbidden()
                call.respondNoco(HttpStatusCode.Created) { nocoDb.createRecord(call.parameters["resource"].orEmpty(), call.receiveText()) }
            }
            put("/{resource}/{id}") {
                val identity = call.requireIdentity(verifier) ?: return@put
                if (!verifier.isStaff(identity)) return@put call.respondForbidden()
                call.respondNoco { nocoDb.updateRecord(call.parameters["resource"].orEmpty(), call.parameters["id"].orEmpty(), call.receiveText()) }
            }
            delete("/{resource}/{id}") {
                val identity = call.requireIdentity(verifier) ?: return@delete
                if (!verifier.isStaff(identity)) return@delete call.respondForbidden()
                call.respondNoco { nocoDb.deleteRecord(call.parameters["resource"].orEmpty(), call.parameters["id"].orEmpty()) }
            }
        }
    }
}

private val studentReadableResources = setOf("clubs", "events", "announcements")
private val clubManagedResources = setOf("memberships", "events", "announcements", "attendance")

private suspend fun ApplicationCall.requireIdentity(verifier: FirebaseTokenVerifier): UserIdentity? {
    val token = request.headers[HttpHeaders.Authorization]
        ?.takeIf { it.startsWith("Bearer ", ignoreCase = true) }
        ?.removePrefix("Bearer ")
        ?.trim()
    if (token.isNullOrBlank()) {
        respondText("""{"success":false,"error":"Authentication required"}""", ContentType.Application.Json, HttpStatusCode.Unauthorized)
        return null
    }
    return try {
        verifier.verify(token)
    } catch (_: Exception) {
        respondText("""{"success":false,"error":"Invalid, unverified, or unauthorized Firebase token"}""", ContentType.Application.Json, HttpStatusCode.Unauthorized)
        null
    }
}

private suspend fun ApplicationCall.respondNoco(status: HttpStatusCode = HttpStatusCode.OK, operation: suspend () -> String?) {
    try {
        val body = operation() ?: return respondText("""{"success":false,"error":"Record not found"}""", ContentType.Application.Json, HttpStatusCode.NotFound)
        respondText("""{"success":true,"data":$body}""", ContentType.Application.Json, status)
    } catch (exception: NocoDbException) {
        respondText("""{"success":false,"error":"${exception.message}"}""", ContentType.Application.Json, HttpStatusCode.BadGateway)
    } catch (exception: IllegalStateException) {
        respondText("""{"success":false,"error":"${exception.message}"}""", ContentType.Application.Json, HttpStatusCode.ServiceUnavailable)
    }
}

private suspend fun ApplicationCall.respondForbidden() {
    respondText("""{"success":false,"error":"Staff administrator access required"}""", ContentType.Application.Json, HttpStatusCode.Forbidden)
}

private suspend fun ApplicationCall.respondBadRequest(message: String) {
    respondText("""{"success":false,"error":"$message"}""", ContentType.Application.Json, HttpStatusCode.BadRequest)
}

private fun ApplicationCall.canManageClub(
    verifier: FirebaseTokenVerifier,
    nocoDb: NocoDbClient,
    identity: UserIdentity,
    clubId: String
): Boolean {
    if (!clubId.matches(Regex("[A-Za-z0-9_-]{1,128}"))) {
        return false
    }
    if (verifier.isStaff(identity)) return true
    return try {
        nocoDb.isClubAdmin(identity.uid, clubId)
    } catch (_: Exception) {
        false
    }
}

private suspend fun ApplicationCall.extractRequiredId(field: String): String? {
    val value = extractIdentifier(receiveText(), field)
    if (value == null) {
        respondText("""{"success":false,"error":"$field is required"}""", ContentType.Application.Json, HttpStatusCode.BadRequest)
    }
    return value
}

private fun extractIdentifier(json: String, field: String): String? =
    Regex("\\\"$field\\\"\\s*:\\s*\\\"([A-Za-z0-9_-]{1,128})\\\"")
        .find(json)
        ?.groupValues
        ?.getOrNull(1)

private fun extractBoolean(json: String, field: String): Boolean? =
    Regex("\\\"$field\\\"\\s*:\\s*(true|false)")
        .find(json)
        ?.groupValues
        ?.getOrNull(1)
        ?.toBooleanStrictOrNull()

private fun String.withClubId(clubId: String): String? {
    val body = trim()
    if (!body.startsWith("{") || !body.endsWith("}") || Regex("\\\"club(_id|Id)\\\"").containsMatchIn(body)) return null
    return body.removeSuffix("}").trimEnd().let { prefix ->
        if (prefix == "{") """{"club_id":"$clubId"}""" else "$prefix,\"club_id\":\"$clubId\"}"
    }
}
