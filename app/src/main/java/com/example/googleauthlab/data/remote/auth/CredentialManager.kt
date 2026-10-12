package com.example.googleauthlab.data.remote.auth

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleAuthClient @Inject constructor(
    private val webClientId: String
) {

    suspend fun getGoogleIdToken(
        context: Context
    ): AuthRes<String> {
        val credentialManager = CredentialManager.create(context)

        return try {
            val credential = try {
                requestCredential(
                    credentialManager = credentialManager,
                    context = context,
                    filterAuthorizedAccounts = true
                )
            } catch (e: NoCredentialException) {
                requestCredential(
                    credentialManager = credentialManager,
                    context = context,
                    filterAuthorizedAccounts = false
                )
            }

            if (
                credential is CustomCredential &&
                credential.type ==
                GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleCredential =
                    GoogleIdTokenCredential.createFrom(credential.data)

                AuthRes.Success(googleCredential.idToken)
            } else {
                AuthRes.Error("El tipo de credencial recibido no es válido.")
            }
        } catch (e: GoogleIdTokenParsingException) {
            AuthRes.Error("No se pudo procesar la credencial de Google.")
        } catch (e: GetCredentialException) {
            AuthRes.Error(
                e.message ?: "No se pudo obtener la credencial de Google."
            )
        } catch (e: Exception) {
            AuthRes.Error(
                e.message ?: "Ocurrió un error durante el inicio de sesión."
            )
        }
    }

    private suspend fun requestCredential(
        credentialManager: CredentialManager,
        context: Context,
        filterAuthorizedAccounts: Boolean
    ) = credentialManager.getCredential(
        context = context,
        request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setServerClientId(webClientId)
                    .setFilterByAuthorizedAccounts(
                        filterAuthorizedAccounts
                    )
                    .build()
            )
            .build()
    ).credential

    suspend fun clearCredentialState(context: Context) {
        val credentialManager = CredentialManager.create(context)

        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
    }

}