package com.pedrocomitto.mlock.lock

import com.pedrocomitto.mlock.MlockProperties
import org.springframework.core.io.ClassPathResource
import org.springframework.data.redis.RedisConnectionFailureException
import org.springframework.data.redis.connection.RedisPassword
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.script.DefaultRedisScript
import org.springframework.scripting.support.ResourceScriptSource
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID
import kotlin.random.Random

@Component
class MlockImpl(mlockProperties: MlockProperties) : Mlock {

    private val redisTemplates = mlockProperties.nodes
        .map { redisNode ->
            JedisConnectionFactory(
                RedisStandaloneConfiguration(redisNode.host, redisNode.port)
                    .also { it.password = RedisPassword.of(redisNode.password) },
                JedisClientConfiguration.builder()
                    .readTimeout(Duration.ofMillis(500))
                    .build()
            )
        }.map { StringRedisTemplate(it) }

    private val redisScript: DefaultRedisScript<Long> = DefaultRedisScript<Long>().apply {
        setScriptSource(ResourceScriptSource(ClassPathResource("scripts/check-and-delete.lua")))
        resultType = Long::class.java
    }

    private val nodesToLock = redisTemplates.size / 2 + 1

    /**
     *
     */
    override fun tryLock(key: String, lockDuration: Duration): Boolean {
        val initialTime = System.currentTimeMillis()
        val value = UUID.randomUUID().toString()

        for (i in 1..5) {
            if (isLocked(key)) {
                return false
            }

            val lockedNodes = setIfAbsentOnEach(key, value, lockDuration).count { it }

            val totalTime = System.currentTimeMillis()

            if (lockedNodes < nodesToLock || (totalTime - initialTime) >= lockDuration.toMillis()) {
                unlockByValue(key, value)
                Thread.sleep(Random.nextLong(5, 150))
                continue
            }

            break
        }

        return true
    }

    override fun unlock(key: String) {
        deleteFromAll(key)
    }

    private fun unlockByValue(key: String, value: String) {
        redisTemplates.forEach { redisTemplate ->
            runCatching { redisTemplate.execute(redisScript, listOf(key), value) }
        }
    }

    private fun isLocked(key: String) =
        getFromAll(key).isNotEmpty()

    private fun setIfAbsentOnEach(key: String, value: String, lockDuration: Duration) =
        redisTemplates.mapNotNull { redisTemplate ->
            try {
                redisTemplate.opsForValue()
                    .setIfAbsent(key, value, lockDuration) ?: false
            } catch (e: RedisConnectionFailureException) {
                null
            }
        }

    private fun getFromAll(key: String) =
        redisTemplates.mapNotNull { redisTemplate ->
            try {
                redisTemplate.opsForValue().get(key)
            } catch (e: RedisConnectionFailureException) {
                null
            }
        }

    private fun deleteFromAll(key: String) =
        redisTemplates.forEach { redisTemplate ->
            redisTemplate.delete(key)
        }

}