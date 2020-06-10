package com.example.repository

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Likes: Table() {
    val uId : Column<String> = varchar("uId", 256).primaryKey()
    val userId : Column<Long> = long("userId").references(Users.userId)
    val postId : Column<Long> = long("postId").references(Posts.id)
}