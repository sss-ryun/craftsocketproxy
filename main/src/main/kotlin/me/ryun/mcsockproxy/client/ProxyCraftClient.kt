package me.ryun.mcsockproxy.client

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import me.ryun.mcsockproxy.common.CraftConnectionConfiguration
import me.ryun.mcsockproxy.common.CraftSocketConstants
import java.net.BindException  // Import BindException
import java.util.concurrent.atomic.AtomicReference

internal class ProxyCraftClient private constructor(
    private val configuration: CraftConnectionConfiguration,
    private val clientChannel: AtomicReference<Channel?>,
    private val websocketChannel: Channel?,
    private val path: String,
    private val useSecure: Boolean
) {

    init {
        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup()

        try {
            val serverBootstrap = ServerBootstrap()
            serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(CraftClientInitializer(configuration, clientChannel, websocketChannel, path, useSecure))

            val channelFuture = serverBootstrap.bind(configuration.proxyPort).sync()
            websocketChannel?.closeFuture()?.addListener {
                channelFuture.channel().close()
            }
            channelFuture.channel().closeFuture().sync()
        } catch (cause: Throwable) {
            when (cause) {
                is BindException -> {
                    println("Failed to bind to port ${configuration.proxyPort}. Retrying...")
                    ProxyClient.releasePort(configuration.proxyPort)  // Ensure ProxyClient.releasePort() is accessible
                    ProxyClient.scheduleRestart(ProxyClient.serve(configuration, path, useSecure))  // Ensure ProxyClient.scheduleRestart() is accessible
                }
                else -> {
                    println("Unexpected error: ${cause.message}")
                    cause.printStackTrace()
                }
            }
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

    companion object {
        fun serve(configuration: CraftConnectionConfiguration, clientChannel: AtomicReference<Channel?>, websocketChannel: Channel? = null, path: String, useSecure: Boolean): ProxyCraftClient {
            return ProxyCraftClient(configuration, clientChannel, websocketChannel, path, useSecure)
        }
    }

    private class CraftClientInitializer(
        private val configuration: CraftConnectionConfiguration,
        private val clientChannel: AtomicReference<Channel?>,
        private val websocketChannel: Channel? = null,
        private val path: String,
        private val useSecure: Boolean
    ) : ChannelInitializer<SocketChannel>() {
        override fun initChannel(channel: SocketChannel) {
            channel.pipeline().addLast(CraftClientInbound(configuration, clientChannel, websocketChannel, ProxyClient.serve(configuration, path, useSecure)))
        }
    }
}
