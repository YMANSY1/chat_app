package com.example.plugins

import com.example.models.Messages
import com.example.models.Users
import com.example.models.conversations.Conversations
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabases() {
    val driverClass= environment.config.property("ktor.database.driverClassName").getString()
    val user= environment.config.property("ktor.database.user").getString()
    val password= environment.config.property("ktor.database.password").getString()
    val url= environment.config.property("ktor.database.jdbcUrl").getString()
    val database = Database.connect(
        url = url,
        user = user,
        driver = driverClass,
        password = password,
    )

    transaction(database) {
        SchemaUtils.create(Users)
        SchemaUtils.create(Messages)
        SchemaUtils.create(Conversations)
    }
}
