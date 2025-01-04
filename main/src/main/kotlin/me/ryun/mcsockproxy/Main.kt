package me.ryun.mcsockproxy

import me.ryun.mcsockproxy.client.ProxyClient
import me.ryun.mcsockproxy.common.CraftConnectionConfiguration
import me.ryun.mcsockproxy.server.ProxyServer

/**
 * Add your name here if you're contributing.
 */
val authorList = listOf<String>("SSS Ryun (sss-ryun)")

/**
 * Returns 1 for True and 0 for False.
 */
val Boolean.int
    get() = this.compareTo(false)

//Constant values
const val VERSION = "1.0.1"
const val INVALID_HOSTNAME = "Missing or invalid hostname."
const val INVALID_PORT = "Missing or invalid port."
const val INVALID_PROXY_PORT = "Missing or invalid proxy port."
const val STARTING_SERVER = "Starting proxy server..."
const val STARTING_CLIENT = "Starting proxy client..."

/**
 * The main code. This is where the code is executed for the standalone version
 */
fun main(args: Array<String>) {
    val queryVersion = args.contains("--version")

    if(queryVersion) {
        println(VERSION)

        return
    }

    val doServer = args.contains("--s")
    val doClientProxy = args.contains("--c")

    if((doServer.int + doClientProxy.int) > 1) {
        println("Ambiguous arguments. You cannot start both a proxy and a server in the same instance.")

        return
    }

    if(!doServer && !doClientProxy) {
        println(
            """
                Craft Socket Proxy
                Version: $VERSION
                Authors: ${authorList.joinToString(", ")}
                
            """.trimIndent()
        )
        printHelp()

        return
    }

    if(!requireString(args, "-host", INVALID_HOSTNAME))
        return

    if(!requireInt(args, "-port", INVALID_PORT))
        return

    if(!requireInt(args, "-proxy", INVALID_PROXY_PORT))
        return

    val hasPath = hasValidString(args, "-path")

    val hostname = getString(args, "-host")
    val port = getInt(args, "-port")
    val proxyPort = getInt(args, "-proxy")
    val path = if(hasPath) getString(args, "-path") else "/"

    if(doServer) {
        println(STARTING_SERVER)
        val config = CraftConnectionConfiguration(proxyPort, hostname, port)
        ProxyServer.serve(config, path)
    } else {
        println(STARTING_CLIENT)
        val config = CraftConnectionConfiguration(proxyPort, hostname, port)
        ProxyClient.serve(config, path)
    }
}

/**
 * Prints a default help output.
 */
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
            --version         | Query version
        """.trimIndent()
    )
}

/**
 * Validates if the required Int value is valid and available for use.
 */
fun requireInt(args: Array<String>, arg: String, message: String): Boolean {
    val hasArg = args.contains(arg)
    val argIndex = args.indexOf(arg) + 1
    val isValidInt = (argIndex < args.size) && args[argIndex].toIntOrNull() != null

    if(!hasArg || !isValidInt) {
        println(message)
        printHelp()
        return false
    }

    return true
}

/**
 * Returns the Int after the `arg` value.
 */
fun getInt(args: Array<String>, arg: String): Int {
    return args[args.indexOf(arg) + 1].toInt()
}

/**
 * Validates if the required String value is valid and available for use.
 */
fun requireString(args: Array<String>, arg: String, message: String): Boolean {
    if(!hasValidString(args, arg)) {
        println(message)
        printHelp()
        return false
    }

    return true
}

/**
 * Checks if the String value after the `arg` value is valid.
 */
fun hasValidString(args: Array<String>, arg: String): Boolean {
    val hasArg = args.contains(arg)
    if(!hasArg)
        return false
    val argIndex = args.indexOf(arg) + 1
    val isValidString = (argIndex < args.size) && args[argIndex].toIntOrNull() == null

    return isValidString
}

/**
 * Returns the String after the `arg` value.
 */
fun getString(args: Array<String>, arg: String): String {
    return args[args.indexOf(arg) + 1]
}