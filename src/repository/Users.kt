package com.example.repository

import com.example.repository.Posts.uniqueIndex
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val userId : Column<Long> = long("id").autoIncrement().uniqueIndex().primaryKey()
    val avatar: Column<String> = varchar("avatar", 512)
    val email = varchar("email", 128).uniqueIndex()
    val displayName = varchar("display_name", 256)
    val passwordHash = varchar("password_hash", 64)
}