package com.precon.mhsclubs.services

import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/** AES-256-GCM envelope encryption for OAuth secrets stored in PostgreSQL. */
class CalendarTokenCipher(base64Key: String) {
    private val key = Base64.getDecoder().decode(base64Key).also {
        require(it.size == 32) { "CALENDAR_TOKEN_ENCRYPTION_KEY must decode to exactly 32 bytes" }
    }
    private val secureRandom = SecureRandom()

    fun encrypt(plainText: String): String {
        val nonce = ByteArray(12).also(secureRandom::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.ENCRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, nonce))
        }
        return Base64.getEncoder().encodeToString(nonce + cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8)))
    }

    fun decrypt(envelope: String): String {
        val bytes = Base64.getDecoder().decode(envelope)
        require(bytes.size > 28) { "Invalid calendar-token envelope" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(Cipher.DECRYPT_MODE, SecretKeySpec(key, "AES"), GCMParameterSpec(128, bytes.copyOfRange(0, 12)))
        }
        return String(cipher.doFinal(bytes.copyOfRange(12, bytes.size)), StandardCharsets.UTF_8)
    }
}
