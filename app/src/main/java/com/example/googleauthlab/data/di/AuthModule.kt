package com.example.googleauthlab.data.di

import android.content.Context
import com.example.googleauthlab.R
import com.example.googleauthlab.data.remote.auth.AuthManager
import com.example.googleauthlab.data.remote.auth.GoogleAuthClient
import com.example.googleauthlab.data.repository.AuthRepositoryImpl
import com.example.googleauthlab.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//Nota:
/*
* Aunque ambos pertenecen al Auth de Gogole, los separé porque AuthModule está declarado como abstract class y
* contiene dos tipos de métodos de inyección:
- @Binds: debe ser un método abstracto.
- @Provides: tiene un cuerpo de implementación y, en este caso, no es estático.
Dagger/Hilt no permite combinar esos métodos de esa manera dentro de un mismo módulo.
* */

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /*
    ¿Por qué @Binds? Porque AuthRepositoryImpl ya tiene un constructor con @Inject.
    Hilt puede construir esa implementación e inyectar sus dependencias.
    Solo necesita saber que debe utilizarla cuando alguien solicite AuthRepository.
    * */

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepository: AuthRepositoryImpl
    ): AuthRepository

}

@Module
@InstallIn(SingletonComponent::class)
object AuthModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth() : FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    @Singleton
    fun providesGoogleAuthClient(
        @ApplicationContext context: Context
    ): GoogleAuthClient = GoogleAuthClient(
        context.getString(
            R.string.web_client_id
        )
    )

}