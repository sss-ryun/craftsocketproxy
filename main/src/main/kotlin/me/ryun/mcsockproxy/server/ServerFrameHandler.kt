package me.ryun.mcsockproxy.server

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.websocketx.*
import me.ryun.mcsockproxy.common.CraftSocketConstants
import java.net.SocketException
import java.nio.charset.Charset

internal class ServerFrameHandler: SimpleChannelInboundHandler<Any>() {

    override fun channelRead0(context: ChannelHandlerContext, frame: Any) {
        when(frame) {
            is PingWebSocketFrame -> context.writeAndFlush(PongWebSocketFrame(frame.content().retain()))
            is BinaryWebSocketFrame -> context.fireChannelRead(frame.retain())
            else -> {
                context.writeAndFlush(TextWebSocketFrame(Unpooled.copiedBuffer(CraftSocketConstants.UNSUPPORTED_WEBSOCKET_FRAME + frame.javaClass.name, Charset.defaultCharset())))
                context.disconnect()
                println(CraftSocketConstants.UNSUPPORTED_WEBSOCKET_FRAME + frame.javaClass.name)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(context: ChannelHandlerContext, cause: Throwable) {
        if(cause is SocketException && cause.message!!.contains("Connection reset")) {
            println(CraftSocketConstants.PLAYER_TERMINATED_WEBSOCKET)
        }
    }

    override fun channelInactive(context: ChannelHandlerContext) {
        context.disconnect()
        context.fireChannelInactive()
    }

    override fun userEventTriggered(context: ChannelHandlerContext, event: Any) {
        if(event is WebSocketServerProtocolHandler.HandshakeComplete)
            context.pipeline().remove(ServerPageHandler::class.java)
        else
            super.userEventTriggered(context, event)
    }
}