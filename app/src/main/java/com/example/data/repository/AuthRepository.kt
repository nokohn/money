package com.example.data.repository

import com.example.data.dao.SettingDao
import com.example.data.dao.UserDao
import com.example.data.model.SettingEntity
import com.example.data.model.UserEntity
import com.example.util.SecurityUtil
import kotlinx.coroutines.flow.Flow

sealed class AuthResult {
    data class Success(val user: UserEntity) : AuthResult()
    data class Error(val messageLao: String, val messageEng: String) : AuthResult()
}

class AuthRepository(
    private val userDao: UserDao,
    private val settingDao: SettingDao
) {
    suspend fun login(login: String, password: String): AuthResult {
        val user = userDao.getUserByUsernameOrEmail(login.trim())
            ?: return AuthResult.Error("ບໍ່ພົບຊື່ຜູ້ໃຊ້ ຫຼື ອີເມວນີ້", "User or email not found")

        val isValid = SecurityUtil.verifyPassword(password, user.salt, user.passwordHash)
        return if (isValid) {
            AuthResult.Success(user)
        } else {
            AuthResult.Error("ລະຫັດຜ່ານບໍ່ຖືກຕ້ອງ", "Incorrect password")
        }
    }

    suspend fun register(
        username: String,
        email: String,
        password: String,
        fullName: String,
        currency: String = "LAK",
        language: String = "LAO"
    ): AuthResult {
        if (username.isBlank() || password.isBlank() || fullName.isBlank()) {
            return AuthResult.Error("ກະລຸນາປ້ອນຂໍ້ມູນໃຫ້ຄົບຖ້ວນ", "Please fill in all required fields")
        }

        val existing = userDao.getUserByUsernameOrEmail(username.trim())
        if (existing != null) {
            return AuthResult.Error("ຊື່ຜູ້ໃຊ້ນີ້ມີຢູ່ແລ້ວໃນລະບົບ", "Username is already taken")
        }

        val salt = SecurityUtil.generateSalt()
        val passwordHash = SecurityUtil.hashPassword(password, salt)
        val newUser = UserEntity(
            username = username.trim().lowercase(),
            email = email.trim().lowercase(),
            passwordHash = passwordHash,
            salt = salt,
            fullName = fullName.trim(),
            currency = currency,
            language = language
        )

        val id = userDao.insertUser(newUser)
        val createdUser = newUser.copy(id = id)

        // Default setting
        settingDao.saveSetting(
            SettingEntity(
                userId = id,
                currency = currency,
                language = language,
                isDarkMode = false,
                notificationsEnabled = true
            )
        )

        return AuthResult.Success(createdUser)
    }

    suspend fun changePassword(userId: Long, oldPass: String, newPass: String): Boolean {
        val user = userDao.getUserById(userId) ?: return false
        if (!SecurityUtil.verifyPassword(oldPass, user.salt, user.passwordHash)) {
            return false
        }
        val newSalt = SecurityUtil.generateSalt()
        val newHash = SecurityUtil.hashPassword(newPass, newSalt)
        userDao.updatePassword(userId, newHash, newSalt)
        return true
    }

    suspend fun getUser(userId: Long): UserEntity? = userDao.getUserById(userId)

    fun getUserFlow(userId: Long): Flow<UserEntity?> = userDao.getUserByIdFlow(userId)

    suspend fun updateUserProfile(user: UserEntity) = userDao.updateUser(user)
}
