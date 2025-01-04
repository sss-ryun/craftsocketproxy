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

/**
 * Receives a WebSocket connection from a WebSocket server and passes the content to CraftOutboundConnection.
 */
internal class ClientInboundConnectionHandler(
    private val handshaker: WebSocketClientHandshaker): SimpleChannelInboundHandler<Any>() {
    private lateinit var handshakeFuture: ChannelPromise

    /**
     * Called when content is received.
     */
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

    /**
     * Throws an exception if there is an exception.
     */
    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(context: ChannelHandlerContext, cause: Throwable) {
        if(cause is SocketException && cause.message!!.contains("Connection reset")) {
            println(CraftSocketConstants.CONNECTION_TERMINATED)
        }
    }

    /**
     * Returns the HandshakeFuture without guaranteeing if its initialized or valid.
     */
    fun getHandshakeFuture(): ChannelFuture {
        return handshakeFuture
    }

    /**
     * Called each time a Handler is added.
     */
    override fun handlerAdded(context: ChannelHandlerContext) {
        handshakeFuture = context.newPromise()
    }

    /**
     * Called once the Channel has an active connection.
     */
    override fun channelActive(context: ChannelHandlerContext) {
        handshaker.handshake(context.channel())
    }

    /**
     * Called when the Channel disconnects.
     */
    override fun channelInactive(context: ChannelHandlerContext) {
        context.disconnect()
        context.fireChannelInactive()
        println(CraftSocketConstants.DISCONNECTED_WEBSOCKET)
    }
}