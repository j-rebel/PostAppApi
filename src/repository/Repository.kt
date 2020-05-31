package com.example.repository

import User

interface Repository {
    suspend fun addUser(email: String,
                        displayName: String,
                        passwordHash: String,
                        avatar: String): User?
    suspend fun findUser(userId: Long): User?
    suspend fun findUserByEmail(email: String): User?
}

