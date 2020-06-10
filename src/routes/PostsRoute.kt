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
import io.ktor.routing.post

const val POSTS = "$API_VERSION/posts"
const val POST_LIKE = "$POSTS/like"

@KtorExperimentalLocationsAPI
@Location(POSTS)
class PostRoute

@KtorExperimentalLocationsAPI
@Location(POST_LIKE)
class PostLikeRoute

@KtorExperimentalLocationsAPI
fun Route.posts(db: Repository) {
    authenticate("jwt") { // 1
        post<PostRoute> { // 2
            val postsParameters = call.receive<Parameters>()

            val type = postsParameters["type"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, "Missing type")
            val repost = postsParameters["repost"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, "Missing repost")
            val text = postsParameters["text"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, "Missing text")
            val video = postsParameters["video"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, "Missing video")
            val address = postsParameters["address"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, "Missing address")
            val geo_long = postsParameters["geo_long"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, "Missing longitude")
            val geo_lat = postsParameters["geo_lat"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, "Missing latitude")

            // 3
            val user = call.sessions.get<MySession>()?.let {
                db.findUser(it.userId)
            }
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, "Problems retrieving User")
                return@post
            }

            try {
                // 4
                val currentPost = db.addPost(
                    user.userId, type, repost.toLong(), text, video, address, geo_long.toFloat(), geo_lat.toFloat())
                currentPost?.id?.let {
                    call.respond(HttpStatusCode.OK, currentPost)
                }
            } catch (e: Throwable) {
                application.log.error("Failed to add post", e)
                call.respond(HttpStatusCode.BadRequest, "Problems Saving post")
            }
        }

        get<PostRoute> {
            val user = call.sessions.get<MySession>()?.let { db.findUser(it.userId) }
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
                return@get
            }
            try {
                val posts = db.getPost(user.userId)
                call.respond(posts)
            } catch (e: Throwable) {
                application.log.error("Failed to get Posts", e)
                call.respond(HttpStatusCode.BadRequest, "Problems getting Posts")
            }
        }

        delete<PostRoute> {
            val postsParameters = call.receive<Parameters>()
            if (!postsParameters.contains("id")) {
                return@delete call.respond(HttpStatusCode.BadRequest, "Missing Post Id")
            }
            val postId =
                postsParameters["id"] ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing Post Id")
            val post = db.findPost(postId.toLong()) ?: return@delete call.respond(HttpStatusCode.BadRequest, "Missing Post")
            val user = call.sessions.get<MySession>()?.let { db.findUser(it.userId) }
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, "Problems retrieving User")
                return@delete
            }
            if (user.userId != post?.posted_by) {
                call.respond(HttpStatusCode.Forbidden, "Forbidden")
                return@delete
            }

            try {
                db.deletePost(postId.toLong())
                call.respond(HttpStatusCode.OK)
            } catch (e: Throwable) {
                application.log.error("Failed to delete post", e)
                call.respond(HttpStatusCode.BadRequest, "Problems Deleting post")
            }
        }

        post<PostLikeRoute> {
            val postsParameters = call.receive<Parameters>()
            val userId = postsParameters["user"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, "Missing user")
            val postId = postsParameters["post"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest, "Missing post")
            val user = db.findUser(userId.toLong())
            if (user == null) {
                call.respond(HttpStatusCode.BadRequest, "User not found")
                return@post
            }
            val post = db.findPost(postId.toLong())
            if (post == null) {
                call.respond(HttpStatusCode.BadRequest, "Post not found")
                return@post
            }
            try {
                db.addLike(user.userId, post.id)
                call.respond(HttpStatusCode.OK)
            } catch (e: Throwable) {
                application.log.error("Failed to process", e)
                call.respond(HttpStatusCode.BadRequest, "Failed to process")
            }
        }
    }
}