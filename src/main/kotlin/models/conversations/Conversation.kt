package com.example.models.conversations

import com.example.models.messages.Message
import com.example.models.users.User
import kotlinx.serialization.Serializable

@Serializable
data class Conversation(
    val id: Long?=null,
    val user1: User,
    val user2: User,
    var lastMessage: Message?=null,
)
