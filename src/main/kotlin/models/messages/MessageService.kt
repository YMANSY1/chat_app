package com.example.models.messages

import com.example.models.conversations.Conversation

interface MessageService {
    suspend fun getAllMessages(): List<Message>
    suspend fun getById(messageId: Long): Message?
    suspend fun getConversation(id: Long): List<Message>
    suspend fun deleteMessage(message: Message)
    suspend fun addMessage(message: Message): Message
    suspend fun getAllMessagesForTwoUser(firstUserId: Long, secondUserId:Long): List<Message>
    suspend fun getMessageById(id: Long): Message?
    suspend fun deleteMessagesBetweenUsers(firstUserId: Long, secondUserId: Long)
    suspend fun sendMessage(message: Message)
}