package me.ryun.mcsockproxy.client

import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.HttpClientCodec
import io.netty.handler.codec.http.HttpObjectAggregator
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler
import me.ryun.mcsockproxy.common.CraftOutboundConnection
import java.util.concurrent.atomic.AtomicReference

/**
 * Initializes the Client by setting the flow of the content from the server to ClientInboundConnectionHandler to
 * CraftOutboundConnection.
 */
internal class ClientInitializer(
    private val handler: ClientInboundConnectionHandler,
    private val clientChannel: AtomicReference<Channel?>): ChannelInitializer<SocketChannel>() {

    /**
     * Called when the Channel is initialized for the first time.
     */
    override fun initChannel(channel: SocketChannel) {
        channel.pipeline().addLast(
            HttpClientCodec(),
            HttpObjectAggregator(2048),
            WebSocketClientCompressionHandler.INSTANCE,
            handler
        )
        channel.pipeline().addLast(CraftOutboundConnection(clientChannel))
    }
}