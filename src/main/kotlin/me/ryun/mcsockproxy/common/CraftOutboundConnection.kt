package me.ryun.mcsockproxy.common

import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import java.util.concurrent.atomic.AtomicReference

class CraftOutboundConnection(private val outboundChannel: AtomicReference<Channel?>): ChannelInboundHandlerAdapter() {
    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        var decapsulatedMessage = message
        if(message is BinaryWebSocketFrame) decapsulatedMessage = message.content()
        val channel = outboundChannel.get()
        if(channel == null) {
            println("Not connected yet.")

            return
        }
        channel.writeAndFlush(decapsulatedMessage).addListener(ChannelFutureListener {
            if(it.isSuccess)
                context.channel().read()
            else
                it.channel().close()
        })
    }
}