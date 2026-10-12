package com.example.googleauthlab.domain.repository

import android.content.Context
import com.example.googleauthlab.data.remote.auth.AuthRes
import com.example.googleauthlab.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface AuthRepository {

    val currentUser: Flow<UserProfile?>

    suspend fun signInWithGoogle(idToken: String) : AuthRes<UserProfile>

    suspend fun createAnonymouslyAccount() : AuthRes<UserProfile>

    suspend fun signOut()

}