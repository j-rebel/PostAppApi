package com.example.model

import io.ktor.auth.Principal

data class User(
    val userId: Long,
    val avatar: String,
    val email: String,
    val displayName: String,
    val passwordHash: String
) : Principal