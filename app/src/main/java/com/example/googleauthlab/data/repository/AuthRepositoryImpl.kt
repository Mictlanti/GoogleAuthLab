package com.example.googleauthlab.data.repository

import com.example.googleauthlab.data.mapper.toDomain
import com.example.googleauthlab.data.remote.auth.AuthManager
import com.example.googleauthlab.data.remote.auth.AuthRes
import com.example.googleauthlab.domain.model.UserProfile
import com.example.googleauthlab.domain.repository.AuthRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthRepositoryImpl @Inject constructor(
    private val authManager: AuthManager
) : AuthRepository {

    override val currentUser : Flow<UserProfile?> =
        authManager.observeAuthState()
            .map { firebaseUser ->
                firebaseUser?.toDomain()
            }

    override suspend fun signInWithGoogle(idToken: String): AuthRes<UserProfile> =
        when (val res = authManager.signInWithGoogle(idToken = idToken)) {
            is AuthRes.Success -> {
                AuthRes.Success(res.data.toDomain())
            }

            is AuthRes.Error -> {
                AuthRes.Error(res.errorMsg)
            }
        }


    override suspend fun createAnonymouslyAccount(): AuthRes<UserProfile> =
        when (val res = authManager.createAnonymouslyAccount()) {
            is AuthRes.Success -> AuthRes.Success(res.data.toDomain())

            is AuthRes.Error -> AuthRes.Error(res.errorMsg)

        }


    override suspend fun signOut() {
        authManager.signOut()
    }

}