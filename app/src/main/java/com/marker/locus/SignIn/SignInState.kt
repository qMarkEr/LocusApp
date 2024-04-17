package com.marker.locus.SignIn

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
    val isNewUser : Boolean = false
)