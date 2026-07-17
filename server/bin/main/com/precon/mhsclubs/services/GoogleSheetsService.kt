package com.precon.mhsclubs.services

import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.oauth2.ServiceAccountCredentials
import com.google.auth.oauth2.GoogleCredentials
import com.google.auth.http.HttpCredentialsAdapter
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.ValueRange
import com.precon.mhsclubs.model.Club
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream

/**
 * Service for reading club data from Google Sheets.
 *
 * Uses the Google Sheets API v4 to fetch club information from a configured
 * spreadsheet. The service account must have read access to the sheet.
 *
 * Configuration:
 * - GOOGLE_SHEETS_CREDENTIALS: Path to the service account JSON key file
 * - GOOGLE_SHEETS_ID: The ID of the Google Sheet (from the URL)
 * - GOOGLE_SHEETS_CLUBS_RANGE: The range containing club data (e.g., "Clubs!A:J")
 *
 * Sheet format (expected columns):
 * A: sheetSourceId (unique identifier)
 * B: name
 * C: description
 * D: logoUrl
 * E: category
 * F: meetingDay
 * G: meetingTime
 * H: meetingLocation
 * I: code (unique club code)
 */
class GoogleSheetsService {
    companion object {
        private val log = LoggerFactory.getLogger(GoogleSheetsService::class.java)
        
        /**
         * Environment variable names for configuration.
         */
        private const val CREDENTIALS_ENV = "GOOGLE_SHEETS_CREDENTIALS"
        private const val SHEET_ID_ENV = "GOOGLE_SHEETS_ID"
        private const val CLUBS_RANGE_ENV = "GOOGLE_SHEETS_CLUBS_RANGE"
        
        /**
         * Default range for club data.
         */
        private const val DEFAULT_CLUBS_RANGE = "Clubs!A:I"
    }

    private val sheets: Sheets?
    private val sheetId: String
    private val clubsRange: String

    /**
     * Creates a GoogleSheetsService with configuration from environment variables.
     *
     * @throws IllegalStateException if required configuration is missing
     */
    constructor() {
        val credentialsPath = System.getenv(CREDENTIALS_ENV)
            ?: System.getProperty("google.sheets.credentials")
        
        sheetId = System.getenv(SHEET_ID_ENV)
            ?: System.getProperty("google.sheets.id")
            ?: throw IllegalStateException(
                "GOOGLE_SHEETS_ID environment variable or google.sheets.id property not set"
            )
        
        clubsRange = System.getenv(CLUBS_RANGE_ENV)
            ?: System.getProperty("google.sheets.clubs.range")
            ?: DEFAULT_CLUBS_RANGE

        sheets = if (credentialsPath != null && File(credentialsPath).exists()) {
            createSheetsService(credentialsPath)
        } else {
            log.warn(
                "GOOGLE_SHEETS_CREDENTIALS not set or file not found. " +
                "Google Sheets integration will be disabled."
            )
            null
        }
    }

    /**
     * Creates a GoogleSheetsService with explicit configuration.
     *
     * @param credentialsPath Path to the service account JSON key file
     * @param sheetId The Google Sheet ID
     * @param clubsRange The range containing club data
     */
    constructor(credentialsPath: String, sheetId: String, clubsRange: String = DEFAULT_CLUBS_RANGE) {
        this.sheetId = sheetId
        this.clubsRange = clubsRange
        this.sheets = createSheetsService(credentialsPath)
    }

    /**
     * Checks if the service is configured and ready to use.
     */
    fun isConfigured(): Boolean {
        return sheets != null
    }

    /**
     * Fetches all clubs from the configured Google Sheet.
     *
     * @return List of Club objects parsed from the sheet
     * @throws IllegalStateException if the service is not configured
     * @throws Exception if the sheet cannot be read
     */
    suspend fun fetchClubs(): List<Club> {
        if (sheets == null) {
            throw IllegalStateException("Google Sheets service not configured")
        }

        return try {
            val request = sheets.spreadsheets().values().get(sheetId, clubsRange)
            val response = request.execute()
            parseClubsFromSheet(response)
        } catch (e: Exception) {
            log.error("Failed to fetch clubs from Google Sheets: ${e.message}", e)
            throw e
        }
    }

    /**
     * Fetches a single club by its sheetSourceId.
     *
     * @param sheetSourceId The unique identifier from the sheet
     * @return The Club object, or null if not found
     */
    suspend fun fetchClubBySheetId(sheetSourceId: String): Club? {
        return fetchClubs().find { it.sheetSourceId == sheetSourceId }
    }

    /**
     * Fetches a single club by its code.
     *
     * @param code The unique club code
     * @return The Club object, or null if not found
     */
    suspend fun fetchClubByCode(code: String): Club? {
        return fetchClubs().find { it.code.equals(code, ignoreCase = true) }
    }

    /**
     * Syncs clubs from Google Sheets to the database.
     * This is a placeholder for the actual sync logic that would:
     * 1. Fetch all clubs from the sheet
     * 2. Compare with existing clubs in the database
     * 3. Insert new clubs
     * 4. Update existing clubs
     * 5. Optionally archive clubs that no longer exist in the sheet
     *
     * @return Sync result with statistics
     */
    suspend fun syncClubs(): SyncResult {
        if (sheets == null) {
            return SyncResult(
                success = false,
                error = "Google Sheets service not configured"
            )
        }

        return try {
            val sheetClubs = fetchClubs()
            // TODO: Implement actual database sync logic
            // For now, just return success with the count
            SyncResult(
                success = true,
                fetched = sheetClubs.size,
                inserted = 0,
                updated = 0,
                archived = 0
            )
        } catch (e: Exception) {
            SyncResult(
                success = false,
                error = e.message ?: "Unknown error"
            )
        }
    }

    /**
     * Parses club data from a Google Sheets value range.
     *
     * Expected columns:
     * 0: sheetSourceId
     * 1: name
     * 2: description
     * 3: logoUrl
     * 4: category
     * 5: meetingDay
     * 6: meetingTime
     * 7: meetingLocation
     * 8: code
     */
    private fun parseClubsFromSheet(response: ValueRange): List<Club> {
        val values = response.values ?: return emptyList()
        
        // Skip header row if present
        val rows = if (values.isNotEmpty() && isHeaderRow(values[0])) {
            values.subList(1, values.size)
        } else {
            values
        }

        return rows.mapNotNull { row ->
            try {
                parseClubRow(row)
            } catch (e: Exception) {
                log.warn("Failed to parse club row: $row. Error: ${e.message}")
                null
            }
        }
    }

    /**
     * Checks if a row looks like a header row (first cell is a string that looks like a header).
     */
    private fun isHeaderRow(row: List<Any>): Boolean {
        if (row.isEmpty()) return false
        val firstCell = row[0].toString().lowercase()
        return firstCell.contains("id") || firstCell.contains("source") || firstCell.contains("sheet")
    }

    /**
     * Parses a single row from the sheet into a Club object.
     */
    private fun parseClubRow(row: List<Any>): Club {
        // Ensure we have at least the required columns
        require(row.size >= 9) { "Row must have at least 9 columns: $row" }

        fun getString(index: Int): String = row.getOrNull(index)?.toString() ?: ""

        return Club(
            sheetSourceId = getString(0),
            name = getString(1),
            description = getString(2),
            logoUrl = getString(3).ifEmpty { null },
            category = getString(4).ifEmpty { "General" },
            meetingDay = getString(5).ifEmpty { null },
            meetingTime = getString(6).ifEmpty { null },
            meetingLocation = getString(7).ifEmpty { null },
            code = getString(8)
        )
    }

    /**
     * Creates a configured Sheets service using service account credentials.
     */
    private fun createSheetsService(credentialsPath: String): Sheets {
        val credentials = GoogleCredentials.fromStream(FileInputStream(credentialsPath))
            .createScoped(listOf(SheetsScopes.SPREADSHEETS_READONLY))

        return Sheets.Builder(
            com.google.api.client.http.javanet.NetHttpTransport(),
            com.google.api.client.json.gson.GsonFactory.getDefaultInstance(),
            HttpCredentialsAdapter(credentials)
        ).setApplicationName("MHS-Clubs-Sheets-Importer")
            .build()
    }

    /**
     * Result of a sync operation.
     */
    data class SyncResult(
        val success: Boolean,
        val fetched: Int = 0,
        val inserted: Int = 0,
        val updated: Int = 0,
        val archived: Int = 0,
        val error: String? = null
    )
}
