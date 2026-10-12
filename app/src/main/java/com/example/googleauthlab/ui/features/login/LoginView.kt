package com.example.googleauthlab.ui.features.login

import android.transition.CircularPropagation
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Adb
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.googleauthlab.ui.components.textStyles.BodyLarge
import com.example.googleauthlab.ui.components.textStyles.BodyMedium

data class ProjectArchitecture(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val content: String
)

@Composable
fun LoginViewRoute(loginViewModel: LoginViewModel, navProfile: () -> Unit) {

    val context = LocalContext.current
    val event = loginViewModel.event
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(event) {
        event.collect { event ->
            when(event) {
                LoginUiEvent.NavigateToHome -> navProfile()

                is LoginUiEvent.ShowError -> {
                    Toast.makeText(context, event.msg, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomAppBar(containerColor = Color.Transparent) {
                ElevatedButton(
                    onClick = {
                        loginViewModel.signInWithGoogle(context = context)
                    },
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    enabled = !uiState.isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if(uiState.isLoading) {
                        CircularProgressIndicator()
                    } else {
                        BodyMedium(
                            text = "SignIn with Google",
                            color = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }
    ) { pad ->
        LoginView(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(15.dp)
        )
    }
}

@Composable
private fun LoginView(modifier: Modifier = Modifier) {

    val textContent = listOf(
        ProjectArchitecture(
            Icons.Default.Smartphone,
            "Presentation",
            "Compose - ViewModel - UiState",
            "LoginView - ProfileView"
        ),
        ProjectArchitecture(
            Icons.Default.Adb,
            "Domain",
            "Models - Repositories (interfaces) - Use Cases",
            "UserProfile - SignInWithGoogleUseCases"
        ),
        ProjectArchitecture(
            Icons.Default.Dataset,
            "Data",
            "Firebase Auth - Repositories",
            "FirebaseAuthDataSource - UserProfileUserRepo"
        )
    )

    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = "Sign In Security",
                modifier = Modifier.size(70.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        item {
            BodyLarge(text = "SignIn with Google")
        }
        item {
            BodyMedium(
                text = "Authentification, Persistence and Android Architecture",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                textAlign = TextAlign.Center
            )
        }
        item {
            BodyLarge(
                text = "Content:",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.Start),
                fontSize = 24.sp
            )
        }
        items(textContent) { data ->
            OutlinedCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = data.icon,
                        contentDescription = data.title
                    )
                    BodyLarge(text = data.title, fontSize = 24.sp)
                    BodyMedium(
                        text = data.description,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp
                    )
                    BodyMedium(
                        text = data.content,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp
                    )
                }
            }
        }
    }
}