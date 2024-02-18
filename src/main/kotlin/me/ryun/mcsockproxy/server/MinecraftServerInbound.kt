package me.ryun.mcsockproxy.server

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import me.ryun.mcsockproxy.common.MinecraftConnectionConfiguration
import me.ryun.mcsockproxy.common.MinecraftOutboundConnection
import java.net.SocketException
import java.util.concurrent.atomic.AtomicReference

class MinecraftServerInbound(private val configuration: MinecraftConnectionConfiguration): SimpleChannelInboundHandler<BinaryWebSocketFrame>() {
    private lateinit var channel: Channel
    private var isChannelFlushable = false

    override fun channelActive(context: ChannelHandlerContext) {
        println("Player connected.")
        val bootstrap = Bootstrap()
        bootstrap.group(context.channel().eventLoop())
            .channel(NioSocketChannel::class.java)
            .handler(MinecraftServerHandler(context.channel()))

        val channelFuture = bootstrap.connect(configuration.host, configuration.port)
        channel = channelFuture.channel()
        channelFuture.addListener {future ->
            if(future.isSuccess) channelFuture.channel().flush()
            //else context.disconnect() <- Fix restart loop.
            isChannelFlushable = future.isSuccess
        }
    }

    override fun channelInactive(context: ChannelHandlerContext) {
        channel.close()
        context.disconnect()
        println("Player disconnected.")
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if(cause is SocketException && cause.message!!.contains("Connection reset")) {
            println("Player connection terminated.")
        }
    }

    override fun channelRead0(context: ChannelHandlerContext, binary: BinaryWebSocketFrame) {
        channel.write(binary.content())
        if(isChannelFlushable) channel.flush()
    }

    private class MinecraftServerHandler(private val channel: Channel): ChannelInitializer<SocketChannel>() {
        override fun initChannel(channel: SocketChannel) {
            channel.pipeline().addLast(MinecraftOutboundConnection(AtomicReference<Channel?>(this.channel)))
        }
    }
}