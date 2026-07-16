package com.precon.mhsclubs

import com.precon.mhsclubs.services.CalendarTokenCipher
import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class CalendarTokenCipherTest {
    @Test fun `encrypts OAuth secrets with a random envelope`() {
        val cipher = CalendarTokenCipher(Base64.getEncoder().encodeToString(ByteArray(32) { it.toByte() }))
        val first = cipher.encrypt("refresh-token")
        val second = cipher.encrypt("refresh-token")
        assertNotEquals(first, second)
        assertEquals("refresh-token", cipher.decrypt(first))
    }
}
