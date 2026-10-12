package com.example.googleauthlab.data.mapper

import com.example.googleauthlab.domain.model.UserProfile
import com.google.firebase.auth.FirebaseUser

fun FirebaseUser.toDomain() = UserProfile(
    id = this.uid,
    username = this.displayName.orEmpty(),
    email = this.email,
    photoUrl = this.photoUrl?.toString(),
    isAnonymously = this.isAnonymous
)