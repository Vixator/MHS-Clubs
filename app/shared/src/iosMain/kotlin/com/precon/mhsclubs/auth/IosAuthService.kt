package com.precon.mhsclubs.auth

import com.precon.mhsclubs.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSNotificationCenter

private const val SIGN_IN_NOTIFICATION = "MHSClubsStartGoogleSignIn"
private const val SIGN_OUT_NOTIFICATION = "MHSClubsSignOut"

/** iOS side of the Firebase bridge. Swift owns the native Google presentation UI. */
class IosAuthService : AuthService {
    private val state = MutableStateFlow<AuthState>(AuthState.SignedOut)
    private var user: User? = null
    private var token: String? = null

    override val authState: Flow<AuthState> = state.asStateFlow()
    override val currentUser: User? get() = user
    override val isSignedIn: Boolean get() = user != null

    override suspend fun signInWithGoogle(): Result<User> {
        state.value = AuthState.Loading
        NSNotificationCenter.defaultCenter.postNotificationName(SIGN_IN_NOTIFICATION, null)
        return Result.failure(SignInPendingException())
    }

    override suspend fun signOut(): Result<Unit> {
        NSNotificationCenter.defaultCenter.postNotificationName(SIGN_OUT_NOTIFICATION, null)
        signedOut()
        return Result.success(Unit)
    }

    override suspend fun refreshUser(): Result<User?> = Result.success(user)
    override suspend fun getIdToken(): String? = token

    fun completeSignIn(uid: String, email: String, displayName: String?, avatarUrl: String?, idToken: String) {
        try {
            User.fromFirebase(uid, email, displayName.orEmpty(), avatarUrl).also {
                user = it
                token = idToken
                state.value = AuthState.SignedIn(it)
            }
        } catch (error: IllegalArgumentException) {
            failed("MCPASD school email required")
        }
    }

    fun failed(message: String) { state.value = AuthState.Error(message) }
    fun signedOut() { user = null; token = null; state.value = AuthState.SignedOut }
}

/** Stable object Swift calls after native Firebase and Google Sign-In complete. */
object IosFirebaseAuthBridge {
    private val service = IosAuthService()
    fun service(): AuthService = service
    fun completeSignIn(uid: String, email: String, displayName: String?, avatarUrl: String?, idToken: String) =
        service.completeSignIn(uid, email, displayName, avatarUrl, idToken)
    fun failed(message: String) = service.failed(message)
    fun signedOut() = service.signedOut()
}

actual fun createAuthService(): AuthService = IosFirebaseAuthBridge.service()
