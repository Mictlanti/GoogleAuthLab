package com.example.googleauthlab

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.googleauthlab.ui.features.login.LoginViewModel
import com.example.googleauthlab.ui.navigation.Navigation
import com.example.googleauthlab.ui.theme.GoogleAuthLabTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {

            val loginViewModel : LoginViewModel = hiltViewModel()

            GoogleAuthLabTheme {
                Navigation(loginViewModel)
            }
        }
    }
}