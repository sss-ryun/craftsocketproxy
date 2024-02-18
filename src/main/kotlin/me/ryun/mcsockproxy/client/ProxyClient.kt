package me.ryun.mcsockproxy.client

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.DefaultHttpHeaders
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory
import io.netty.handler.codec.http.websocketx.WebSocketVersion
import me.ryun.mcsockproxy.common.CraftConnectionConfiguration
import java.net.ConnectException
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

class ProxyClient(val host: String, val port: Int, val connectionConfiguration: CraftConnectionConfiguration, val path: String = "/") {
    private val group = NioEventLoopGroup()
    private var restartAttempts = 0
    private var maxRestarts = 5
    fun start() { //TODO: Sanitize configuration
        //TODO: Give client a unique ID
        val wsURI = URI("ws://$host:$port$path")
        println("Attempting connection... $wsURI")

        try {
            val handler = ClientInboundConnectionHandler(WebSocketClientHandshakerFactory.newHandshaker(wsURI, WebSocketVersion.V13, "", true, DefaultHttpHeaders()))
            val atomicChannel = AtomicReference<Channel?>(null)
            val craftClientProxy = CraftClientProxy(connectionConfiguration, atomicChannel)

            val bootstrap = Bootstrap()
            bootstrap.group(group)
                .channel(NioSocketChannel::class.java)
                .handler(ClientInitializer(handler, atomicChannel));

            val channel = bootstrap.connect(wsURI.host, wsURI.port).sync().channel()
            handler.getHandshakeFuture().sync()
            craftClientProxy.websocketChannel = channel
            craftClientProxy.start() //TODO: Handle connection timeouts

            channel.closeFuture().addListener {
                println("Restarting...")
                scheduleRestart()
            }
        } catch(cause: Throwable) {
            if(cause is ConnectException && cause.message!!.contains("Connection refused")) {
                println("Failed to connect to host.")
            }
        }
    }

    fun shutdown() {
        group.shutdownGracefully()
        println("ProxyClient shutting down.")
    }

    private fun scheduleRestart() {
        if(maxRestarts > restartAttempts++) {
            restartAttempts = 0
            group.schedule({start()}, 1, TimeUnit.MILLISECONDS)
        } else {
            println("Too many failed restart attempts.")
            shutdown()
        }
    }
}