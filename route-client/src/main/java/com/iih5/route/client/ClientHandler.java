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

public class ClientHandler extends SimpleChannelInboundHandler<Object> {
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
            if (Client.protoType == ProtoType.TEXT) {
                if (Client.textLabels != null) {
                    String pack = Client.serverPwd+" # " + JSON.toJSONString(Client.textLabels);
                    ctx.channel().writeAndFlush(new TextWebSocketFrame(pack));
                }
            } else {
                if (Client.textLabels != null) {
                    ByteBuf data = Unpooled.buffer();
                    data.writeBytes(Utils.stringToBinary(Client.serverPwd));
                    data.writeBytes(Utils.stringToBinary(JSON.toJSONString(Client.textLabels)));
                    ctx.channel().writeAndFlush(new BinaryWebSocketFrame(data));
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
            ByteBuf content = bw.content();
            if (Client.clientHandler != null) {
                String label = Utils.binaryToString(content);
                Client.clientHandler.onMessage(label, content.array());
            }
        } else {
            String content = ((TextWebSocketFrame) frame).text();
            if (Client.clientHandler != null) {
                String arr[] = content.split(" # ", 2);
                if (arr != null && arr.length == 2) {
                    Client.clientHandler.onMessage(arr[0], arr[1]);
                }
            }
        }
    }
}
