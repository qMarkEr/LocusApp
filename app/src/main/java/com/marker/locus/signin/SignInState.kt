package com.marker.locus.signin

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
    val isNewUser : Boolean = false
)