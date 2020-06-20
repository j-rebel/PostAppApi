package com.example.routes

import com.example.API_VERSION
import com.example.auth.JwtService
import com.example.auth.MySession
import com.example.repository.Repository
import io.ktor.application.application
import io.ktor.application.call
import io.ktor.application.log
import io.ktor.auth.authenticate
import io.ktor.features.UnsupportedMediaTypeException
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.http.content.streamProvider
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Location
import io.ktor.locations.get
import io.ktor.locations.post
import io.ktor.request.receive
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondFile
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.sessions.set
import java.io.File
import java.util.*

const val UPLOAD = "$API_VERSION/upload"
const val DOWNLOAD = "$API_VERSION/download"

@KtorExperimentalLocationsAPI
@Location(UPLOAD)
class FilesUpload

@KtorExperimentalLocationsAPI
@Location(DOWNLOAD)
class FilesDownload

@KtorExperimentalLocationsAPI
fun Route.files(db: Repository) {
    authenticate("jwt") {
        post<FilesUpload> {
            val user = call.sessions.get<MySession>()?.let { db.findUser(it.userId) }
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "No user data sent")
                )
                return@post
            }

            // retrieve all multipart data (suspending)
            val multipart = call.receiveMultipart()
            multipart.forEachPart { part ->
                // if part is a file (could be form item)
                if(part is PartData.FileItem) {
                    // retrieve file name of upload
                    val ext = when (part.contentType) {
                        ContentType.Image.JPEG -> "jpg"
                        ContentType.Image.PNG -> "png"
                        else -> return@forEachPart call.respond(HttpStatusCode.UnsupportedMediaType, mapOf("error" to "Not JPEG or PNG"))
                    }
                    //val name = part.originalFileName!!
                    val name = "${UUID.randomUUID()}.$ext"
                    val file = File("./uploads/$name")

                    // use InputStream from part to save file
                    part.streamProvider().use { its ->
                        // copy the stream to the file with buffering
                        file.outputStream().buffered().use {
                            // note that this is blocking
                            its.copyTo(it)
                        }
                    }
                    return@forEachPart call.respond(HttpStatusCode.OK, mapOf("success" to "Added $name"))
                }
                // make sure to dispose of the part after use to prevent leaks
                part.dispose()
            }
        }

        get<FilesDownload> {
            val postsParameters = call.receive<Parameters>()
            val user = call.sessions.get<MySession>()?.let { db.findUser(it.userId) }
            if (user == null) {
                call.respond(
                    HttpStatusCode.BadRequest, mapOf("error" to "No user data sent")
                )
                return@get
            }
            val filename = postsParameters["file"]
                ?: return@get call.respond (
                    HttpStatusCode.BadRequest, mapOf("error" to "File field not specified")
                )
            // construct reference to file
            // ideally this would use a different filename
            val file = File("./uploads/$filename")
            if(file.exists()) {
                call.respondFile(file)
            }
            else call.respond(HttpStatusCode.NotFound, mapOf("error" to "File not found"))
        }

    }
}