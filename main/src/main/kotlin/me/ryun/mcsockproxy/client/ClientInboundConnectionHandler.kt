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
import java.util.Queue
import java.util.concurrent.ConcurrentLinkedQueue

internal class ClientInboundConnectionHandler(
    private val handshaker: WebSocketClientHandshaker,
    private val onReconnect: () -> Unit // Add reconnect function as a parameter
) : SimpleChannelInboundHandler<Any>() {
    private lateinit var handshakeFuture: ChannelPromise
    private val packetQueue: Queue<Any> = ConcurrentLinkedQueue()

    override fun channelRead0(context: ChannelHandlerContext, any: Any) {
        val channel = context.channel()
        if (!handshaker.isHandshakeComplete) {
            try {
                handshaker.finishHandshake(channel, any as FullHttpResponse)

                handshakeFuture.setSuccess()
                println(CraftSocketConstants.HANDSHAKE_COMPLETE)
            } catch (exception: WebSocketHandshakeException) {
                handshakeFuture.setFailure(exception)
                println(CraftSocketConstants.HANDSHAKE_FAILED)
                onReconnect()
            }
            return
        }

        when (any) {
            is FullHttpResponse -> {
                println(CraftSocketConstants.UNSUPPORTED_HTTP_RESPONSE)
                context.close()
            }
            is BinaryWebSocketFrame -> {
                packetQueue.offer(any.retain())
                context.fireChannelRead(any.retain())
            }
        }
    }

    override fun exceptionCaught(context: ChannelHandlerContext, cause: Throwable) {
        if (cause is SocketException && cause.message!!.contains("Connection reset")) {
            println(CraftSocketConstants.CONNECTION_TERMINATED)
            onReconnect()
        } else {
            println("Unhandled exception: ${cause.message}")
            cause.printStackTrace()
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
        onReconnect()
    }

    private fun forwardBufferedPackets(context: ChannelHandlerContext) {
        val channel = context.channel()
        while (!packetQueue.isEmpty()) {
            val packet = packetQueue.poll()
            channel.writeAndFlush(packet)
        }
    }
}
