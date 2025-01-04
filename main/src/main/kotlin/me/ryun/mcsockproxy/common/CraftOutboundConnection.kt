package me.ryun.mcsockproxy.common

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import me.ryun.mcsockproxy.client.ProxyClient
import java.util.concurrent.atomic.AtomicReference

internal class CraftOutboundConnection(
    private val outboundChannel: AtomicReference<Channel?>,
    private val encapsulate: Boolean = false,
    private val configuration: CraftConnectionConfiguration // Add configuration parameter
) : ChannelInboundHandlerAdapter() {

    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        var packed = message

        if (!encapsulate) {
            if (message is BinaryWebSocketFrame)
                packed = message.content()
        } else {
            if (message is ByteBuf)
                packed = BinaryWebSocketFrame(message)
        }

        val channel = outboundChannel.get()

        if (channel == null) {
            println(CraftSocketConstants.CONNECTION_WAITING)
            return
        }

        channel.writeAndFlush(packed).addListener(ChannelFutureListener {
            if (it.isSuccess)
                context.channel().read()
            else
                it.channel().close()
        })
    }

    override fun channelInactive(context: ChannelHandlerContext) {
        context.disconnect()
        println(CraftSocketConstants.DISCONNECTED_CLIENT)
        context.close()
        ProxyClient.scheduleRestart(ProxyClient.serve(configuration, "/", false))  // Use appropriate parameters for ProxyClient
    }
}
