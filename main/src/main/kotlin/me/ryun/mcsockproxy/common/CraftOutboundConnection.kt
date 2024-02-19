package me.ryun.mcsockproxy.common

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import java.util.concurrent.atomic.AtomicReference

internal class CraftOutboundConnection(
    private val outboundChannel: AtomicReference<Channel?>,
    private val encapsulate: Boolean = false): ChannelInboundHandlerAdapter() {

    override fun channelRead(context: ChannelHandlerContext, message: Any) {
        var packed = message

        if(!encapsulate) {
            if(message is BinaryWebSocketFrame)
                packed = message.content()
        } else {
            if(message is ByteBuf)
                packed = BinaryWebSocketFrame(message)
        }

        val channel = outboundChannel.get()

        if(channel == null) {
            println(CraftSocketConstants.CONNECTION_WAITING)

            return
        }

        channel.writeAndFlush(packed).addListener(ChannelFutureListener {
            if(it.isSuccess)
                context.channel().read()
            else
                it.channel().close()
        })
    }
}