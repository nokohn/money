package com.example.util

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtil {
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(16)
        random.nextBytes(salt)
        return salt.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val bytes = (password + salt).toByteArray(Charsets.UTF_8)
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val actualHash = hashPassword(password, salt)
        return actualHash.equals(expectedHash, ignoreCase = true)
    }
}
