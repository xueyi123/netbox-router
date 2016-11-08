package com.iih5.router.handler;

import com.alibaba.fastjson.JSON;
import com.iih5.route.server.Handler;
import com.iih5.route.server.SessionManager;
import com.iih5.router.Main;
import com.iih5.router.constant.Constant;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class ServerSessionHandler extends Handler {
    private static Logger logger = LoggerFactory.getLogger(ServerSessionHandler.class);
    private static final String WEBSOCKET_PATH = "/websocket";
    private WebSocketServerHandshaker handshaker;

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("建立连接");
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("断开连接,解除session");
        SessionManager.getInstance().deleteSession(ctx.channel());
    }

    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.close();
    }

    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            try {
                handleWebSocketFrame(ctx, (WebSocketFrame) msg);
            } catch (Exception e) {
                logger.error("读操作异常：", e);
            }
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }
        if ("/".equals(req.getUri())) {
            ByteBuf content = TestPage.getContent(getWebSocketLocation(req));
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

            res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
            HttpHeaders.setContentLength(res, content.readableBytes());

            sendHttpResponse(ctx, req, res);
            return;
        }
        if ("/favicon.ico".equals(req.getUri())) {
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            sendHttpResponse(ctx, req, res);
            return;
        }
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), req);
        }
    }

    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(res, res.content().readableBytes());
        }
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HOST) + WEBSOCKET_PATH;
        return "ws://" + location;
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof PongWebSocketFrame) {
            ctx.channel().write(new PingWebSocketFrame(frame.content().retain()));
            return;
        }
        if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame bw = (BinaryWebSocketFrame) frame;
            ByteBuf fullPackData = bw.content();
            ByteBuf newFullPackData = fullPackData.copy();
            short length = fullPackData.readShort();
            byte[] cnt = fullPackData.readBytes(length).array();
            String arr0 = new String(cnt, Charset.forName("UTF-8"));
            if ("~".equals(arr0)) {
                short len = fullPackData.readShort();
                byte[] content = fullPackData.readBytes(len).array();
                String arr1 = new String(content, Charset.forName("UTF-8"));
                List<String> list = JSON.parseArray(arr1, String.class);
                SessionManager.getInstance().createSession(ctx.channel(), list);
            } else {
                String label = arr0;
                if (Constant.SPECIAL_NODE_START){
                    SessionManager.getInstance().broadcastToAllCluster(newFullPackData);
                }else {
                    if (Main.client !=null ){
                        Main.client.send(newFullPackData);
                    }
                }
                SessionManager.getInstance().broadcastToAllSession(label, newFullPackData);
            }
            return;
        }
        if (frame instanceof TextWebSocketFrame) {
            String fullPackData = ((TextWebSocketFrame) frame).text();
            logger.debug("<<<<<receive from user: "+fullPackData);
            String arr[] = fullPackData.split("#", 2);
            if (arr.length < 2) return;
            if ("~".equals(arr[0])) {
                List<String> list = JSON.parseArray(arr[1], String.class);
                SessionManager.getInstance().createSession(ctx.channel(), list);
            } else {
                String label = arr[0];
                if (Constant.SPECIAL_NODE_START){
                    logger.debug("本节点是高可用服务端，广播到集群其他节点");
                    SessionManager.getInstance().broadcastToAllCluster(fullPackData);
                }else {
                    if (Main.client !=null ){
                        logger.debug("本节点是高可用客户端，发往集群高可用服务主节点");
                        Main.client.send(fullPackData);
                    }
                }
                logger.debug("返回给本节点的用户");
                SessionManager.getInstance().broadcastToAllSession(label, fullPackData);
            }
            return;
        }
    }
}
