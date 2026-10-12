package com.example.googleauthlab.domain.model

data class UserProfile(
    val id: String,
    val username: String?,
    val email: String?,
    val photoUrl: String?,
    val isAnonymously: Boolean
)
