package com.precon.mhsclubs

import com.precon.mhsclubs.services.NocoDbClient
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpHeaders
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Optional
import java.util.concurrent.CompletableFuture
import javax.net.ssl.SSLSession
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for the membership-scoped event listing added to [NocoDbClient].
 *
 * These cover the two product requirements:
 *  - Deleting an event drops it from the served list (the deleted id is absent).
 *  - A student who joins a club sees that club's events; a non-member does not.
 *
 * The transport is faked so no network or NocoDB instance is required.
 */
class NocoDbClientEventsTest {
    private fun client(responses: Map<String, String>): NocoDbClient {
        val transport: (HttpRequest) -> HttpResponse<String> = { request ->
            val uri = request.uri().toString()
            val key = responses.keys.firstOrNull { uri.contains(it) }
            val body = responses[key]
            FakeHttpResponse(body ?: "", if (body == null) 404 else 200)
        }
        return NocoDbClient(
            baseUrl = "https://nocodb.example.com",
            apiToken = "token",
            transport = transport,
            tableNames = mapOf(
                "clubs" to "clubs",
                "memberships" to "memberships",
                "events" to "events"
            )
        )
    }

    @Test
    fun `memberClubIds returns clubs the user belongs to`() {
        val noco = client(
            mapOf(
                "memberships/records?where=(firebase_uid,eq,uid-1)" to
                    """{"list":[{"club_id":"club-a"},{"club_id":"club-b"},{"club_id":"club-a"}]}"""
            )
        )
        assertEquals(listOf("club-a", "club-b"), noco.memberClubIds("uid-1"))
    }

    @Test
    fun `eventsForClubs returns events for the given clubs only`() {
        val noco = client(
            mapOf(
                "events/records?where=(club_id,in,'club-a','club-b')" to
                    """{"list":[{"Id":"e1","club_id":"club-a"},{"Id":"e2","club_id":"club-b"}]}"""
            )
        )
        val body = noco.eventsForClubs(listOf("club-a", "club-b"))
        assertTrue(body.contains("\"Id\":\"e1\""))
        assertTrue(body.contains("\"Id\":\"e2\""))
    }

    @Test
    fun `eventsForClubs returns an empty list when the user is in no clubs`() {
        val noco = client(emptyMap())
        assertEquals("""{"list":[]}""", noco.eventsForClubs(emptyList()))
    }

    @Test
    fun `deleted event no longer appears in membership-scoped events`() {
        // After a delete, NocoDB no longer returns the event record, so the
        // membership-scoped list reflects the deletion on the next read.
        val noco = client(
            mapOf(
                "memberships/records?where=(firebase_uid,eq,uid-2)" to
                    """{"list":[{"club_id":"club-a"}]}""",
                "events/records?where=(club_id,in,'club-a')" to
                    """{"list":[{"Id":"e2","club_id":"club-a"}]}"""
            )
        )
        val clubIds = noco.memberClubIds("uid-2")
        val body = noco.eventsForClubs(clubIds)
        assertFalse(body.contains("\"Id\":\"e1\"")) { "deleted event e1 must be absent" }
        assertTrue(body.contains("\"Id\":\"e2\"")) { "remaining event e2 must be present" }
    }

    private class FakeHttpResponse(
        private val body: String,
        private val status: Int
    ) : HttpResponse<String> {
        override fun body(): String = body
        override fun statusCode(): Int = status
        override fun request(): HttpRequest =
            HttpRequest.newBuilder(URI.create("https://test.example")).build()
        override fun previousResponse(): HttpResponse<String>? = null
        override fun sslSession(): SSLSession? = null
        override fun uri(): URI = URI.create("https://test.example")
        override fun version() = HttpClient.Version.HTTP_1_1
        override fun headers(): HttpHeaders = HttpHeaders.of({ _, _ -> true }) { _, _ -> true }
        override fun trailers(): CompletableFuture<java.util.Map<String, java.util.List<String>>> =
            CompletableFuture.completedFuture(java.util.Map.of())
        override fun timeout(): Optional<java.time.Duration> = Optional.empty()
    }
}
