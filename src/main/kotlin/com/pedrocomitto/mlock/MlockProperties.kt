package com.pedrocomitto.mlock

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("spring.mlock")
data class MlockProperties(val nodes: List<RedisNode>)

@ConstructorBinding
data class RedisNode(val host: String, val port: Int, val password: String)
