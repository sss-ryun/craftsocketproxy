package me.ryun.mcsockproxy.client

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import me.ryun.mcsockproxy.common.CraftConnectionConfiguration
import me.ryun.mcsockproxy.common.CraftSocketConstants
import java.util.concurrent.atomic.AtomicReference

internal class ProxyCraftClient private constructor(
    configuration: CraftConnectionConfiguration,
    clientChannel: AtomicReference<Channel?>,
    websocketChannel: Channel?) {

    init {
        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup()

        try {
            val serverBootstrap = ServerBootstrap()
            serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(CraftClientInitializer(configuration, clientChannel, websocketChannel))

            val channelFuture = serverBootstrap.bind(configuration.proxyPort).sync()
            websocketChannel?.closeFuture()?.addListener {
                channelFuture.channel().close()
            }
            channelFuture.channel().closeFuture().sync()
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

    companion object {
        fun serve(configuration: CraftConnectionConfiguration, clientChannel: AtomicReference<Channel?>, websocketChannel: Channel? = null): ProxyCraftClient {
            return ProxyCraftClient(configuration, clientChannel, websocketChannel)
        }
    }

    private class CraftClientInitializer(private val configuration: CraftConnectionConfiguration, private val clientChannel: AtomicReference<Channel?>, private val websocketChannel: Channel? = null): ChannelInitializer<SocketChannel>() {
        override fun initChannel(channel: SocketChannel) {
            channel.pipeline().addLast(CraftClientInbound(configuration, clientChannel, websocketChannel))
        }
    }
}