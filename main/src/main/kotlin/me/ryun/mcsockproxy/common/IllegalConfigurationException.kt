package me.ryun.mcsockproxy.common

class IllegalConfigurationException internal constructor(message: String): RuntimeException("Illegal! $message")