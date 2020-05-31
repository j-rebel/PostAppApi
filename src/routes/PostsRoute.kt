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

const val POSTS = "$API_VERSION/posts"

@KtorExperimentalLocationsAPI
@Location(POSTS)
class PostRoute

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
                val currentTodo = db.addPost(
                    user.userId, type, repost.toLong(), text, video, address, geo_long.toFloat(), geo_lat.toFloat())
                currentTodo?.id?.let {
                    call.respond(HttpStatusCode.OK, currentTodo)
                }
            } catch (e: Throwable) {
                application.log.error("Failed to add post", e)
                call.respond(HttpStatusCode.BadRequest, "Problems Saving post")
            }
        }
    }
}