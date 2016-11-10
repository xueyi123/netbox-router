package com.iih5.route.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable()
public abstract class Handler extends SimpleChannelInboundHandler<Object> {
//ChannelInboundHandlerAdapter,SimpleChannelInboundHandler,ChannelInboundHandler
}
