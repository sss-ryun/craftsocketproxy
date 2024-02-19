package me.ryun.mcsockproxy.client

import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelPromise
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException
import me.ryun.mcsockproxy.common.CraftSocketConstants
import java.net.SocketException

internal class ClientInboundConnectionHandler(
    private val handshaker: WebSocketClientHandshaker): SimpleChannelInboundHandler<Any>() {
    private lateinit var handshakeFuture: ChannelPromise

    override fun channelRead0(context: ChannelHandlerContext, any: Any) {
        val channel = context.channel()
        if(!handshaker.isHandshakeComplete) {
            try {
                handshaker.finishHandshake(channel, any as FullHttpResponse)

                handshakeFuture.setSuccess()

                println(CraftSocketConstants.HANDSHAKE_COMPLETE)
            } catch (exception: WebSocketHandshakeException) {
                handshakeFuture.setFailure(exception)

                println(CraftSocketConstants.HANDSHAKE_FAILED)
            }

            return
        }

        when(any) {
            is FullHttpResponse -> {
                println(CraftSocketConstants.UNSUPPORTED_HTTP_RESPONSE)
                context.close()
            }
            is BinaryWebSocketFrame -> context.fireChannelRead(any.retain())
        }
    }
    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(context: ChannelHandlerContext, cause: Throwable) {
        if(cause is SocketException && cause.message!!.contains("Connection reset")) {
            println(CraftSocketConstants.CONNECTION_TERMINATED)
        }
    }

    fun getHandshakeFuture(): ChannelFuture {
        return handshakeFuture
    }

    override fun handlerAdded(context: ChannelHandlerContext) {
        handshakeFuture = context.newPromise()
    }

    override fun channelActive(context: ChannelHandlerContext) {
        handshaker.handshake(context.channel())
    }

    override fun channelInactive(context: ChannelHandlerContext) {
        context.disconnect()
        context.fireChannelInactive()
        println(CraftSocketConstants.DISCONNECTED_WEBSOCKET)
    }
}