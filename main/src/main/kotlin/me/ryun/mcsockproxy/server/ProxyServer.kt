package me.ryun.mcsockproxy.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import me.ryun.mcsockproxy.common.CraftConnectionConfiguration
import me.ryun.mcsockproxy.common.CraftSocketConstants
import me.ryun.mcsockproxy.common.IllegalConfigurationException

class ProxyServer private constructor(configuration: CraftConnectionConfiguration, path: String) {
    init {
        if(configuration.host.isNullOrEmpty())
            throw IllegalConfigurationException("Host is not configured.")
        if(configuration.port == 0)
            throw IllegalConfigurationException("Port is not configured.")
        if(configuration.proxyPort == 0)
            throw IllegalConfigurationException("Proxy Port is not configured.")

        val bossGroup = NioEventLoopGroup(1)
        val workerGroup = NioEventLoopGroup()
        val bootstrap = ServerBootstrap()

        try {
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(ServerInitializer(configuration, path))

            val channel = bootstrap.bind(configuration.proxyPort).sync().channel()

            println(CraftSocketConstants.PROXYING + ": " + configuration.host + ":" + configuration.port + " -> ws://localhost:${configuration.proxyPort}$path")

            channel.closeFuture().sync()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }

    companion object {
        fun serve(configuration: CraftConnectionConfiguration, path: String = "/"): ProxyServer {
            return ProxyServer(configuration, path)
        }
    }
}