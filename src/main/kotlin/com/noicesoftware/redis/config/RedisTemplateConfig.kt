package com.noicesoftware.redis.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.noicesoftware.redis.model.Game
import io.lettuce.core.RedisURI
import org.slf4j.Logger
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer

@Configuration
class RedisTemplateConfig(
        val configuration: RedisConfig,
        val objectMapper: ObjectMapper,
        val logger: Logger
) {
    @Bean
    fun connectionFactory(): LettuceConnectionFactory {
        logger.info("redis url: ${configuration.redisUrl}")

        return try {
            val uri = RedisURI.create(configuration.redisUrl)
            val config = RedisStandaloneConfiguration(uri.host, uri.port)
            config.setPassword(uri.password)
            LettuceConnectionFactory(config)
        } catch (e: Exception) {
            val config = RedisStandaloneConfiguration(configuration.redisUrl)
            LettuceConnectionFactory(config)
        }
    }

    @Bean
    fun <T> redisTemplate(builder: RestTemplateBuilder): RedisTemplate<String, T> {
        // TODO How do you make the serializer generic like the template? Object::class.java doesn't work.
        val serializer = Jackson2JsonRedisSerializer(Game::class.java)
        serializer.setObjectMapper(objectMapper)

        val template = RedisTemplate<String, T>()
        template.setConnectionFactory(connectionFactory())
        template.setDefaultSerializer(serializer)
        return template
    }
}