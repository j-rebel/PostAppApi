package com.example.model

data class PostResponse(val id: Long,
                val posterName: String,
                val posterAvatar: String,
                val date: Long,
                val type: PostType,
                val repost: PostResponse?,
                val text: String,
                val video: String?,
                val address: String?,
                val geo: Pair<Double, Double>?,
                val likes: Int,
                val comments: Int,
                val shares: Int,
                val isLiked: Boolean = false,
                val isCommented: Boolean = false,
                val isShared: Boolean = false)