package com.pedrocomitto.mlock.lock

import java.time.Duration

interface Lock {

    fun tryLock(key: String, lockDuration: Duration): Boolean

    fun unlock(key: String)
}