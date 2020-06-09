package com.example.model

data class Like (
    val postId: Long,
    val userId: Long,
    val uId: String = "$userId:$postId"
)