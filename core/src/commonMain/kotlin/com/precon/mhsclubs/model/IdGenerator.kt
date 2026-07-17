package com.precon.mhsclubs.model

import kotlin.random.Random

/** Generates UUIDv4-shaped identifiers without relying on JVM-only APIs. */
internal fun newId(): String {
    val bytes = Random.Default.nextBytes(16)
    bytes[6] = (bytes[6].toInt() and 0x0f or 0x40).toByte()
    bytes[8] = (bytes[8].toInt() and 0x3f or 0x80).toByte()
    val hex = bytes.joinToString("") { byte ->
        (byte.toInt() and 0xff).toString(16).padStart(2, '0')
    }
    return "${hex.substring(0, 8)}-${hex.substring(8, 12)}-${hex.substring(12, 16)}-${hex.substring(16, 20)}-${hex.substring(20)}"
}
