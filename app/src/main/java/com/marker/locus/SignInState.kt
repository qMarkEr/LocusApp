package com.marker.locus

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)