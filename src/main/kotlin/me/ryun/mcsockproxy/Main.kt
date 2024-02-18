package me.ryun.mcsockproxy

import me.ryun.mcsockproxy.common.CraftConnectionConfiguration
import me.ryun.mcsockproxy.server.ProxyServer

fun main(args: Array<String>) {
    val configuration = CraftConnectionConfiguration("localhost", 25566, 80)
    ProxyServer(configuration, "/minecraft")
}