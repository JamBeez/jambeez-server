package com.github.jambeez.server

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val logger: Logger = LoggerFactory.getLogger(JamBeezServerApplication::class.java)!!

fun createObjectMapper(): ObjectMapper {
    val objectMapper = ObjectMapper().registerKotlinModule()
    objectMapper.configure(SerializationFeature.INDENT_OUTPUT, true)
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    objectMapper.setVisibility(
        objectMapper.serializationConfig.defaultVisibilityChecker //
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)//
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)//
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)//
            .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE)
    )
    return objectMapper
}

inline fun <reified T> ObjectMapper.readValueOrNull(data: ByteArray): T? {
    try {
        return this.readValue(data)
    } catch (e: Exception) {
        logger.error(e.message, e)
        return null
    }
}
