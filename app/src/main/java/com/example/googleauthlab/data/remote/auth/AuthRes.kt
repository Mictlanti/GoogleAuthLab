package com.example.googleauthlab.data.remote.auth

sealed class AuthRes<out T> {
    data class Success<T>(val data: T) : AuthRes<T>()
    data class Error(val errorMsg: String) : AuthRes<Nothing>()
}