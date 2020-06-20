package com.example.model

data class Post(
    val id: Long,
    val postedBy: Long,
    val date: Long,
    val type: String,
    val repost: Long?,
    val text: String,
    val video: String?,
    val address: String?,
    val geoLong: Float?,
    val geoLat: Float?,
    val likesCount: Int,
    val sharesCount: Int,
    val commentsCount: Int,
    val views: Int
)