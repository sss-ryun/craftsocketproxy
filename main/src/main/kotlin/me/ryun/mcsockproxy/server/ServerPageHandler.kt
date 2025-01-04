package me.ryun.mcsockproxy.server

import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*

/**
 * A Handler for all HTTP requests to the server. Responds Forbidden to all non-WebSocket requests.
 */
internal class ServerPageHandler(private val path: String = "/"): SimpleChannelInboundHandler<FullHttpRequest>() {

    override fun channelRead0(context: ChannelHandlerContext, request: FullHttpRequest) {
        if(request.decoderResult().isFailure) {
            sendHttpResponse(
                context,
                request,
                DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.BAD_REQUEST, context.alloc().buffer(0))
            )

            return
        }

        if(request.headers().contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true)) {
            context.fireChannelRead(request.retain())

            return
        }

        //Respond with Forbidden to all requests outside the specified WebSocket path.
        sendHttpResponse(
            context,
            request,
            DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.FORBIDDEN, context.alloc().buffer(0))
        )
    }

    /**
     * Helper method to send a HttpResponse.
     */
    private fun sendHttpResponse(context: ChannelHandlerContext, request: FullHttpRequest, response: FullHttpResponse) {
        val status = response.status()

        if(status.code() != 200) {
            ByteBufUtil.writeUtf8(response.content(), status.toString())
            HttpUtil.setContentLength(response, response.content().readableBytes().toLong())
        }

        val isKeepAlive = HttpUtil.isKeepAlive(request) && status.code() == 200
        HttpUtil.setKeepAlive(response, isKeepAlive)
        val future = context.writeAndFlush(response)
        if(!isKeepAlive)
            future.addListener(ChannelFutureListener.CLOSE)
    }

    /**
     * Closes the connection if an exception occurs.
     */
    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(context: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        context.close()
    }
}