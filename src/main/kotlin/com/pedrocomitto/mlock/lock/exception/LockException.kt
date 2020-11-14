package com.pedrocomitto.klock.lock.exception

import java.lang.RuntimeException

class LockException : RuntimeException("Failed to obtain lock")
