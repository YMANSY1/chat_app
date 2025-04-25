package com.example.models.users

import com.example.models.LocalDateTimeSerializer
import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class User(
    val id: Long,
    val username: String,
    val email: String,
    val passwordHash: String,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime,
    val isOnline: Boolean,
)
