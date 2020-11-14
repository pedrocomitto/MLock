package com.pedrocomitto.mlock

import com.pedrocomitto.mlock.lock.MlockImpl
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty(prefix = "spring", value = ["mlock"], matchIfMissing = true)
@EnableConfigurationProperties(value = [MlockProperties::class])
class MlockAutoConfiguration {

    @Bean
    fun mlock(mlockProperties: MlockProperties) =
        MlockImpl(mlockProperties)
}