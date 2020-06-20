package com.example.routes

import com.example.API_VERSION
import com.example.auth.JwtService
import com.example.auth.MySession
import com.example.repository.Repository
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.sessions.sessions
import io.ktor.sessions.set

const val USERS = "$API_VERSION/users"
const val USER_LOGIN = "$USERS/login"
const val USER_CREATE = "$USERS/create"

@KtorExperimentalLocationsAPI
@Location(USER_LOGIN)
class UserLoginRoute

@KtorExperimentalLocationsAPI
@Location(USER_CREATE)
class UserCreateRoute

@KtorExperimentalLocationsAPI
fun Route.users(
    db: Repository,
    jwtService: JwtService,
    hashFunction: (String) -> String
) {
    post<UserCreateRoute> {
        val signupParameters = call.receive<Parameters>()
        val password = signupParameters["password"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, mapOf("error" to "Missing pass")
            )
        val displayName = signupParameters["displayName"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, mapOf("error" to "Missing name")
            )
        val email = signupParameters["email"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, mapOf("error" to "Missing email")
            )
        val avatar = signupParameters["avatar"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, mapOf("error" to "Missing avatar")
            )
        val hash = hashFunction(password)
        try {
            val newUser = db.addUser(email, displayName, hash, avatar)
            newUser?.userId?.let {
                call.sessions.set(MySession(it))
                call.respond(
                    HttpStatusCode.Created, mapOf("token" to jwtService.generateToken(newUser))
                )
            }
        } catch (e: Throwable) {
            application.log.error("Failed to register user", e)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to create user"))
        }
    }

    post<UserLoginRoute> {
        val signinParameters = call.receive<Parameters>()
        val password = signinParameters["password"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, mapOf("error" to "Missing pass")
            )
        val email = signinParameters["email"]
            ?: return@post call.respond(
                HttpStatusCode.Unauthorized, mapOf("error" to "Missing email")
            )
        val hash = hashFunction(password)
        try {
            val currentUser = db.findUserByEmail(email)
            currentUser?.userId?.let {
                if (currentUser.passwordHash == hash) {
                    call.sessions.set(MySession(it))
                    call.respond(
                        HttpStatusCode.OK, mapOf("token" to jwtService.generateToken(currentUser))
                    )
                } else {
                    call.respond(
                        HttpStatusCode.BadRequest, mapOf("error" to "Failed to retrieve user")
                    )
                }
            }
        } catch (e: Throwable) {
            application.log.error("Failed to login user", e)
            call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Failed to retrieve user"))
        }
    }
}

