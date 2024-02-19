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
    private val path: String) {

    private val group = NioEventLoopGroup()
    private var restartAttempts = 0
    private var successRestarts = 0
    private var maxFailedRestarts = 5
    private var maxRestarts = 5

    init {
        if(configuration.host.isNullOrEmpty())
            throw IllegalConfigurationException("Host is not configured.")
        if(configuration.port == 0)
            throw IllegalConfigurationException("Port is not configured.")
        if(configuration.proxyPort == 0)
            throw IllegalConfigurationException("Proxy Port is not configured.")

        start()
    }

    companion object {
        fun serve(configuration: CraftConnectionConfiguration, path: String = "/"): ProxyClient {
            return ProxyClient(configuration, path)
        }
    }

    private fun start() {
        //TODO: Give client a unique ID
        val outboundChannel = AtomicReference<Channel?>(null)

        try {
            var channel: Channel? = null

            if(configuration.port == 80 || configuration.port == 443) {
                val wsURI = URI("ws://" + configuration.host + ":" + configuration.port + path)

                println(CraftSocketConstants.CONNECTION_ATTEMPT + " $wsURI")

                val handler = ClientInboundConnectionHandler(
                    WebSocketClientHandshakerFactory.newHandshaker(
                        wsURI,
                        WebSocketVersion.V13,
                        "",
                        true,
                        DefaultHttpHeaders()
                    )
                )

                val bootstrap = Bootstrap()
                bootstrap.group(group)
                    .channel(NioSocketChannel::class.java)
                    .handler(ClientInitializer(handler, outboundChannel));

                channel = bootstrap.connect(wsURI.host, wsURI.port).sync().channel()
                handler.getHandshakeFuture().sync()

                //TODO: Handle connection timeouts

                channel.closeFuture().addListener {
                    if(it.isSuccess) {
                        successRestarts = 0
                    }
                    scheduleRestart()
                }
            }

            println(CraftSocketConstants.PROXYING + ": " + (if(channel != null) "ws://" else "") + configuration.host + ":" + configuration.port + " -> localhost:" + configuration.proxyPort)

            ProxyCraftClient.serve(configuration, outboundChannel, channel)
        } catch(cause: Throwable) {
            if(cause is ConnectException && cause.message!!.contains("Connection refused")) {
                println(CraftSocketConstants.CONNECTION_FAILED)
            } else if(cause is BindException && cause.message!!.contains("Address already in use")) {
                println(CraftSocketConstants.CONNECTION_BIND)
                shutdown()
            } else cause.printStackTrace()
        }
    }

    fun shutdown() {
        group.shutdownGracefully()
        println(CraftSocketConstants.SHUTTING_DOWN)
    }

    private fun scheduleRestart() {
        if(maxFailedRestarts > restartAttempts++ && maxRestarts > successRestarts++) {
            restartAttempts = 0
            group.schedule({
                if(!(group.isShutdown || group.isShuttingDown || group.isTerminated)) {
                    println(CraftSocketConstants.CONNECTION_RESTART)
                    start()
                }
            }, 1, TimeUnit.MILLISECONDS)
        } else {
            successRestarts = 0
            println(CraftSocketConstants.TOO_MANY_RESTARTS)
            shutdown()
        }
    }
}