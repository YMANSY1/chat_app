package com.example.models.users

interface UserService {
    suspend fun getAllUsers(): List<User>
    suspend fun getById(id: Long): User?
    suspend fun createUser(user: User)
    suspend fun updateUser(user: User): User
    suspend fun deleteUserById(id: String): Boolean
    suspend fun getByEmail(email: String): User?
    suspend fun getByUsername(username: String): User?
    suspend fun registerUser(username: String, email: String, password:String): User?
    suspend fun loginUser(emailOrUsername: String, password: String): User?
    suspend fun getAllUsersExcept(id: Long): List<User>
}