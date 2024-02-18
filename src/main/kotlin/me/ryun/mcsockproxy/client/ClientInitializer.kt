package me.ryun.mcsockproxy.client

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler
import me.ryun.mcsockproxy.common.MinecraftOutboundConnection
import java.util.concurrent.atomic.AtomicReference

class ClientInitializer(private val handler: ClientInboundConnectionHandler, private val clientChannel: AtomicReference<Channel?>): ChannelInitializer<SocketChannel>() {
    override fun initChannel(channel: SocketChannel) {
        channel.pipeline().addLast(
            HttpClientCodec(),
            HttpObjectAggregator(8192),
            WebSocketClientCompressionHandler.INSTANCE,
            handler
        )
        channel.pipeline().addLast(MinecraftOutboundConnection(clientChannel))
    }
}