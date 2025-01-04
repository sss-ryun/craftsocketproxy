package me.ryun.mcsockproxy.server

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.logging.LogLevel
import io.netty.handler.logging.LoggingHandler
import me.ryun.mcsockproxy.common.CraftConnectionConfiguration
import me.ryun.mcsockproxy.common.CraftOutboundConnection
import me.ryun.mcsockproxy.common.CraftSocketConstants
import java.net.SocketException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

internal class CraftServerInbound(
    private val configuration: CraftConnectionConfiguration): SimpleChannelInboundHandler<BinaryWebSocketFrame>() {
    private lateinit var channel: Channel
    private var isChannelFlushable = false

    override fun channelActive(context: ChannelHandlerContext) {
        println(CraftSocketConstants.PLAYER_CONNECTED)
        val bootstrap = Bootstrap()
        bootstrap.group(context.channel().eventLoop())
            .channel(NioSocketChannel::class.java)
            .handler(CraftServerHandler(context.channel(), configuration))

        val channelFuture = bootstrap.connect(configuration.host, configuration.port)
        channel = channelFuture.channel()
        channelFuture.addListener {future ->
            if(future.isSuccess) channelFuture.channel().flush()
            else { //Restart Minecraft Server connection after 100ms.
                println(CraftSocketConstants.CONNECTION_REFUSED)
                context.channel().eventLoop().schedule({context.disconnect()}, 10, TimeUnit.SECONDS)
            }

            isChannelFlushable = future.isSuccess
        }
    }

    override fun channelInactive(context: ChannelHandlerContext) {
        channel.close()
        context.disconnect()
        println(CraftSocketConstants.PLAYER_DISCONNECTED)
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if(cause is SocketException && cause.message!!.contains("Connection reset")) {
            println(CraftSocketConstants.PLAYER_TERMINATED)
        }
    }

    override fun channelRead0(context: ChannelHandlerContext, binary: BinaryWebSocketFrame) {
        channel.write(binary.content())
        if(isChannelFlushable) channel.flush()
    }

    private class CraftServerHandler(
        private val channel: Channel,
        private val configuration: CraftConnectionConfiguration // Add configuration parameter
    ): ChannelInitializer<SocketChannel>() {
        override fun initChannel(channel: SocketChannel) {
            channel.pipeline().addLast(CraftOutboundConnection(AtomicReference<Channel?>(this.channel), true, configuration))
        }
    }
}
