package me.ryun.mcsockproxy.server

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler
import me.ryun.mcsockproxy.common.CraftConnectionConfiguration

internal class ServerInitializer(
    private val configuration: CraftConnectionConfiguration,
    private val path: String = "/"): ChannelInitializer<SocketChannel>() {

    override fun initChannel(channel: SocketChannel) {
        val pipeline = channel.pipeline()
        pipeline.addLast(HttpServerCodec())
        pipeline.addLast(HttpObjectAggregator(2048))
        pipeline.addLast(ServerPageHandler(path))
        pipeline.addLast(WebSocketServerCompressionHandler())
        pipeline.addLast(WebSocketServerProtocolHandler(path, "", true))
        pipeline.addLast(ServerFrameHandler())
        pipeline.addLast(CraftServerInbound(configuration))
    }
}