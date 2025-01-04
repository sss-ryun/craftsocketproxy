package me.ryun.mcsockproxy.common

import io.netty.buffer.ByteBuf
import io.netty.channel.Channel
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import java.util.concurrent.atomic.AtomicReference

/**
 * Encapsulates a ByteBuf into a BinaryWebSocketFrame before sending it out and decapsulates a BinaryWebSocketFrame
 * before sending it out. This is used in both the Client and the Server.
 *
 */
internal class CraftOutboundConnection(
    /**
     * The Channel object which is atomic for thread safety.
     */
    private val outboundChannel: AtomicReference<Channel?>,
    /**
     * Sets whether to Encapsulate ByteBuf to a BinaryWebSocketFrame or Decapsulate a BinaryWebSocketFrame to a ByteBuf.
     */
    private val encapsulate: Boolean = false): ChannelInboundHandlerAdapter() {

    /**
     * Called when an incoming content is received.
     */
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