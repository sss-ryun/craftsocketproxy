package me.ryun.mcsockproxy.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import me.ryun.mcsockproxy.common.CraftConnectionConfiguration

class ProxyServer(configuration: CraftConnectionConfiguration, path: String = "/") {
    init { //TODO: Sanitize configuration
        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup()
        val bootstrap = ServerBootstrap()

        try {
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(ServerInitializer(configuration, path))

            val channel = bootstrap.bind(configuration.proxyPort).sync().channel()

            println("Serving: ws://localhost:${configuration.proxyPort}$path")

            channel.closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }
}