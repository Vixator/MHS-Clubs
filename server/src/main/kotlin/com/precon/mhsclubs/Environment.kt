package com.precon.mhsclubs

import io.github.cdimascio.dotenv.dotenv
import java.io.File

private val environmentDirectory = generateSequence(File(System.getProperty("user.dir")).absoluteFile) { it.parentFile }
    .firstOrNull { File(it, ".env").isFile }
    ?: File(System.getProperty("user.dir")).absoluteFile
private val dotenv = dotenv {
    directory = environmentDirectory.path
    ignoreIfMissing = true
}

fun environment(name: String): String? = System.getenv(name) ?: dotenv[name]

/** Resolves relative local secret paths beside the discovered project `.env` file. */
fun resolveEnvironmentPath(path: String): File = File(path).let { file ->
    if (file.isAbsolute) file else File(environmentDirectory, path)
}
