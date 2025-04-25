package com.example.models.conversations

import com.example.models.messages.Message

interface ConversationService {
    suspend fun getAll(): List<Conversation>
    suspend fun getById(conversationId: Long): Conversation?
    suspend fun addConversation(conversation: Conversation): Conversation
    suspend fun findConversationByUsersId(firstId: Long, secondId: Long): Conversation?
    suspend fun findConversationsForUser(userId: Long): List<Conversation>
    suspend fun updateLastMessage(id:Long, message: Message)
}