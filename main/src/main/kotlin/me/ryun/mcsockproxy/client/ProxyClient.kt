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
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class ProxyClient private constructor(
    private val configuration: CraftConnectionConfiguration,
    private val path: String,
    private val useWss: Boolean
) {
    private val group = NioEventLoopGroup()
    private var restartAttempts = 0
    private var successRestarts = 0
    private val maxFailedRestarts = 5
    private val maxRestarts = 10

    init {
        if (configuration.host.isNullOrEmpty()) throw IllegalConfigurationException("Host is not configured.")
        if (configuration.port == 0) throw IllegalConfigurationException("Port is not configured.")
        if (configuration.proxyPort == 0) throw IllegalConfigurationException("Proxy Port is not configured.")
        start()
    }

    companion object {
        fun serve(configuration: CraftConnectionConfiguration, path: String = "/", useWss: Boolean = false): ProxyClient {
            return ProxyClient(configuration, path, useWss)
        }
    }

    private fun start() {
        val outboundChannel = AtomicReference<Channel?>(null)
        try {
            val protocol = if (useWss) "wss" else "ws"
            val wsURI = URI("$protocol://${configuration.host}:${configuration.port}$path")

            println("${CraftSocketConstants.CONNECTION_ATTEMPT} $wsURI")

            val handler = ClientInboundConnectionHandler(
                WebSocketClientHandshakerFactory.newHandshaker(
                    wsURI,
                    WebSocketVersion.V13,
                    null,
                    true,
                    DefaultHttpHeaders()
                )
            )

            val bootstrap = Bootstrap()
            bootstrap.group(group)
                .channel(NioSocketChannel::class.java)
                .handler(ClientInitializer(handler, outboundChannel))

            val channel = bootstrap.connect(wsURI.host, wsURI.port).sync().channel()
            handler.getHandshakeFuture().sync()

            println("${CraftSocketConstants.PROXYING}: $protocol://${configuration.host}:${configuration.port} -> localhost:${configuration.proxyPort}")

            channel.closeFuture().addListener {
                if (it.isSuccess) {
                    successRestarts = 0
                }
                scheduleRestart()
            }

            ProxyCraftClient.serve(configuration, outboundChannel, channel)
        } catch (cause: Throwable) {
            when {
                cause is ConnectException && cause.message?.contains("Connection refused") == true -> {
                    println(CraftSocketConstants.CONNECTION_FAILED)
                }
                cause is BindException && cause.message?.contains("Address already in use") == true -> {
                    println(CraftSocketConstants.CONNECTION_BIND)
                    shutdown()
                }
                else -> {
                    println("Unexpected error: ${cause.message}")
                    cause.printStackTrace()
                }
            }
        }
    }

    fun shutdown() {
        group.shutdownGracefully()
        println(CraftSocketConstants.SHUTTING_DOWN)
    }

    private fun scheduleRestart() {
        if (maxFailedRestarts > restartAttempts++ && maxRestarts > successRestarts++) {
            println("${CraftSocketConstants.CONNECTION_RESTART} (Attempt $restartAttempts)")
            group.schedule({
                if (!(group.isShutdown || group.isShuttingDown || group.isTerminated)) {
                    start()
                }
            }, 1, TimeUnit.MILLISECONDS)
        } else {
            println(CraftSocketConstants.TOO_MANY_RESTARTS)
            shutdown()
        }
    }
}
