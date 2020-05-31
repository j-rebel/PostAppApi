package com.example.repository

import com.example.model.PostType
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Posts: Table() {
    val id : Column<Long> = long("id").autoIncrement().primaryKey()
    val posted_by : Column<Long> = long("posted_by").references(Users.userId)
    val date: Column<Long> = long("date")
    val type: Column<String> = varchar("type", 256)
    val repost: Column<Long> = long("repost")
    val text: Column<String> = varchar("text", 2048)
    val video: Column<String> = varchar("video", 256)
    val address: Column<String> = varchar("address", 512)
    val geo_long: Column<Float> = float("longitude")
    val geo_lat: Column<Float> = float("latitude")
    val likes_count: Column<Int> = integer("likes_count")
    val shares_count: Column<Int> = integer("shares_count")
    val comments_count: Column<Int> = integer("comments_count")
    val liked_by: Column<String> = varchar("liked_by", 2048)
    val shared_by: Column<String> = varchar("shared_by", 2048)
    val commented_by: Column<String> = varchar("commented_by", 2048)
}