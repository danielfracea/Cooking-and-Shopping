package com.cooking.app.ui.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cooking.app.data.repository.AppRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Replace with your actual Web Client ID from google-services.json (OAuth 2.0 Web Client ID).
// Found in Firebase Console → Authentication → Sign-in method → Google → Web SDK configuration.
// The app will throw an error at sign-in time if this is not configured.
private const val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID_HERE"

sealed class AuthState {
    data object Loading : AuthState()
    data class Authenticated(val user: FirebaseUser) : AuthState()
    data class Guest(val uid: String, val displayName: String = "Guest") : AuthState()
    data object Unauthenticated : AuthState()
}

class AuthViewModel(private val repository: AppRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.authState.collect { user ->
                _authState.value = if (user != null) {
                    AuthState.Authenticated(user)
                } else if (_authState.value is AuthState.Guest) {
                    _authState.value // keep guest state
                } else {
                    AuthState.Unauthenticated
                }
            }
        }
    }

    fun continueAsGuest() {
        val guestId = "guest-${java.util.UUID.randomUUID()}"
        _authState.value = AuthState.Guest(uid = guestId)
    }

    fun signInWithGoogle(activity: Activity, onError: (String) -> Unit) {
        if (WEB_CLIENT_ID == "YOUR_WEB_CLIENT_ID_HERE") {
            onError("WEB_CLIENT_ID is not configured. See AuthViewModel.kt setup instructions.")
            return
        }
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(activity)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(WEB_CLIENT_ID)
                    .build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()
                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
                auth.signInWithCredential(firebaseCredential).await()
            } catch (e: Exception) {
                onError(e.message ?: "Sign in failed")
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            if (_authState.value is AuthState.Guest) {
                _authState.value = AuthState.Unauthenticated
            } else {
                repository.signOut()
                _authState.value = AuthState.Unauthenticated
            }
        }
    }

    val currentUserId: String?
        get() = when (val s = _authState.value) {
            is AuthState.Authenticated -> s.user.uid
            is AuthState.Guest -> s.uid
            else -> null
        }

    val isGuest: Boolean get() = _authState.value is AuthState.Guest

    class Factory(private val repository: AppRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(repository) as T
    }
}
