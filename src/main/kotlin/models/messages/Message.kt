package com.example.models.messages

import com.example.models.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class Message(
    val id: Long? = null,
    val senderId: Long,
    val receiverId: Long,
    val content: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val timeSent: LocalDateTime= LocalDateTime.now(),
    val isRead: Boolean = false,
    val conversationId: Long
)
