package me.ryun.mcsockproxy

import me.ryun.mcsockproxy.client.ProxyClient
import me.ryun.mcsockproxy.common.CraftConnectionConfiguration

fun main(args: Array<String>) {
    val configuration = CraftConnectionConfiguration("localhost", 25566, 25567)
    ProxyClient("example.com", 80, configuration, "/minecraft").start()
}