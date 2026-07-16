package com.precon.mhsclubs.firebase

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream

/**
 * Firebase configuration utility for the MHS Clubs server.
 *
 * Initializes the Firebase Admin SDK with service account credentials.
 * The service account JSON file path can be configured via:
 * - FIREBASE_SERVICE_ACCOUNT environment variable
 * - firebase.serviceAccount system property
 * - Default: looks for firebase-service-account.json in the working directory
 *
 * In development mode (no service account file found), Firebase will not be
 * initialized and token verification will be skipped (development mode).
 */
object FirebaseConfig {
    private val log = LoggerFactory.getLogger(FirebaseConfig::class.java)
    
    /**
     * Path to the Firebase service account JSON file.
     */
    val serviceAccountPath: String?
        get() = System.getenv("FIREBASE_SERVICE_ACCOUNT")
            ?: System.getProperty("firebase.serviceAccount")
            ?: null

    /**
     * Firebase project ID from environment or properties.
     */
    val projectId: String
        get() = System.getenv("FIREBASE_PROJECT_ID")
            ?: System.getProperty("firebase.projectId")
            ?: ""

    /**
     * Initializes Firebase Admin SDK if a service account file is available.
     * Must be called before any Firebase operations (e.g., token verification).
     *
     * In production, this should be called during application startup.
     * In development, if no service account is found, a warning is logged and
     * Firebase operations will run in development mode (no verification).
     *
     * @return true if Firebase was successfully initialized, false otherwise
     */
    fun initialize(): Boolean {
        // Check if already initialized
        if (FirebaseApp.getApps().isNotEmpty()) {
            log.info("Firebase Admin SDK already initialized")
            return true
        }

        val serviceAccount = serviceAccountPath
        
        if (serviceAccount == null || !File(serviceAccount).exists()) {
            log.warn(
                "Firebase service account file not found. " +
                "Set FIREBASE_SERVICE_ACCOUNT environment variable or " +
                "firebase.serviceAccount system property to the path of your " +
                "Firebase service account JSON file. " +
                "Running in development mode (token verification skipped)."
            )
            return false
        }

        return try {
            val credentials = GoogleCredentials.fromStream(FileInputStream(serviceAccount))
            val options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build()
            
            FirebaseApp.initializeApp(options)
            log.info("Firebase Admin SDK initialized successfully for project: $projectId")
            true
        } catch (e: Exception) {
            log.error("Failed to initialize Firebase Admin SDK: ${e.message}", e)
            false
        }
    }

    /**
     * Returns the FirebaseAuth instance.
     * In development mode (Firebase not initialized), this returns null.
     */
    fun getFirebaseAuth(): FirebaseAuth? {
        return if (FirebaseApp.getApps().isNotEmpty()) {
            FirebaseAuth.getInstance()
        } else {
            null
        }
    }

    /**
     * Checks if Firebase Admin SDK is initialized and ready for use.
     */
    fun isInitialized(): Boolean {
        return FirebaseApp.getApps().isNotEmpty()
    }
}
