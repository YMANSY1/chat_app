package com.example.models.conversations

import com.example.models.Messages
import com.example.models.Users
import com.example.models.messages.Message
import com.example.models.users.User
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object Conversations : Table(), ConversationService {
    val id = long("id").autoIncrement()
    val user1Id = long("user1_id").references(Users.id)
    val user2Id = long("user2_id").references(Users.id)
    val lastMessageId = long("last_message_id").references(Messages.id).nullable()


    val user1Alias = Users.alias("user1")
    val user2Alias = Users.alias("user2")
    val lastMessageAlias = Messages.alias("last_message")

    private fun resultRowToConversation(resultRow: ResultRow): Conversation {
        val hasLastMessage = resultRow[lastMessageId] != null

        return Conversation(
            id = resultRow[id],
            user1 = User(
                id = resultRow[user1Id],
                email = resultRow[user1Alias[Users.email]],
                username = resultRow[user1Alias[Users.username]],
                passwordHash = resultRow[user1Alias[Users.passwordHash]],
                createdAt = resultRow[user1Alias[Users.createdAt]],
                isOnline = resultRow[user1Alias[Users.isOnline]],
            ),
            user2 = User(
                id = resultRow[user2Id],
                email = resultRow[user2Alias[Users.email]],
                username = resultRow[user2Alias[Users.username]],
                passwordHash = resultRow[user2Alias[Users.passwordHash]],
                createdAt = resultRow[user2Alias[Users.createdAt]],
                isOnline = resultRow[user2Alias[Users.isOnline]],
            ),
            lastMessage = if (hasLastMessage) Message(
                senderId = resultRow[lastMessageAlias[Messages.senderId]],
                receiverId = resultRow[lastMessageAlias[Messages.receiverId]],
                content = resultRow[lastMessageAlias[Messages.content]],
                timeSent = resultRow[lastMessageAlias[Messages.timeSent]],
                isRead = resultRow[lastMessageAlias[Messages.isRead]],
                conversationId = resultRow[id],
            ) else null
        )
    }

    override suspend fun getAll(): List<Conversation> {
        return transaction {
            (Conversations
                .innerJoin(user1Alias, { user1Id }, { user1Alias[Users.id] })
                .innerJoin(user2Alias, { user2Id }, { user2Alias[Users.id] })
                .leftJoin(lastMessageAlias, { lastMessageId }, { lastMessageAlias[Messages.id] })
                .selectAll()
                .map { resultRowToConversation(it) })
        }
    }


    override suspend fun getById(conversationId: Long): Conversation? = newSuspendedTransaction {
        (Conversations
            .innerJoin(user1Alias, { user1Id }, { user1Alias[Users.id] })
            .innerJoin(user2Alias, { user2Id }, { user2Alias[Users.id] })
            .leftJoin(lastMessageAlias, { lastMessageId }, { lastMessageAlias[Messages.id] })
            .selectAll()
            .where { Conversations.id eq conversationId }
            .firstOrNull()
            ?.let { resultRowToConversation(it) })
    }

    override suspend fun findConversationByUsersId(firstId: Long, secondId: Long): Conversation? = transaction {
        (Conversations
            .innerJoin(user1Alias, { user1Id }, { user1Alias[Users.id] })
            .innerJoin(user2Alias, { user2Id }, { user2Alias[Users.id] })
            .leftJoin(lastMessageAlias, { lastMessageId }, { lastMessageAlias[Messages.id] })
            .selectAll()
            .where {
                ((user1Id eq firstId) and (user2Id eq secondId)) or
                        ((user1Id eq secondId) and (user2Id eq firstId))
            }
            .firstOrNull()
            ?.let { resultRowToConversation(it) })
    }

    override suspend fun findConversationsForUser(userId: Long): List<Conversation> = transaction {
        Conversations
            .innerJoin(user1Alias, { user1Id }, { user1Alias[Users.id] })
            .innerJoin(user2Alias, { user2Id }, { user2Alias[Users.id] })
            .leftJoin(lastMessageAlias, { lastMessageId }, { lastMessageAlias[Messages.id] })
            .selectAll().where {
                (user1Id eq userId) or (user2Id eq userId)
            }
            .map { resultRowToConversation(it) }
    }


    override suspend fun updateLastMessage(id: Long, message: Message): Unit = transaction {
        Conversations.update({ Conversations.id eq id }) {
            it[lastMessageId] = message.id
        }
    }

    override suspend fun addConversation(conversation: Conversation): Conversation = transaction {
        val inserted = Conversations.insert {
            it[user1Id] = conversation.user1.id
            it[user2Id] = conversation.user2.id
            it[lastMessageId] = conversation.lastMessage?.id
        }

        val newId = inserted.resultedValues?.singleOrNull()?.get(Conversations.id)

        Conversation(
            id = newId,
            user1 = conversation.user1,
            user2 = conversation.user2,
            lastMessage = conversation.lastMessage
        )
    }
}