package com.example.model

data class Share (
    val postId: Long,
    val userId: Long,
    val uId: String = "$userId:$postId"
)