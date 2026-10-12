package com.example.googleauthlab.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface AppRoute {

    @Serializable data object Login : AppRoute
    @Serializable data object Profile : AppRoute

}