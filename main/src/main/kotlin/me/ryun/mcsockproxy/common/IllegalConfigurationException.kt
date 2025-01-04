package me.ryun.mcsockproxy.common

/**
 * Thrown if illegal or incompatible configurations are set.
 */
class IllegalConfigurationException internal constructor(message: String): RuntimeException("Illegal! $message")