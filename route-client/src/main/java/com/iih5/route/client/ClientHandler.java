package com.iih5.route.client;

import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientHandler extends SimpleChannelInboundHandler<Object> {
    private static Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;


    public ClientHandler(WebSocketClientHandshaker handshaker) {
        this.handshaker = handshaker;
    }

    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    public void channelInactive(ChannelHandlerContext ctx) {
        Client.channel = null;
        Client.clientHandler.disconnect(ctx.channel());
    }

    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();

            Client.channel = ch;
            Client.clientHandler.connect(ctx.channel());
            if (Client.textLabels != null) {
                String pack = "~#" + JSON.toJSONString(Client.textLabels);
                ctx.channel().writeAndFlush(new TextWebSocketFrame(pack));
            }
            if (Client.binaryLabels != null) {
                try {
                    byte[] head = "~".getBytes("UTF-8");
                    ByteBuf data = Unpooled.buffer();
                    data.writeShort(head.length);
                    data.writeBytes(head);
                    data.writeShort(Client.binaryLabels.length);
                    data.writeBytes(Client.binaryLabels);
                    ctx.channel().writeAndFlush(new BinaryWebSocketFrame(data));
                } catch (Exception e) {
                    logger.error("订阅失败", e);
                }
            }
            return;
        }
        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame bw = (BinaryWebSocketFrame) frame;
            ByteBuf data = bw.content();
            short length = data.readShort();
            byte[] arr0 = data.readBytes(length).array();
            if (Client.clientHandler != null) {
                Client.clientHandler.onMessage(arr0, data.array());
            }
        } else {
            String request = ((TextWebSocketFrame) frame).text();
            String arr[] = request.split("#", 2);
            if (arr != null && arr.length == 2) {
                if (Client.clientHandler != null) {
                    Client.clientHandler.onMessage(arr[0], arr[1]);
                }
            }
        }
    }
}
