package com.example.googleauthlab.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.googleauthlab.ui.features.login.LoginViewModel
import com.example.googleauthlab.ui.features.login.LoginViewRoute
import com.example.googleauthlab.ui.features.profile.ProfileViewRoute
import kotlin.math.log

@Composable
fun Navigation(loginViewModel: LoginViewModel) {

    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    if(uiState.isCheckingSession) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        key(uiState.user != null) {
            val navController = rememberNavController()

            NavHost(
                navController = navController,
                startDestination = if(uiState.user != null) AppRoute.Profile else AppRoute.Login
            ) {
                composable<AppRoute.Login> {
                    LoginViewRoute(loginViewModel) {
                        navController.navigate(AppRoute.Profile)
                    }
                }

                composable<AppRoute.Profile> {
                    ProfileViewRoute(loginViewModel)
                }
            }

        }
    }


}