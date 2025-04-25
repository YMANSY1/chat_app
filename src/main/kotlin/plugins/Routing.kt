package com.example.plugins

import com.example.models.Messages
import com.example.models.Users.getAllUsers
import com.example.models.Users.getAllUsersExcept
import com.example.models.Users.getById
import com.example.models.Users.loginUser
import com.example.models.Users.registerUser
import com.example.models.conversations.Conversations
import com.example.models.messages.Message
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import java.sql.SQLIntegrityConstraintViolationException

fun Application.configureRouting() {
    routing {
        route("/users") {
            get {
                val exceptId = call.request.queryParameters["except"]?.toLongOrNull()  // Query parameter
                val id = call.request.queryParameters["id"]?.toLongOrNull()  // Query parameter

                if (exceptId != null && id != null) {
                    call.respond(HttpStatusCode.BadRequest, "except and id cannot have a value simultaneously")
                    return@get
                }

                try {
                    if (id != null) {
                        val user = getById(id)
                        if (user != null) {
                            call.respond(user)  // Ensure getById returns a valid response
                        } else {
                            call.respond(HttpStatusCode.NotFound, "User not found")
                        }
                    } else if (exceptId != null) {
                        val users = getAllUsersExcept(exceptId)
                        call.respond(users)  // Ensure getAllUsersExcept returns a valid list of users
                    } else {
                        val users = getAllUsers()
                        call.respond(users)  // Ensure getAllUsers returns a valid list of users
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to fetch users: ${e.message}")
                }
            }

            post("/register") {
                try {
                    val params = call.receiveParameters()

                    val email = params["email"]?.trim()
                    val password = params["password"]?.trim()
                    val username = params["username"]?.trim()

                    if (email.isNullOrEmpty() || password.isNullOrEmpty() || username.isNullOrEmpty()) {
                        return@post call.respond(
                            HttpStatusCode.BadRequest,
                            "Email, password, and username are required."
                        )
                    }

                    val existingUser = getAllUsers().find { it.email == email || it.username == username }
                    if (existingUser != null) {
                        return@post call.respond(HttpStatusCode.Conflict, "Email or username already taken.")
                    }

                    // Register the user and get the user object
                    val registeredUser = registerUser(username = username, email = email, password = password)

                    if (registeredUser != null) {
                        // Return the created user in the response
                        call.respond(HttpStatusCode.Created, registeredUser)
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Error during registration.")
                    }

                } catch (e: SQLIntegrityConstraintViolationException) {
                    println(e.message ?: "no message")
                    call.respond(HttpStatusCode.BadRequest, "Email or username already taken")
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "Error during registration: ${e.message}")
                }
            }

            post("/login") {
                val params = call.receiveParameters()

                val emailOrUser = params["email_or_username"]?.trim()
                val password = params["password"]?.trim()

                if (emailOrUser.isNullOrEmpty() || password.isNullOrEmpty()) {
                    return@post call.respond(
                        HttpStatusCode.BadRequest,
                        "Email/Username and Password are required."
                    )
                }

                try {
                    val user = loginUser(emailOrUser, password)

                    if (user != null) {
                        call.respond(HttpStatusCode.OK, user)
                    } else {
                        call.respond(HttpStatusCode.Unauthorized, "Invalid username or password.")
                    }
                } catch (e: Exception) {
                    call.respond(HttpStatusCode.InternalServerError, "An error occurred: ${e.message}")
                }
            }


        }

        route ("/chats") {
            get {
                val params = call.request.queryParameters
                val user1Id = params["user1Id"]?.toLongOrNull()
                val user2Id = params["user2Id"]?.toLongOrNull()

                if (user2Id == null && user1Id != null) call.respond(Conversations.findConversationsForUser(user1Id))
                else if (user1Id != null && user2Id != null) call.respond(Conversations.findConversationByUsersId(user1Id, user2Id)?:call.respond(HttpStatusCode.OK, listOf<Message>()))
                else if (user1Id == null && user2Id==null) call.respond(Conversations.getAll())
                else call.respond(HttpStatusCode.BadRequest)
            }
            get ("/conversation") {
                val params = call.request.queryParameters
                val conversationId = params["conversationId"]?.toLongOrNull()
                    ?: return@get call.respond(HttpStatusCode.BadRequest, "conversationId is required")

                call.respond(Messages.getConversation(conversationId))
            }
        }

    }
}
