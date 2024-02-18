package me.ryun.mcsockproxy.client

import io.netty.bootstrap.Bootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import me.ryun.mcsockproxy.common.CraftConnectionConfiguration
import me.ryun.mcsockproxy.common.CraftOutboundConnection
import java.util.concurrent.atomic.AtomicReference

/**
 * From client
 */
class CraftClientInbound(private val configuration: CraftConnectionConfiguration, private val clientChannel: AtomicReference<Channel?>, private val websocketChannel: Channel? = null): ChannelInboundHandlerAdapter() {
    private lateinit var channel: Channel
    private var isChannelFlushable = false
    override fun channelActive(context: ChannelHandlerContext) {
        clientChannel.set(context.channel())
        if(websocketChannel != null) {
            println("Connected as a WebSocket Proxy.")
            channel = websocketChannel
            isChannelFlushable = true
        } else {
            val bootstrap = Bootstrap()
            bootstrap.group(context.channel().eventLoop())
                .channel(NioSocketChannel::class.java)
                .handler(CraftClientHandler(context.channel()))
            println("Connected as a Transparent Minecraft Proxy.")
            val channelFuture = bootstrap.connect(configuration.host, configuration.port)
            channel = channelFuture.channel()
            channelFuture.addListener { future ->
                if(future.isSuccess) channel.flush()
                isChannelFlushable = future.isSuccess
            }
        }
        println("Serving: localhost:" + configuration.proxyPort)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if(msg is ByteBuf) {
            if(websocketChannel != null && channel.isActive)
                channel.write(BinaryWebSocketFrame(msg))
            else
                channel.write(msg)
            if(isChannelFlushable) channel.flush()
        }
    }

    override fun channelInactive(context: ChannelHandlerContext) {
        context.disconnect()
        channel.disconnect()
        println("Minecraft Client disconnected.")
        context.close()
    }

    private class CraftClientHandler(private val channel: Channel): ChannelInitializer<SocketChannel>() {
        override fun initChannel(channel: SocketChannel) {
            channel.pipeline().addLast(CraftOutboundConnection(AtomicReference<Channel?>(this.channel)))
        }
    }
}