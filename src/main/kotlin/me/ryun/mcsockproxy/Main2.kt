package me.ryun.mcsockproxy

import me.ryun.mcsockproxy.client.ProxyClient
import me.ryun.mcsockproxy.common.MinecraftConnectionConfiguration

fun main(args: Array<String>) {
    val configuration = MinecraftConnectionConfiguration("localhost", 25566, 25567)
    ProxyClient("example.com", 80, configuration, "/minecraft").start()
}