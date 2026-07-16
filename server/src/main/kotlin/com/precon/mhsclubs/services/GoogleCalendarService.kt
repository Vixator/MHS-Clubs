package com.precon.mhsclubs.services

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.Event as CalendarEvent
import com.google.api.services.calendar.model.EventDateTime
import com.google.auth.http.HttpCredentialsAdapter
import com.google.auth.oauth2.GoogleCredentials
import com.precon.mhsclubs.model.Event
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.util.*

/**
 * Service for interacting with Google Calendar API.
 *
 * Provides functionality to:
 * - Create events in Google Calendar
 * - Sync events between the app and Google Calendar
 * - Manage OAuth tokens for calendar access
 *
 * Configuration:
 * - GOOGLE_CALENDAR_CREDENTIALS: Path to the service account JSON key file
 * - GOOGLE_CALENDAR_ID: The ID of the calendar to use (optional, defaults to primary)
 */
class GoogleCalendarService {
    companion object {
        private val log = LoggerFactory.getLogger(GoogleCalendarService::class.java)
        
        private const val APPLICATION_NAME = "MHS-Clubs"
        private const val CALENDAR_ID_ENV = "GOOGLE_CALENDAR_ID"
        private const val CREDENTIALS_ENV = "GOOGLE_CALENDAR_CREDENTIALS"
    }

    private val calendar: Calendar?
    private val calendarId: String

    /**
     * Creates a GoogleCalendarService with configuration from environment variables.
     */
    constructor() {
        val credentialsPath = System.getenv(CREDENTIALS_ENV)
            ?: System.getProperty("google.calendar.credentials")
        
        calendarId = System.getenv(CALENDAR_ID_ENV)
            ?: System.getProperty("google.calendar.id")
            ?: "primary"

        calendar = if (credentialsPath != null && File(credentialsPath).exists()) {
            createCalendarService(credentialsPath)
        } else {
            log.warn(
                "GOOGLE_CALENDAR_CREDENTIALS not set or file not found. " +
                "Google Calendar integration will be disabled."
            )
            null
        }
    }

    /**
     * Creates a GoogleCalendarService with explicit configuration.
     *
     * @param credentialsPath Path to the service account JSON key file
     * @param calendarId The ID of the calendar to use
     */
    constructor(credentialsPath: String, calendarId: String = "primary") {
        this.calendarId = calendarId
        this.calendar = createCalendarService(credentialsPath)
    }

    /**
     * Checks if the service is configured and ready to use.
     */
    fun isConfigured(): Boolean {
        return calendar != null
    }

    /**
     * Creates a new event in Google Calendar.
     *
     * @param event The event to create
     * @param sendNotifications Whether to send notifications to attendees
     * @return The created CalendarEvent, or null if not configured
     */
    fun createEvent(event: Event, sendNotifications: Boolean = false): CalendarEvent? {
        if (calendar == null) {
            log.warn("Google Calendar service not configured")
            return null
        }

        return try {
            val calendarEvent = CalendarEvent().apply {
                summary = event.title
                description = event.description
                location = event.location
                
                // Set start and end times
                start = EventDateTime().apply {
                    dateTime = com.google.api.client.util.DateTime(event.startTime.toString())
                    timeZone = TimeZone.getDefault().id
                }
                
                if (event.endTime != null) {
                    end = EventDateTime().apply {
                        dateTime = com.google.api.client.util.DateTime(event.endTime.toString())
                        timeZone = TimeZone.getDefault().id
                    }
                }
                
                // Mark as full-day if no time specified
                if (event.endTime == null) {
                    start.date = com.google.api.client.util.DateTime(event.startTime.toString())
                    end = EventDateTime().apply {
                        date = com.google.api.client.util.DateTime(event.startTime.toString())
                    }
                }
            }

            val request = calendar.events().insert(calendarId, calendarEvent)
            request.sendNotifications = sendNotifications
            request.execute()
        } catch (e: Exception) {
            log.error("Failed to create calendar event: ${e.message}", e)
            null
        }
    }

    /**
     * Updates an existing event in Google Calendar.
     *
     * @param eventId The Google Calendar event ID
     * @param event The updated event data
     * @return The updated CalendarEvent, or null if not configured or event not found
     */
    fun updateEvent(eventId: String, event: Event): CalendarEvent? {
        if (calendar == null) {
            log.warn("Google Calendar service not configured")
            return null
        }

        return try {
            val calendarEvent = CalendarEvent().apply {
                summary = event.title
                description = event.description
                location = event.location
                
                start = EventDateTime().apply {
                    dateTime = com.google.api.client.util.DateTime(event.startTime.toString())
                    timeZone = TimeZone.getDefault().id
                }
                
                if (event.endTime != null) {
                    end = EventDateTime().apply {
                        dateTime = com.google.api.client.util.DateTime(event.endTime.toString())
                        timeZone = TimeZone.getDefault().id
                    }
                }
            }

            val request = calendar.events().update(calendarId, eventId, calendarEvent)
            request.execute()
        } catch (e: Exception) {
            log.error("Failed to update calendar event: ${e.message}", e)
            null
        }
    }

    /**
     * Deletes an event from Google Calendar.
     *
     * @param eventId The Google Calendar event ID
     * @return true if successful, false otherwise
     */
    fun deleteEvent(eventId: String): Boolean {
        if (calendar == null) {
            log.warn("Google Calendar service not configured")
            return false
        }

        return try {
            calendar.events().delete(calendarId, eventId).execute()
            true
        } catch (e: Exception) {
            log.error("Failed to delete calendar event: ${e.message}", e)
            false
        }
    }

    /**
     * Syncs an event to Google Calendar and returns the calendar event ID.
     *
     * @param event The event to sync
     * @return The Google Calendar event ID, or null if sync failed
     */
    fun syncEvent(event: Event): String? {
        if (calendar == null) {
            log.warn("Google Calendar service not configured")
            return null
        }

        return try {
            val calendarEvent = createEvent(event) ?: return null
            calendarEvent.id
        } catch (e: Exception) {
            log.error("Failed to sync event to calendar: ${e.message}", e)
            null
        }
    }

    /**
     * Creates a configured Calendar service using service account credentials.
     */
    private fun createCalendarService(credentialsPath: String): Calendar {
        val credentials = GoogleCredentials.fromStream(FileInputStream(credentialsPath))
            .createScoped(listOf(CalendarScopes.CALENDAR))

        return Calendar.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(credentials)
        ).setApplicationName(APPLICATION_NAME)
            .build()
    }
}
