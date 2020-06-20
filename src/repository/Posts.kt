package com.example.repository

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Posts : Table() {
    val id: Column<Long> = long("id").autoIncrement().uniqueIndex().primaryKey()
    val postedBy: Column<Long> = long("posted_by").references(Users.userId)
    val date: Column<Long> = long("date")
    val type: Column<String> = varchar("type", 256)
    val repost: Column<Long> = long("repost")
    val text: Column<String> = varchar("text", 2048)
    val video: Column<String> = varchar("video", 256)
    val address: Column<String> = varchar("address", 512)
    val geoLong: Column<Float> = float("longitude")
    val geoLat: Column<Float> = float("latitude")
    val likesCount: Column<Int> = integer("likes_count")
    val sharesCount: Column<Int> = integer("shares_count")
    val commentsCount: Column<Int> = integer("comments_count")
    val views: Column<Int> = integer("views")
}