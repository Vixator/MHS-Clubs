package com.precon.mhsclubs.auth

import com.precon.mhsclubs.model.User
import kotlinx.coroutines.await
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.js.Promise

/** Firebase Web Auth implementation backed by the bundled browser module. */
class JsAuthService : AuthService {
    private val state = MutableStateFlow<AuthState>(AuthState.SignedOut)
    private var user: User? = null

    override val authState: Flow<AuthState> = state.asStateFlow()
    override val currentUser: User? get() = user
    override val isSignedIn: Boolean get() = user != null

    override suspend fun signInWithGoogle(): Result<User> = runCatching {
        state.value = AuthState.Loading
        val record = bridge().signInWithGoogle().unsafeCast<Promise<dynamic>>().await()
        toUser(record).also { signedInUser -> user = signedInUser; state.value = AuthState.SignedIn(signedInUser) }
    }.onFailure { error -> state.value = AuthState.Error("Google sign-in failed: ${error.message}", error) }

    override suspend fun signOut(): Result<Unit> = runCatching {
        bridge().signOut().unsafeCast<Promise<dynamic>>().await()
        user = null
        state.value = AuthState.SignedOut
    }.onFailure { error -> state.value = AuthState.Error("Sign-out failed: ${error.message}", error) }

    override suspend fun refreshUser(): Result<User?> = runCatching {
        val record = bridge().currentUser().unsafeCast<Promise<dynamic>>().await()
        if (record == null) null else toUser(record).also { signedInUser -> user = signedInUser; state.value = AuthState.SignedIn(signedInUser) }
    }

    override suspend fun getIdToken(): String? = runCatching {
        bridge().getIdToken().unsafeCast<Promise<dynamic>>().await() as? String
    }.getOrNull()

    private fun bridge(): dynamic = js("window.mhsFirebaseAuth")

    private fun toUser(record: dynamic): User = User.fromFirebase(
        firebaseUid = record.uid as String,
        email = record.email as String,
        displayName = (record.displayName as? String).orEmpty(),
        avatarUrl = record.photoUrl as? String
    )
}

actual fun createAuthService(): AuthService = JsAuthService()
