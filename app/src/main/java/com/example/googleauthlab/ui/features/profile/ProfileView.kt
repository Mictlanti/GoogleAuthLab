package com.example.googleauthlab.ui.features.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person4
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.googleauthlab.ui.components.textStyles.BodyLarge
import com.example.googleauthlab.ui.components.textStyles.BodyMedium
import com.example.googleauthlab.ui.features.login.LoginUiState
import com.example.googleauthlab.ui.features.login.LoginViewModel
import kotlin.math.log

data class UserDataModel(
    val imageVector: ImageVector,
    val type: String,
    val userData: String
)

@Composable
fun ProfileViewRoute(loginViewModel: LoginViewModel) {

    val userState by loginViewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { paddingValues ->
        ProfileView(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(15.dp),
            userState = userState,
            onClickSignOut = {
                loginViewModel.sighOut(context)
            }
        )
    }
}

@Composable
private fun ProfileView(
    modifier: Modifier = Modifier,
    userState: LoginUiState,
    onClickSignOut: () -> Unit
) {

    val user = userState.user
    val userDataList = listOf(
        UserDataModel(
            Icons.Default.Person4,
            "Username",
            user?.username?.takeIf { it.isNotBlank() } ?: "Not found"
        ),
        UserDataModel(Icons.Default.Email, "Email", user?.email ?: "Not available"),
        UserDataModel(
            Icons.Default.Security,
            "Access provider",
            when {
                user == null -> "Unknow"
                user.isAnonymously -> "Anonymous"
                else -> "Google"
            }
        ),
        UserDataModel(
            Icons.Default.Person4,
            "Account Status",
            when {
                user == null -> "Disconnected"
                user.isAnonymously -> "Anonymous account"
                else -> "Connected"
            }
        )
    )

    LazyColumn(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        item {
            ProfileImage(
                imageUrl = userState.user?.photoUrl,
                modifier = Modifier
                    .fillMaxWidth(.4f)
                    .clip(CircleShape)
                    .aspectRatio(1f)
            )
        }
        item {
            BodyLarge(text = userState.user?.username ?: "Not found")
        }
        item {
            BodyMedium(
                text = userState.user?.email ?: "Not found",
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(.5f)
            )
        }
        item { Spacer(Modifier.height(10.dp)) }
        item {
            BodyMedium(
                text = "Data Account",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.Start)
            )
        }
        items(userDataList) { userData ->
            UserData(
                modifier = Modifier
                    .fillMaxWidth(),
                imageVector = userData.imageVector,
                type = userData.type,
                userData = userData.userData
            )
        }
        item { Spacer(Modifier.height(7.dp)) }
        item {
            BodyMedium(
                text = "Preferences",
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(Alignment.Start)
            )
        }
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Palette,
                                contentDescription = null
                            )
                            BodyMedium(text = "Personalize")
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null
                    )
                }
            }
        }
        item { Spacer(Modifier.height(10.dp)) }
        item {
            OutlinedButton(
                onClick = onClickSignOut,
                modifier = Modifier
                    .fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(
                        22.dp,
                        Alignment.CenterHorizontally
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        "Exit to app",
                        tint = Color.Red
                    )
                    BodyMedium(text = "Log out", color = Color.Red)
                }
            }
        }
    }
}