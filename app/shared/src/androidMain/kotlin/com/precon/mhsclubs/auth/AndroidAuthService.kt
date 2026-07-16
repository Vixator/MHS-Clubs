package com.precon.mhsclubs.auth

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.precon.mhsclubs.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * Android implementation of AuthService using Firebase Authentication.
 *
 * Handles Google Sign-In with Firebase and provides the current authentication state.
 * The service automatically listens to Firebase auth state changes.
 *
 * @param context The Android application context
 */
class AndroidAuthService(private val context: Context) : AuthService {
    
    private val auth: FirebaseAuth = Firebase.auth
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    override val authState: Flow<AuthState> = _authState.asStateFlow()
    
    private var googleSignInClient: GoogleSignInClient? = null
    
    // Scope for auth state listeners
    private val authScope = CoroutineScope(Dispatchers.Main)
    
    // Auth state listener registration
    private var authStateListener: FirebaseAuth.AuthStateListener? = null
    
    init {
        // Configure Google Sign-In
        configureGoogleSignIn()
        
        // Set up Firebase auth state listener
        setupAuthStateListener()
    }
    
    /**
     * Configures Google Sign-In options.
     * Requests email, profile, and ID token for Firebase.
     * Also requests Google Calendar scope for event sync.
     */
    private fun configureGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.precon.mhsclubs.R.string.default_web_client_id))
            .requestEmail()
            .requestProfile()
            .requestServerAuthCode(context.getString(com.precon.mhsclubs.R.string.default_web_client_id))
            .requestScopes(
                com.google.android.gms.common.api.Scope("https://www.googleapis.com/auth/calendar.events")
            )
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }
    
    /**
     * Sets up a listener for Firebase auth state changes.
     */
    private fun setupAuthStateListener() {
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            authScope.launch {
                updateAuthState(firebaseAuth)
            }
        }
        auth.addAuthStateListener(authStateListener!!)
    }
    
    /**
     * Updates the auth state based on the current Firebase user.
     */
    private suspend fun updateAuthState(firebaseAuth: FirebaseAuth) {
        val firebaseUser = firebaseAuth.currentUser
        
        if (firebaseUser != null) {
            try {
                val user = User.fromFirebase(
                    firebaseUid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    avatarUrl = firebaseUser.photoUrl?.toString()
                )
                _authState.value = AuthState.SignedIn(user)
            } catch (e: IllegalArgumentException) {
                // Invalid email domain
                _authState.value = AuthState.Error(
                    message = "Invalid email domain: ${e.message}",
                    exception = e
                )
                // Sign out the invalid user
                firebaseAuth.signOut()
            }
        } else {
            _authState.value = AuthState.SignedOut
        }
    }
    
    override val currentUser: User?
        get() {
            val firebaseUser = auth.currentUser ?: return null
            return try {
                User.fromFirebase(
                    firebaseUid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    avatarUrl = firebaseUser.photoUrl?.toString()
                )
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    
    override val isSignedIn: Boolean
        get() = auth.currentUser != null
    
    /**
     * Signs in with Google using Firebase Authentication.
     * Launches the Google Sign-In activity.
     *
     * @return Result containing the signed-in user or an error
     */
    override suspend fun signInWithGoogle(): Result<User> {
        return Result.failure(UnsupportedOperationException(
            "signInWithGoogle must be called from an Activity context. " +
            "Use launchGoogleSignIn() from an Activity."
        ))
    }
    
    /**
     * Launches the Google Sign-In flow from an Activity.
     * This is the recommended way to sign in on Android.
     *
     * @param activity The activity to launch the sign-in flow from
     * @param requestCode The request code to use for the activity result
     */
    fun launchGoogleSignIn(activity: android.app.Activity, requestCode: Int) {
        googleSignInClient?.signOut()?.addOnCompleteListener {
            val signInIntent = googleSignInClient?.signInIntent
            activity.startActivityForResult(signInIntent, requestCode)
        }
    }
    
    /**
     * Handles the result of the Google Sign-In activity.
     *
     * @param requestCode The request code used when launching the sign-in
     * @param resultCode The result code from the activity
     * @param data The intent data from the activity result
     * @return Result containing the signed-in user or an error
     */
    suspend fun handleSignInResult(
        requestCode: Int,
        resultCode: Int,
        data: android.content.Intent?
    ): Result<User> {
        return try {
            if (requestCode != 1001) { // Use a constant for the request code
                return Result.failure(IllegalArgumentException("Invalid request code"))
            }
            
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.await()
            
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            
            val firebaseUser = authResult.user ?: 
                return Result.failure(IllegalStateException("No user after sign-in"))
            
            val user = User.fromFirebase(
                firebaseUid = firebaseUser.uid,
                email = firebaseUser.email ?: "",
                displayName = firebaseUser.displayName ?: "",
                avatarUrl = firebaseUser.photoUrl?.toString()
            )
            
            Result.success(user)
        } catch (e: ApiException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun signOut(): Result<Unit> {
        return try {
            googleSignInClient?.signOut()?.await()
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun refreshUser(): Result<User?> {
        return try {
            auth.currentUser?.reload()?.await()
            val firebaseUser = auth.currentUser
            
            if (firebaseUser != null) {
                val user = User.fromFirebase(
                    firebaseUid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    avatarUrl = firebaseUser.photoUrl?.toString()
                )
                Result.success(user)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getIdToken(): String? {
        return try {
            auth.currentUser?.getIdToken(false)?.await()?.token
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Cleans up resources when the service is no longer needed.
     */
    fun cleanup() {
        authStateListener?.let { auth.removeAuthStateListener(it) }
        authStateListener = null
        authScope.cancel()
    }
}

/**
 * Creates an Android-specific AuthService with a context.
 */
fun createAndroidAuthService(context: Context): AuthService {
    return AndroidAuthService(context)
}
