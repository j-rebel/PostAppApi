package com.example.model

data class Post(
    val id: Long,
    val posted_by: Long,
    val date: Long,
    val type: String,
    val repost: Long,
    val text: String,
    val video: String,
    val address: String,
    val geo_long: Float,
    val geo_lat: Float,
    val likes_count: Int,
    val shares_count: Int,
    val comments_count: Int,
    val views: Int
)