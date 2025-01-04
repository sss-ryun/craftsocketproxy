package me.ryun.mcsockproxy.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory
import io.netty.handler.codec.http.websocketx.WebSocketVersion
import me.ryun.mcsockproxy.common.CraftConnectionConfiguration
import me.ryun.mcsockproxy.common.CraftSocketConstants
import me.ryun.mcsockproxy.common.IllegalConfigurationException
import java.net.BindException
import java.net.ConnectException
import java.net.ServerSocket
import java.net.URI
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.pow

class ProxyClient private constructor(
    private val configuration: CraftConnectionConfiguration,
    private val path: String,
    private val useSecure: Boolean = false
) {

    private val group = NioEventLoopGroup()
    private var restartAttempts = 0
    private var successRestarts = 0
    private val maxFailedRestarts = 5
    private val maxRestarts = 5
    private var channel: Channel? = null
    private val outboundChannel = AtomicReference<Channel?>(null)
    private val packetQueue: Queue<Any> = ConcurrentLinkedQueue()
    private var backoffDelay = 1L

    init {
        if (configuration.host.isNullOrEmpty())
            throw IllegalConfigurationException("Host is not configured.")
        if (configuration.port == 0)
            throw IllegalConfigurationException("Port is not configured.")
        if (configuration.proxyPort == 0)
            throw IllegalConfigurationException("Proxy Port is not configured.")

        start()
    }

    companion object {
        fun serve(configuration: CraftConnectionConfiguration, path: String = "/", useSecure: Boolean = false): ProxyClient {
            return ProxyClient(configuration, path, useSecure)
        }

        fun scheduleRestart(proxyClient: ProxyClient) {
            proxyClient.group.schedule({
                if (!(proxyClient.group.isShutdown || proxyClient.group.isShuttingDown || proxyClient.group.isTerminated)) {
                    println(CraftSocketConstants.CONNECTION_RESTART)
                    proxyClient.start()
                }
            }, proxyClient.backoffDelay, TimeUnit.SECONDS)
            proxyClient.backoffDelay = (proxyClient.backoffDelay * 2).coerceAtMost(64)
        }

        fun releasePort(port: Int) {
            try {
                ServerSocket(port).close()
                println("Port $port released.")
            } catch (e: IOException) {
                println("Failed to release port $port: ${e.message}")
            }
        }
    }

    private fun start() {
        try {
            val scheme = if (useSecure) "wss" else "ws"
            val wsURI = URI("$scheme://${configuration.host}:${configuration.port}$path")

            println(CraftSocketConstants.CONNECTION_ATTEMPT + " $wsURI")

            val handler = ClientInboundConnectionHandler(
                WebSocketClientHandshakerFactory.newHandshaker(
                    wsURI,
                    WebSocketVersion.V13,
                    "",
                    true,
                    DefaultHttpHeaders()
                )
            ) { reconnect() }

            val bootstrap = Bootstrap()
            bootstrap.group(group)
                .channel(NioSocketChannel::class.java)
                .handler(ClientInitializer(handler, outboundChannel, configuration))

            channel = bootstrap.connect(wsURI.host, wsURI.port).sync().channel()
            handler.getHandshakeFuture().sync()

            channel?.closeFuture()?.addListener {
                if (it.isSuccess) {
                    successRestarts = 0
                }
                scheduleRestart(this)
            }

            println(
                CraftSocketConstants.PROXYING + ": " +
                    "${if (useSecure) "wss" else "ws"}://${configuration.host}:${configuration.port} -> localhost:${configuration.proxyPort}"
            )

            if (!isPortAvailable(configuration.proxyPort)) {
                println("Failed to bind to port ${configuration.proxyPort}. Retrying...")
                releasePort(configuration.proxyPort)
                scheduleRestart(this)
                return
            }

            ProxyCraftClient.serve(configuration, outboundChannel, channel, path, useSecure)
            backoffDelay = 1
        } catch (cause: Throwable) {
            when {
                cause is ConnectException && cause.message!!.contains("Connection refused") -> {
                    println(CraftSocketConstants.CONNECTION_FAILED)
                }
                cause is BindException && cause.message!!.contains("Address already in use") -> {
                    println(CraftSocketConstants.CONNECTION_BIND)
                    releasePort(configuration.proxyPort)
                    scheduleRestart(this)
                }
                else -> {
                    println("Unexpected error: ${cause.message}")
                    cause.printStackTrace()
                    scheduleRestart(this)
                }
            }
        }
    }

    private fun reconnect() {
        println("Attempting to reconnect to the WebSocket...")
        scheduleRestart(this)
    }

    fun shutdown() {
        group.shutdownGracefully()
        println(CraftSocketConstants.SHUTTING_DOWN)
    }

    private fun isPortAvailable(port: Int): Boolean {
        return try {
            ServerSocket(port).close()
            true
        } catch (e: IOException) {
            false
        }
    }
}
