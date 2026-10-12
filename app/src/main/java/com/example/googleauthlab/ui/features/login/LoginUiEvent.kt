package com.example.googleauthlab.ui.features.login

sealed interface LoginUiEvent {

    data object NavigateToHome : LoginUiEvent

    data class ShowError(
        val msg: String
    ) : LoginUiEvent

}