package com.example.repository

import User
import com.example.model.Post
import com.example.repository.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.plus
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.statements.UpdateStatement
import java.util.*

class PostRepository: Repository {
    override suspend fun addUser(
        email: String,
        displayName: String,
        passwordHash: String,
        avatar: String) : User? {
        var statement : InsertStatement<Number>? = null // 1
        dbQuery { // 2
            // 3
            statement = Users.insert { user ->
                user[Users.email] = email
                user[Users.displayName] = displayName
                user[Users.passwordHash] = passwordHash
                user[Users.avatar] = avatar
            }
        }
        // 4
        return rowToUser(statement?.resultedValues?.get(0))
    }

    override suspend fun findUser(userId: Long) = dbQuery {
        Users.select { Users.userId.eq(userId) }
            .map { rowToUser(it) }.singleOrNull()
    }

    override suspend fun findUserByEmail(email: String)= dbQuery {
        Users.select { Users.email.eq(email) }
            .map { rowToUser(it) }.singleOrNull()
    }

    override suspend fun addPost(
        posted_by: Long,
        type: String,
        repost: Long,
        text: String,
        video: String,
        address: String,
        geo_long: Float,
        geo_lat: Float
    ): Post? {
        var statement : InsertStatement<Number>? = null
        dbQuery {
            statement = Posts.insert {
                it[Posts.posted_by] = posted_by
                it[Posts.date] = Date().time
                it[Posts.type] = type
                it[Posts.repost] = repost
                it[Posts.text] = text
                it[Posts.video] = video
                it[Posts.address] = address
                it[Posts.geo_long] = geo_long
                it[Posts.geo_lat] = geo_lat
                it[Posts.likes_count] = 0
                it[Posts.shares_count] = 0
                it[Posts.comments_count] = 0
                it[Posts.views] = 0
            }
        }
        return rowToPost(statement?.resultedValues?.get(0))
    }

    override suspend fun getPost(userId: Long): List<Post> {

        return dbQuery {
            Posts.update ({ Posts.posted_by eq userId }) {
                with(SqlExpressionBuilder) {
                    it.update(Posts.views, Posts.views + 1)
                }
            }
            Posts.select {
                Posts.posted_by.eq(userId) // 3
            }.mapNotNull { rowToPost(it) }
        }
    }

    override suspend fun findPost(postId: Long): Post? {
        return dbQuery {
            Posts.select { Posts.id.eq(postId) }
                .map { rowToPost(it) }.singleOrNull()
        }
    }

    override suspend fun deletePost(postId: Long) {
        dbQuery {
            Posts.deleteWhere {
                Posts.id.eq(postId)
            }
        }
    }

    private fun rowToPost(row: ResultRow?): Post? {
        if (row == null) {
            return null
        }
        return Post(
            id = row[Posts.id],
            posted_by = row[Posts.posted_by],
            date = row[Posts.date],
            type = row[Posts.type],
            repost = row[Posts.repost],
            text = row[Posts.text],
            video = row[Posts.video],
            address = row[Posts.address],
            geo_lat = row[Posts.geo_lat],
            geo_long = row[Posts.geo_long],
            likes_count = row[Posts.likes_count],
            shares_count = row[Posts.shares_count],
            comments_count = row[Posts.comments_count],
            views = row[Posts.views]
        )
    }
}

private fun rowToUser(row: ResultRow?): User? {
    if (row == null) {
        return null
    }
    return User(
        userId = row[Users.userId],
        avatar = row[Users.avatar],
        email = row[Users.email],
        displayName = row[Users.displayName],
        passwordHash = row[Users.passwordHash]
    )
}