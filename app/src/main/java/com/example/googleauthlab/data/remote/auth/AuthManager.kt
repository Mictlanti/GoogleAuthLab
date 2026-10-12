package com.example.googleauthlab.data.remote.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthManager @Inject constructor(private val firebaseAuth: FirebaseAuth)  {

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser)
        }

        firebaseAuth.addAuthStateListener(listener)

        trySend(firebaseAuth.currentUser)

        awaitClose {
            firebaseAuth.removeAuthStateListener(listener)
        }
    }

    suspend fun signInWithGoogle(
        idToken: String
    ): AuthRes<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(
                idToken,
                null
            )

            val authResult = firebaseAuth
                .signInWithCredential(credential)
                .await()

            val user = authResult.user
                ?: return AuthRes.Error(
                    "Firebase no devolvió un usuario autenticado."
                )

            AuthRes.Success(user)
        } catch (e: Exception) {
            AuthRes.Error(
                e.message ?: "No se pudo iniciar sesión con Firebase."
            )
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun createAnonymouslyAccount() : AuthRes<FirebaseUser>{
        return try {
            val authRes = firebaseAuth.signInAnonymously().await()

            val user = authRes.user
                ?: return AuthRes.Error(
                    "Fireabse not return user"
                )

            AuthRes.Success(user)

        } catch (e: Exception) {
            AuthRes.Error("Error with log in anonymously")
        }
    }


}