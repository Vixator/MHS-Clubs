package com.precon.mhsclubs.auth

import com.precon.mhsclubs.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Authentication service interface for the MHS Clubs app.
 *
 * Provides a platform-agnostic API for:
 * - Signing in with Google (via Firebase)
 * - Signing out
 * - Accessing the current user
 * - Checking authentication state
 *
 * Implementations should be provided for each platform (Android, iOS, Web).
 */
interface AuthService {
    
    /**
     * Flow of the current authentication state.
     */
    val authState: Flow<AuthState>

    /**
     * The current user, or null if not signed in.
     */
    val currentUser: User?

    /**
     * Returns true if the user is currently signed in.
     */
    val isSignedIn: Boolean

    /**
     * Signs in with Google using Firebase Authentication.
     *
     * On Android, this typically launches a Google Sign-In activity.
     * On iOS, this uses the Firebase Auth SDK.
     * On Web, this redirects to Google OAuth.
     *
     * @return Result containing the signed-in user or an error
     */
    suspend fun signInWithGoogle(): Result<User>

    /**
     * Signs out the current user.
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Refreshes the current user's data from Firebase.
     */
    suspend fun refreshUser(): Result<User?>

    /**
     * Gets the Firebase ID token for the current user.
     * This can be used to authenticate API requests.
     *
     * @return The ID token string, or null if not signed in
     */
    suspend fun getIdToken(): String?
}

/**
 * Authentication state for the app.
 */
sealed class AuthState {
    /**
     * User is not signed in.
     */
    object SignedOut : AuthState()

    /**
     * User is signed in.
     */
    data class SignedIn(val user: User) : AuthState()

    /**
     * Authentication is in progress.
     */
    object Loading : AuthState()

    /**
     * An error occurred during authentication.
     */
    data class Error(val message: String, val exception: Throwable? = null) : AuthState()
}

/**
 * Default implementation of AuthService for platforms that don't have Firebase.
 * Useful for testing or platforms without Firebase support.
 */
class DefaultAuthService : AuthService {
    
    private val _authState = kotlinx.coroutines.flow.MutableStateFlow<AuthState>(AuthState.SignedOut)
    override val authState = _authState.asStateFlow()
    
    override val currentUser: User? = null
    override val isSignedIn: Boolean = false
    
    override suspend fun signInWithGoogle(): Result<User> {
        return Result.failure(UnsupportedOperationException("Google Sign-In not supported"))
    }
    
    override suspend fun signOut(): Result<Unit> {
        return Result.success(Unit)
    }
    
    override suspend fun refreshUser(): Result<User?> {
        return Result.success(null)
    }
    
    override suspend fun getIdToken(): String? {
        return null
    }
}

/**
 * Factory function to create an AuthService instance.
 * Each platform provides its own implementation.
 */
expect fun createAuthService(): AuthService
