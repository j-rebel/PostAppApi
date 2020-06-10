package com.example.repository

import User
import com.example.model.Like
import com.example.model.Post

interface Repository {
    suspend fun addUser(email: String,
                        displayName: String,
                        passwordHash: String,
                        avatar: String): User?
    suspend fun findUser(userId: Long): User?
    suspend fun findUserByEmail(email: String): User?
    suspend fun addPost(posted_by: Long,
                        type: String,
                        repost: Long,
                        text: String,
                        video: String,
                        address: String,
                        geo_long: Float,
                        geo_lat: Float
    ): Post?
    suspend fun getAllPosts(): List<Post>
    suspend fun getPostsByUser(userId: Long): List<Post>
    suspend fun findPostById(postId: Long): Post?
    suspend fun deletePost(postId: Long)
    suspend fun addLike(userId: Long,
                        postId: Long
    ): Like?
}

