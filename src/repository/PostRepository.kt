package com.example.repository

import com.example.model.User
import com.example.model.Like
import com.example.model.Post
import com.example.model.Share
import com.example.repository.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.statements.InsertStatement
import java.util.*

class PostRepository : Repository {
    override suspend fun addUser(
        email: String,
        displayName: String,
        passwordHash: String,
        avatar: String
    ): User? {
        var statement: InsertStatement<Number>? = null
        dbQuery {
            statement = Users.insert { user ->
                user[Users.email] = email
                user[Users.displayName] = displayName
                user[Users.passwordHash] = passwordHash
                user[Users.avatar] = avatar
            }
        }
        return rowToUser(statement?.resultedValues?.get(0))
    }

    override suspend fun findUser(userId: Long) = dbQuery {
        Users.select { Users.userId.eq(userId) }
            .map { rowToUser(it) }.singleOrNull()
    }

    override suspend fun findUserByEmail(email: String) = dbQuery {
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
        var statement: InsertStatement<Number>? = null
        dbQuery {
            statement = Posts.insert {
                it[Posts.postedBy] = posted_by
                it[Posts.date] = Date().time
                it[Posts.type] = type
                it[Posts.repost] = repost
                it[Posts.text] = text
                it[Posts.video] = video
                it[Posts.address] = address
                it[Posts.geoLong] = geo_long
                it[Posts.geoLat] = geo_lat
                it[Posts.likesCount] = 0
                it[Posts.sharesCount] = 0
                it[Posts.commentsCount] = 0
                it[Posts.views] = 0
            }
        }
        return rowToPost(statement?.resultedValues?.get(0))
    }

    override suspend fun getAllPosts(): List<Post> {
        return dbQuery {
            Posts.update {
                with(SqlExpressionBuilder) {
                    it.update(Posts.views, Posts.views + 1)
                }
            }
            Posts.selectAll().mapNotNull { rowToPost(it) }
        }
    }

    override suspend fun getPostsByUser(userId: Long): List<Post> {

        return dbQuery {
            Posts.update({ Posts.postedBy eq userId }) {
                with(SqlExpressionBuilder) {
                    it.update(Posts.views, Posts.views + 1)
                }
            }
            Posts.select {
                Posts.postedBy.eq(userId)
            }.mapNotNull { rowToPost(it) }
        }
    }

    override suspend fun findPostById(postId: Long): Post? {
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

    override suspend fun updatePost(
        postId: Long,
        type: String,
        repost: Long,
        text: String,
        video: String,
        address: String,
        geo_long: Float,
        geo_lat: Float
    ) {
        dbQuery {
            Posts.update({ Posts.id eq postId }) {
                it[Posts.type] = type
                it[Posts.repost] = repost
                it[Posts.text] = text
                it[Posts.video] = video
                it[Posts.address] = address
                it[Posts.geoLong] = geo_long
                it[Posts.geoLat] = geo_lat
            }
        }
    }

    override suspend fun addLike(userId: Long, postId: Long): Like? {
        val check = dbQuery {
            Likes.select { Likes.uId.eq("$userId:$postId") }
                .map { rowToLike(it) }.singleOrNull()
        }

        if (check == null) {
            var statement: InsertStatement<Number>? = null
            dbQuery {
                Posts.update({ Posts.id eq postId }) {
                    with(SqlExpressionBuilder) {
                        it.update(Posts.likesCount, Posts.likesCount + 1)
                    }
                }
                statement = Likes.insert { like ->
                    like[Likes.userId] = userId
                    like[Likes.postId] = postId
                    like[Likes.uId] = "$userId:$postId"
                }
            }
            return rowToLike(statement?.resultedValues?.get(0))
        } else {
            dbQuery {
                Posts.update({ Posts.id eq postId }) {
                    with(SqlExpressionBuilder) {
                        it.update(Posts.likesCount, Posts.likesCount - 1)
                    }
                }
                Likes.deleteWhere {
                    Likes.uId.eq("$userId:$postId")
                }
            }
        }
        return null
    }

    override suspend fun addShare(userId: Long, postId: Long): Share? {
        val check = dbQuery {
            Shares.select { Shares.uId.eq("$userId:$postId") }
                .map { rowToShare(it) }.singleOrNull()
        }

        if (check == null) {
            var statement: InsertStatement<Number>? = null
            dbQuery {
                Posts.update({ Posts.id eq postId }) {
                    with(SqlExpressionBuilder) {
                        it.update(Posts.sharesCount, Posts.sharesCount + 1)
                    }
                }
                statement = Shares.insert { share ->
                    share[Shares.userId] = userId
                    share[Shares.postId] = postId
                    share[Shares.uId] = "$userId:$postId"
                }
            }
            return rowToShare(statement?.resultedValues?.get(0))
        } else {
            dbQuery {
                Posts.update({ Posts.id eq postId }) {
                    with(SqlExpressionBuilder) {
                        it.update(Posts.sharesCount, Posts.sharesCount - 1)
                    }
                }
                Likes.deleteWhere {
                    Shares.uId.eq("$userId:$postId")
                }
            }
        }
        return null
    }

    private fun rowToPost(row: ResultRow?): Post? {
        if (row == null) {
            return null
        }
        return Post(
            id = row[Posts.id],
            postedBy = row[Posts.postedBy],
            date = row[Posts.date],
            type = row[Posts.type],
            repost = row[Posts.repost],
            text = row[Posts.text],
            video = row[Posts.video],
            address = row[Posts.address],
            geoLat = row[Posts.geoLat],
            geoLong = row[Posts.geoLong],
            likesCount = row[Posts.likesCount],
            sharesCount = row[Posts.sharesCount],
            commentsCount = row[Posts.commentsCount],
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

private fun rowToLike(row: ResultRow?): Like? {
    if (row == null) {
        return null
    }
    return Like(
        uId = row[Likes.uId],
        userId = row[Likes.userId],
        postId = row[Likes.postId]
    )
}

private fun rowToShare(row: ResultRow?): Share? {
    if (row == null) {
        return null
    }
    return Share(
        uId = row[Shares.uId],
        userId = row[Shares.userId],
        postId = row[Shares.postId]
    )
}