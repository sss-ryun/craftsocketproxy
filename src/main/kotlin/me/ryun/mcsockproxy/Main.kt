package me.ryun.mcsockproxy

import me.ryun.mcsockproxy.common.MinecraftConnectionConfiguration
import me.ryun.mcsockproxy.server.ProxyServer

fun main(args: Array<String>) {
    val configuration = MinecraftConnectionConfiguration("localhost", 25566, 80)
    ProxyServer(configuration, "/minecraft")
}