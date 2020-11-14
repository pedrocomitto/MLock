package com.pedrocomitto.mlock.lock

import java.time.Duration

interface Mlock {

    fun tryLock(key: String, lockDuration: Duration): Boolean

    fun unlock(key: String)
}