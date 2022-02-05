package com.github.jambeez.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory

val logger = LoggerFactory.getLogger("JamBeez")

inline fun <reified T> ObjectMapper.readValueOrNull(data: ByteArray): T? {
    try {
        return this.readValue(data)
    } catch (e: Exception) {
        logger.error(e.message, e)
        return null
    }
}
