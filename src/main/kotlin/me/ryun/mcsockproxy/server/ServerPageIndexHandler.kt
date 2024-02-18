package me.ryun.mcsockproxy.server

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelFutureListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.handler.codec.http.*
import java.nio.charset.Charset

class ServerPageIndexHandler(val path: String): SimpleChannelInboundHandler<FullHttpRequest>() {

    companion object {
        fun sendHttpResponse(context: ChannelHandlerContext, request: FullHttpRequest, response: FullHttpResponse) {
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
    }
    override fun channelRead0(context: ChannelHandlerContext, request: FullHttpRequest) {
        if(request.decoderResult().isFailure) {
            sendHttpResponse(context, request, DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.BAD_REQUEST, context.alloc().buffer(0)))

            return
        }

        if(request.headers().contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true)) {
            context.fireChannelRead(request.retain())

            return
        }

        if(!HttpMethod.GET.equals(request.method())) {
            sendHttpResponse(context, request, DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.FORBIDDEN, context.alloc().buffer(0)))

            return
        }

        if(!request.uri().startsWith(path)) {
            val content = Unpooled.copiedBuffer("No content.", Charset.defaultCharset())
            val response = DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK, content)

            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8")
            HttpUtil.setContentLength(response, content.readableBytes().toLong())

            sendHttpResponse(context, request, response)
        } else
            sendHttpResponse(context, request, DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.NOT_FOUND, context.alloc().buffer(0)))
    }

    @Deprecated("Deprecated in Java")
    override fun exceptionCaught(context: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()
        context.close()
    }
}