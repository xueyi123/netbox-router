package com.iih5.route.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class RouterServer {
    private static Logger logger = LoggerFactory.getLogger(RouterServer.class);
    private int port = 23568;
    private EventLoopGroup bossGroup = null;
    private EventLoopGroup workerGroup = null;
    ServerBootstrap bootstrap = null;

    public RouterServer() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(8);
        bootstrap = new ServerBootstrap();
    }

    /**
     * 初始化服务
     * @param port 端口
     * @param bossNum 监听线程数量
     * @param workerNum 读写线程数量
     */
    public RouterServer(int port, int bossNum, int workerNum) {
        this.port = port;
        bossGroup = new NioEventLoopGroup(bossNum);
        workerGroup = new NioEventLoopGroup(workerNum);
        bootstrap = new ServerBootstrap();
    }

    /**
     * 添加处理器
     * @param handler
     */
    public void addHandler(Handler handler) {
        Initializer initializer = new Initializer(handler);
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(initializer);
    }

    ChannelFuture channelFuture = null;

    /**
     * 启动服务
     */
    public void start() {
        try {
            channelFuture = bootstrap.bind(port).sync();
            logger.info("监听端口： " + port);
            logger.debug("测试地址: http://"+ InetAddress.getLocalHost().getHostAddress()+":" + port + '/');
        } catch (Exception e) {
            logger.error("shutdown: ", e);
            close();
        }
    }

    /**
     * 关闭监听
     */
    public void close() {
        if (channelFuture != null) {
            channelFuture.removeListener(ChannelFutureListener.CLOSE);
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bootstrap != null) {
            bossGroup.shutdownGracefully();
        }
    }


}
