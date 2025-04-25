package com.example.models

import com.example.models.users.User
import com.example.models.users.UserService
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.javatime.datetime
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.time.LocalDateTime


object Users : Table(), UserService {
    val id = long("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val username = varchar("username", 255).uniqueIndex()
    val passwordHash = text("password_hash")
    val createdAt = datetime("created_at").clientDefault { LocalDateTime.now() }
    val isOnline = bool("is_online").clientDefault { true }
    override val primaryKey = PrimaryKey(id)

    fun resultRowToUser(row: ResultRow): User = User(
        id = row[id],
        email = row[email],
        username = row[username],
        passwordHash = row[passwordHash],
        createdAt = row[createdAt],
        isOnline = row[isOnline],
    )

    override suspend fun getAllUsers(): List<User> = transaction { selectAll().map { resultRowToUser(it) } }

    override suspend fun getAllUsersExcept(id: Long): List<User> = transaction {
        val query = selectAll().where { Users.id neq id }
        query.map { resultRowToUser(it) }
    }

    override suspend fun getById(id: Long): User? = transaction {
        val query = Users.selectAll().where(Users.id eq id)

        println(query.prepareSQL(this))

        query.singleOrNull()?.let { resultRowToUser(it) }
    }

    override suspend fun createUser(user: User) = transaction {
        val insertUser = Users.insert {
            it[email] = user.email
            it[username] = user.username
            it[passwordHash] = user.passwordHash
        }
    }

    override suspend fun updateUser(user: User): User {
        TODO("Not yet implemented")
    }

    override suspend fun deleteUserById(id: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getByEmail(email: String): User? = transaction {
        selectAll().where(Users.email.eq(email)).map { resultRowToUser(it) }.firstOrNull()
    }

    override suspend fun getByUsername(username: String): User? = transaction {
        selectAll().where(Users.username.eq(username)).map { resultRowToUser(it) }.firstOrNull()
    }

    override suspend fun registerUser(username: String, email: String, password: String): User? {
        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
        return transaction {
            val inserted = Users.insert {
                it[Users.email] = email
                it[Users.username] = username
                it[passwordHash] = hashedPassword
            }

            inserted.resultedValues?.firstOrNull()?.let { row ->
                User(
                    id = row[Users.id],
                    username = row[Users.username],
                    email = row[Users.email],
                    passwordHash = row[passwordHash],
                    createdAt = row[createdAt], // if you have this in your table
                    isOnline = row[isOnline]     // if you have this in your table
                )
            }
        }
    }


    override suspend fun loginUser(emailOrUsername: String, password: String): User? {
        val user = getByEmail(emailOrUsername) ?: getByUsername(emailOrUsername)
        return if (user != null && BCrypt.checkpw(password, user.passwordHash)) {
            user
        } else {
            null
        }
    }
}