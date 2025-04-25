package com.example.models

import com.example.models.conversations.Conversations
import com.example.models.messages.Message
import com.example.models.messages.MessageService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime

object Messages: Table(), MessageService {
    val id = long("id").autoIncrement()
    val senderId = long("sender_id")
    val receiverId = long("receiver_id")
    val content = varchar("content", 255)
    val timeSent = datetime("time_sent").clientDefault { LocalDateTime.now() }
    val isRead = bool("is_read").default(false)
    val conversationId = long("conversation_id").references(Conversations.id)

    override val primaryKey = PrimaryKey(id)

    private fun resultRowToMessage(resultRow: ResultRow): Message =
        Message(
            id = resultRow[id],
            senderId = resultRow[senderId],
            receiverId = resultRow[receiverId],
            content = resultRow[content],
            timeSent = resultRow[timeSent],
            isRead = resultRow[isRead],
            conversationId = resultRow[conversationId],
        )

    override suspend fun getAllMessages(): List<Message> = newSuspendedTransaction {selectAll().map { resultRowToMessage(it) }}

    override suspend fun getById(messageId: Long): Message? = newSuspendedTransaction {
        selectAll().where { Messages.id eq messageId }.firstOrNull()?.let { resultRowToMessage(it) }
    }

    override suspend fun getConversation(conversationId: Long): List<Message> = transaction {
        selectAll().where { Messages.conversationId eq conversationId }.orderBy(timeSent).map { resultRowToMessage(it) }
    }

    override suspend fun deleteMessage(message: Message) {
        TODO("Not yet implemented")
    }

    override suspend fun addMessage(message: Message): Message = transaction {
        val id = Messages.insert {
            it[senderId] = message.senderId
            it[receiverId] = message.receiverId
            it[content] = message.content
            it[conversationId] = message.conversationId
            it[timeSent] = message.timeSent
            it[isRead] = message.isRead
        }[Messages.id]

        message.copy(id = id)  // Return a copy of the message with the ID
    }

    override suspend fun getAllMessagesForTwoUser(firstUserId: Long, secondUserId: Long): List<Message> {
        TODO("Not yet implemented")
    }

    override suspend fun getMessageById(id: Long): Message? {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMessagesBetweenUsers(firstUserId: Long, secondUserId: Long) {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(message: Message) {
        TODO("Not yet implemented")
    }
}