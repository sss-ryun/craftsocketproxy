package me.ryun.mcsockproxy

import me.ryun.mcsockproxy.client.ProxyClient
import me.ryun.mcsockproxy.common.CraftConnectionConfiguration
import me.ryun.mcsockproxy.server.ProxyServer

val Boolean.int
    get() = this.compareTo(false)

const val VERSION = "1.1.3"

fun main(args: Array<String>) {
    val queryVersion = args.contains("--version")

    if (queryVersion) {
        println(VERSION)
        return
    }

    val doServer = args.contains("--s")
    val doClientProxy = args.contains("--c")
    val useSecure = args.contains("--wss") 

    if ((doServer.int + doClientProxy.int) > 1) {
        println("Ambiguous arguments. You cannot start both a proxy and a server in the same instance.")
        return
    }

    if (!doServer && !doClientProxy) {
        println(
            """
                Craft Socket Proxy
                Version: $VERSION
                Author: SSS Ryun (sss-ryun)
                Contributor: BedsDrout (bedsaredragons)
            """.trimIndent()
        )
        printHelp()
        return
    }

    if (!requireString(args, "-host", "Missing or invalid hostname."))
        return

    if (!requireInt(args, "-port", "Missing or invalid port."))
        return

    if (!requireInt(args, "-proxy", "Missing or invalid proxy port."))
        return

    val hasPath = hasValidString(args, "-path")
    val hostname = getString(args, "-host")
    val port = getInt(args, "-port")
    val proxyPort = getInt(args, "-proxy")
    val path = if (hasPath) getString(args, "-path") else "/"

    if (doServer) {
        println("Starting proxy server...")
        val config = CraftConnectionConfiguration(proxyPort, hostname, port)
        ProxyServer.serve(config, path)
    } else {
        println("Starting proxy client...")
        val config = CraftConnectionConfiguration(proxyPort, hostname, port)
        ProxyClient.serve(config, path, useSecure) 
    }
}

fun printHelp() {
    println(
        """
        Arguments:
            --c               | Start a Client Proxy
            --s               | Start a Server Proxy
            -host  <Hostname> | Hostname
            -port  <Port>     | Port of Host
            -proxy <Port>     | Output port of Proxy
            -path  <Path>     | (Optional) Path of WebSocket connection
            --wss             | Use secure WebSocket (wss://)
            --version         | Query version
        """.trimIndent()
    )
}

fun requireInt(args: Array<String>, arg: String, message: String): Boolean {
    val hasArg = args.contains(arg)
    val argIndex = args.indexOf(arg) + 1
    val isValidInt = (argIndex < args.size) && args[argIndex].toIntOrNull() != null

    if (!hasArg || !isValidInt) {
        println(message)
        printHelp()
        return false
    }

    return true
}

fun getInt(args: Array<String>, arg: String): Int {
    return args[args.indexOf(arg) + 1].toInt()
}

fun requireString(args: Array<String>, arg: String, message: String): Boolean {
    if (!hasValidString(args, arg)) {
        println(message)
        printHelp()
        return false
    }

    return true
}

fun hasValidString(args: Array<String>, arg: String): Boolean {
    val hasArg = args.contains(arg)
    if (!hasArg) return false
    val argIndex = args.indexOf(arg) + 1
    val isValidString = (argIndex < args.size) && args[argIndex].toIntOrNull() == null

    return isValidString
}

fun getString(args: Array<String>, arg: String): String {
    return args[args.indexOf(arg) + 1]
}
