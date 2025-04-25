import com.example.models.Messages
import com.example.models.Users.getById
import com.example.models.conversations.Conversation
import com.example.models.conversations.Conversations
import com.example.models.messages.Message
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.LocalDateTime
import kotlin.time.Duration.Companion.seconds

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/ws") { // websocketSession
            val params = call.request.queryParameters
            val senderId = params["sender"]?.toLongOrNull()
                ?: return@webSocket close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Sender could not be found"))
            val receiverId = params["receiver"]?.toLongOrNull()
                ?: return@webSocket close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Receiver could not be found"))

            val sender = getById(senderId)
            val receiver = getById(receiverId)
            var conversation = Conversations.findConversationByUsersId(senderId, receiverId)
            if (conversation == null) {
                conversation = Conversation(
                    user1 = sender!!,
                    user2 = receiver!!,
                    lastMessage = null
                )
                conversation =Conversations.addConversation(conversation)
            }

            for (frame in incoming) {

                if (frame is Frame.Text) {
                    val text = frame.readText()

                    val message = Message(
                        senderId = senderId,
                        receiverId = receiverId,
                        content = text,
                        conversationId = conversation.id!!,
                        timeSent = LocalDateTime.now(),
                        isRead = false
                    )

                    outgoing.send(Frame.Text("YOU SAID: $text"))

                    // Save the message to the database
                    val savedMessage=Messages.addMessage(message)

                    // Update the last message of the conversation
                    Conversations.updateLastMessage(conversation.id!!, savedMessage)



                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
            }
        }
    }
}
