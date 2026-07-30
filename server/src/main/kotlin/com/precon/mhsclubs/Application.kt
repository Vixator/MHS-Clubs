package com.precon.mhsclubs

import com.precon.mhsclubs.auth.FirebaseTokenVerifier
import com.precon.mhsclubs.auth.UserIdentity
import com.precon.mhsclubs.firebase.FirebaseConfig
import com.precon.mhsclubs.services.NocoDbClient
import com.precon.mhsclubs.services.NocoDbException
import com.precon.mhsclubs.services.GoogleCalendarSyncService
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonArray
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.net.URI
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime

fun main() {
    val port = environment("PORT")?.toIntOrNull() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    FirebaseConfig.initialize()
    val verifier = FirebaseTokenVerifier(FirebaseConfig.projectId)
    val nocoDb = NocoDbClient()
    val calendarSync = GoogleCalendarSyncService(nocoDb)

    install(CORS) {
        allowHost("localhost", schemes = listOf("http", "https"))
        configuredWebHost()?.let { allowHost(it, schemes = listOf("https")) }
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
                """{"status":"ok","firebaseInitialized":${FirebaseConfig.isInitialized()},"nocoDbConfigured":${nocoDb.isConfigured()},"googleCalendarConfigured":${calendarSync.isConfigured()}}""",
                ContentType.Application.Json
            )
        }
        route("/api") {
            get("/clubs") {
                call.requireIdentity(verifier) ?: return@get
                call.respondNoco { nocoDb.listRecords("clubs") }
            }
            get("/my/memberships") {
                val identity = call.requireIdentity(verifier) ?: return@get
                call.respondNoco { nocoDb.listRecords("memberships", where = "(firebase_uid,eq,${identity.uid})") }
            }
            get("/my/rsvps") {
                val identity = call.requireIdentity(verifier) ?: return@get
                call.respondNoco { nocoDb.listRecords("rsvps", where = "(firebase_uid,eq,${identity.uid})") }
            }
            /** Returns only content for clubs in which the signed-in student is active. */
            get("/my/{resource}") {
                val identity = call.requireIdentity(verifier) ?: return@get
                val resource = call.parameters["resource"].orEmpty()
                if (resource !in studentOwnedResources) return@get call.respondBadRequest("Unsupported student resource")
                call.respondNoco {
                    val clubIds = nocoDb.activeClubIds(identity.uid)
                    nocoDb.recordsForClubs(resource, clubIds)
                }
            }
            /** Staff can force a pull immediately after changing a Google Calendar event. */
            post("/calendar/sync") {
                val identity = call.requireIdentity(verifier) ?: return@post
                if (!verifier.isStaff(identity)) return@post call.respondForbidden()
                call.respondNoco {
                    val result = calendarSync.sync()
                    """{"calendars":${result.calendars},"created":${result.created},"updated":${result.updated}}"""
                }
            }
            post("/memberships/join") {
                val identity = call.requireIdentity(verifier) ?: return@post
                val clubId = call.extractRequiredId("clubId") ?: return@post
                call.respondNoco(HttpStatusCode.Created) {
                    nocoDb.createRecord("memberships", """{"firebase_uid":"${identity.uid}","club_id":"$clubId","status":"active"}""")
                }
            }
            put("/memberships/{membershipId}/leave") {
                val identity = call.requireIdentity(verifier) ?: return@put
                val membershipId = call.parameters["membershipId"].orEmpty()
                if (!nocoDb.membershipBelongsTo(membershipId, identity.uid)) return@put call.respondForbidden()
                call.respondNoco { nocoDb.updateRecord("memberships", membershipId, """{\"status\":\"revoked\"}""") }
            }
            post("/rsvps/respond") {
                val identity = call.requireIdentity(verifier) ?: return@post
                val body = call.receiveText()
                val eventId = extractIdentifier(body, "eventId")
                val status = extractIdentifier(body, "status")
                if (eventId == null || status !in setOf("yes", "no")) {
                    return@post call.respondText("""{"success":false,"error":"eventId and RSVP status (yes or no) are required"}""", ContentType.Application.Json, HttpStatusCode.BadRequest)
                }
                val clubId = nocoDb.clubForEventKey(eventId) ?: return@post call.respondBadRequest("Unknown event")
                if (!nocoDb.isActiveMember(identity.uid, clubId)) return@post call.respondForbidden()
                if (nocoDb.eventStart(eventId)?.isBefore(Instant.now()) == true) {
                    return@post call.respondBadRequest("RSVPs close after an event begins")
                }
                val existing = nocoDb.findRsvp(identity.uid, eventId)
                call.respondNoco(if (existing == null) HttpStatusCode.Created else HttpStatusCode.OK) {
                    if (existing == null) nocoDb.createRecord("rsvps", """{"firebase_uid":"${identity.uid}","event_id":"$eventId","status":"$status"}""")
                    else nocoDb.updateRecord("rsvps", existing, """{"status":"$status"}""")
                }
            }
            get("/clubs/{clubId}/member-count") {
                call.requireIdentity(verifier) ?: return@get
                val clubId = call.parameters["clubId"].orEmpty()
                val count = nocoDb.activeMemberCount(clubId)
                call.respondText("""{"success":true,"data":{"memberCount":$count}}""", ContentType.Application.Json)
            }
            get("/clubs/{clubId}/attendance/{eventId}") {
                val identity = call.requireIdentity(verifier) ?: return@get
                val clubId = call.parameters["clubId"].orEmpty()
                val eventId = call.parameters["eventId"].orEmpty()
                if (!nocoDb.canAdvise(identity, clubId) || !nocoDb.eventBelongsToClub(eventId, clubId)) return@get call.respondForbidden()
                call.respondNoco { nocoDb.attendanceRoster(clubId, eventId) }
            }
            put("/clubs/{clubId}/attendance/{eventId}") {
                val identity = call.requireIdentity(verifier) ?: return@put
                val clubId = call.parameters["clubId"].orEmpty()
                val eventId = call.parameters["eventId"].orEmpty()
                if (!nocoDb.canAdvise(identity, clubId) || !nocoDb.eventBelongsToClub(eventId, clubId)) return@put call.respondForbidden()
                val updates = parseAttendanceUpdates(call.receiveText()) ?: return@put call.respondBadRequest("records must contain userId and present or absent status")
                if (!nocoDb.activeMemberIds(clubId).containsAll(updates.map { it.first }.toSet())) return@put call.respondForbidden()
                call.respondNoco {
                    updates.forEach { (userId, status) -> nocoDb.upsertAttendance(clubId, eventId, userId, status) }
                    "{\"updated\":${updates.size}}"
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
                if (!verifier.isStaff(identity)) return@get call.respondForbidden()
                call.respondNoco { nocoDb.listRecords(resource) }
            }
            get("/{resource}/{id}") {
                val identity = call.requireIdentity(verifier) ?: return@get
                val resource = call.parameters["resource"].orEmpty()
                if (!verifier.isStaff(identity)) return@get call.respondForbidden()
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

        /**
         * Receives a Google Apps Script form-submit relay. The script must send
         * `Authorization: Bearer <FORM_INGEST_SECRET>` and JSON with a NocoDB club_id,
         * title, message_body, and optional links.
         */
        post("/integrations/forms/announcements") {
            val secret = environment("FORM_INGEST_SECRET").orEmpty()
            val supplied = call.request.headers[HttpHeaders.Authorization]
                ?.removePrefix("Bearer ")?.trim().orEmpty()
            if (secret.isBlank() || supplied != secret) {
                return@post call.respondText("""{"success":false,"error":"Unauthorized form relay"}""", ContentType.Application.Json, HttpStatusCode.Unauthorized)
            }
            val payload = try { JsonParser.parseString(call.receiveText()).asJsonObject } catch (_: Exception) {
                return@post call.respondBadRequest("Form payload must be a JSON object")
            }
            val clubId = payload.string("club_id", "clubId")
                ?: return@post call.respondBadRequest("club_id is required")
            val content = listOfNotNull(payload.string("message_body", "messageBody", "content"), payload.string("links"))
                .joinToString("\n\n").trim()
            if (content.isBlank()) return@post call.respondBadRequest("message_body or links is required")
            val announcement = JsonObject().apply {
                addProperty("club_id", clubId)
                addProperty("title", payload.string("title") ?: "Club announcement")
                addProperty("content", content)
                addProperty("author_name", payload.string("author_name", "authorName") ?: "Club staff")
                addProperty("is_active", true)
            }
            call.respondNoco(HttpStatusCode.Created) { nocoDb.createRecord("announcements", announcement.toString()) }
        }
    }
}

private val studentOwnedResources = setOf("events", "announcements")
private val clubManagedResources = setOf("memberships", "events", "announcements", "attendance")

private fun configuredWebHost(): String? = environment("WEB_ALLOWED_HOST")
    ?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?.let { raw ->
        val host = if ("://" in raw) URI(raw).host else raw
        require(!host.isNullOrBlank() && host.matches(Regex("(?:\\*\\.)?[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*"))) {
            "WEB_ALLOWED_HOST must be a hostname, optionally prefixed with *."
        }
        host.lowercase()
    }

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

private fun parseAttendanceUpdates(body: String): List<Pair<String, String>>? = runCatching {
    val records = JsonParser.parseString(body).asJsonObject.getAsJsonArray("records") ?: return null
    records.map { entry ->
        val record = entry.asJsonObject
        val userId = record.get("userId")?.asString?.takeIf { it.matches(Regex("[A-Za-z0-9_-]{1,128}")) }
            ?: error("Invalid userId")
        val status = record.get("status")?.asString?.lowercase() ?: error("Invalid status")
        require(status in setOf("present", "absent")) { "Invalid status" }
        userId to status
    }
}.getOrNull()

private fun String.withClubId(clubId: String): String? {
    val body = trim()
    if (!body.startsWith("{") || !body.endsWith("}") || Regex("\\\"club(_id|Id)\\\"").containsMatchIn(body)) return null
    return body.removeSuffix("}").trimEnd().let { prefix ->
        if (prefix == "{") """{"club_id":"$clubId"}""" else "$prefix,\"club_id\":\"$clubId\"}"
    }
}

private fun JsonObject.string(vararg names: String): String? = names.firstNotNullOfOrNull { name ->
    get(name)?.takeIf { !it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }
}
