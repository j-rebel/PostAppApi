package com.example.routes

import com.example.API_VERSION
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.*
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import com.example.auth.MySession
import com.example.repository.Repository
import io.ktor.sessions.set

const val POSTS = "$API_VERSION/posts"
const val ALL_POSTS = "$POSTS/all"
const val ALL_POSTS_FOR_APP = "$POSTS/all-app"
const val POST_LIKE = "$POSTS/like"
const val POST_SHARE = "$POSTS/share"

@KtorExperimentalLocationsAPI
@Location(POSTS)
class PostRoute

@KtorExperimentalLocationsAPI
@Location(ALL_POSTS)
class AllPostRoute

@KtorExperimentalLocationsAPI
@Location(ALL_POSTS_FOR_APP)
class AllPostRouteForApp

@KtorExperimentalLocationsAPI
@Location(POST_LIKE)
class PostLikeRoute

@KtorExperimentalLocationsAPI
@Location(POST_SHARE)
class PostShareRoute

@KtorExperimentalLocationsAPI
fun Route.posts(db: Repository) {
    authenticate("jwt") {
        post<PostRoute> {
            val postsParameters = call.receive<Parameters>()

            val type = postsParameters["type"]
                ?: return@post call.respond (
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing type")
                )
            val repost = postsParameters["repost"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing repost")
                )
            val text = postsParameters["text"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing text")
                )
            val video = postsParameters["video"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing video")
                )
            val address = postsParameters["address"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing address")
                )
            val geoLong = postsParameters["geo_long"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing longitude")
                )
            val geoLat = postsParameters["geo_lat"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing latitude")
                )
            val user = call.sessions.get<MySession>()?.let {
                db.findUser(it.userId)
            }
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "No user data sent")
                )
                return@post
            }

            try {
                val currentPost = db.addPost(
                    user.userId, type, repost.toLong(), text, video, address, geoLong.toFloat(), geoLat.toFloat()
                )
                currentPost?.id?.let {
                    //call.respond(HttpStatusCode.OK, currentPost)
                    call.respond(HttpStatusCode.OK, mapOf("success" to "Post added"))
                }
            } catch (e: Throwable) {
                application.log.error("Failed to add post", e)
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to add post"))
            }
        }

        get<PostRoute> {
            val user = call.sessions.get<MySession>()?.let { db.findUser(it.userId) }
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "No user data sent")
                )
                return@get
            }
            try {
                val posts = db.getPostsByUser(user.userId)
                call.respond(posts)
            } catch (e: Throwable) {
                application.log.error("Failed to get Posts", e)
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Problems getting Posts"))
            }
        }

        delete<PostRoute> {
            val postsParameters = call.receive<Parameters>()
            if (!postsParameters.contains("id")) {
                return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing Post Id"))
            }
            val postId =
                postsParameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Missing Post Id"))
            val post = db.findPostById(postId.toLong())
                ?: return@delete call.respond(
                HttpStatusCode.NotFound, mapOf("error" to "Missing Post")
            )
            val user = call.sessions.get<MySession>()?.let { db.findUser(it.userId) }
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Problems retrieving user"))
                return@delete
            }
            if (user.userId != post.postedBy) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Forbidden"))
                return@delete
            }

            try {
                db.deletePost(postId.toLong())
                call.respond(HttpStatusCode.OK)
            } catch (e: Throwable) {
                application.log.error("Failed to delete post", e)
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Problems Deleting post"))
            }
        }

        patch<PostRoute> {
            val postsParameters = call.receive<Parameters>()

            val postId = postsParameters["id"]
                ?: return@patch call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing Post Id")
                )
            val post = db.findPostById(postId.toLong())
                ?: return@patch call.respond(
                HttpStatusCode.NotFound, mapOf("error" to "Missing Post")
            )
            val type = postsParameters["type"]
                ?: return@patch call.respond (
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing type")
                )
            val repost = postsParameters["repost"]
                ?: return@patch call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing repost")
                )
            val text = postsParameters["text"]
                ?: return@patch call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing text")
                )
            val video = postsParameters["video"]
                ?: return@patch call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing video")
                )
            val address = postsParameters["address"]
                ?: return@patch call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing address")
                )
            val geoLong = postsParameters["geo_long"]
                ?: return@patch call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing longitude")
                )
            val geoLat = postsParameters["geo_lat"]
                ?: return@patch call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing latitude")
                )
            val user = call.sessions.get<MySession>()?.let {
                db.findUser(it.userId)
            }
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "No user data sent")
                )
                return@patch
            }

            if (user.userId != post.postedBy) {
                call.respond(HttpStatusCode.Forbidden, mapOf("error" to "Forbidden"))
                return@patch
            }

            try {
                db.updatePost(
                    postId.toLong(), type, repost.toLong(), text, video, address, geoLong.toFloat(), geoLat.toFloat()
                )
                call.respond(HttpStatusCode.OK, mapOf("success" to "Post updated"))
            } catch (e: Throwable) {
                application.log.error("Failed to update post", e)
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to update post"))
            }
        }

        get<AllPostRoute> {
            try {
                val posts = db.getAllPosts()
                call.respond(posts)
            } catch (e: Throwable) {
                application.log.error("Failed to get Posts", e)
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Problems getting Posts"))
            }
        }

        get<AllPostRouteForApp> {
            val user = call.sessions.get<MySession>()?.let {
                db.findUser(it.userId)
            }
            try {
                val posts = db.getAllPostsForApp(user!!.userId)
                call.respond(posts)
            } catch (e: Throwable) {
                application.log.error("Failed to get Posts", e)
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Problems getting Posts"))
            }
        }

        post<PostLikeRoute> {
            val postsParameters = call.receive<Parameters>()
            val userId = postsParameters["user"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing user")
                )
            val postId = postsParameters["post"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing post")
                )
            val user = db.findUser(userId.toLong())
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "com.example.model.User not found"))
                return@post
            }
            val post = db.findPostById(postId.toLong())
            if (post == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Post not found"))
                return@post
            }
            try {
                db.addLike(user.userId, post.id)
                call.respond(HttpStatusCode.OK)
            } catch (e: Throwable) {
                application.log.error("Failed to process", e)
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to process"))
            }
        }

        post<PostShareRoute> {
            val postsParameters = call.receive<Parameters>()
            val userId = postsParameters["user"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing user")
                )
            val postId = postsParameters["post"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "Missing post")
                )
            val user = db.findUser(userId.toLong())
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "com.example.model.User not found"))
                return@post
            }
            val post = db.findPostById(postId.toLong())
            if (post == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Post not found"))
                return@post
            }
            try {
                db.addShare(user.userId, post.id)
                call.respond(HttpStatusCode.OK)
            } catch (e: Throwable) {
                application.log.error("Failed to process", e)
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to process"))
            }
        }
    }
}