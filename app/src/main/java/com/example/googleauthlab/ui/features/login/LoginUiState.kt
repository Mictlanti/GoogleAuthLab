package com.example.googleauthlab.ui.features.login

import com.example.googleauthlab.domain.model.UserProfile

data class LoginUiState(
    val isCheckingSession: Boolean = true,
    val isLoading: Boolean = false,
    val user: UserProfile? = null,
    val errorMsg: String? = null
)
