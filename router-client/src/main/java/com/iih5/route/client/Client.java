package com.iih5.route.client;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Client {
	Logger logger = LoggerFactory.getLogger(Client.class);
	public static Channel channel = null;
	public static Handler clientHandler = null;
	public static String[] textLabels = null;
	public static Integer protoType = ProtoType.TEXT;
	public static String serverPwd = "";
	private Bootstrap bootstrap = null;
	private ChannelFuture channelFuture = null;
	private URI uri = null;
	private Timer timer = null;
	private LinkedList<String> urls = null;
	private int connectErrorCount = 0;
	/**
	 * Client
	 * @param handler
	 * @param labels 订阅标签数组
	 */
	public Client(Handler handler, String ... labels) {
		clientHandler = handler;
		textLabels = labels;
		init();
	}
	public Client(Handler handler){
		clientHandler = handler;
		init();
	}
	private void init(){
		bootstrap = new Bootstrap();
		bootstrap.group(new NioEventLoopGroup()).channel(NioSocketChannel.class)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) throws Exception {
						ChannelPipeline p = ch.pipeline();
						p.addLast(new WebSocketInitializer());
					}
				});
		urls = new LinkedList<String>();
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (channel == null){
					logger.debug("进入断线重连。。。");
					disConnect();
					connect();
				}
			}
		},500,1500);
	}

	/**
	 * 设置服务器密码
	 * @param pwd
     */
	public void setServerPwd(String pwd){
		serverPwd = pwd;
	}
	/**
	 * 设置协议类型，默认是text协议
	 * @param type
	 */
	public void setProtoType(Integer type){
		protoType = type;
	}
	/**
	 * 设置负载均衡urls
	 * @param urls
     */
	public void setUrls(LinkedList<String> urls){
		this.urls = urls;
	}
	/**
	 * 连接
	 * @param url
     */
	public void  connect(String url){
		try {
			uri = new URI(System.getProperty("url",url));
			channelFuture = bootstrap.connect(uri.getHost(), uri.getPort()).sync();
		} catch (Exception e) {
			connectErrorCount++;
			clientHandler.connectError(e ,timer,connectErrorCount);
		}
	}

	public void connect(){
		String url = urls.pop();
		logger.debug("尝试当前连接："+url);
		connect(url);
		urls.add(url);
	}
	public void disConnect(){
		if (channelFuture != null) {
			channelFuture.channel().close();
		}

	}

	/**
	 * 发布内容
	 * @param label
	 * @param content
     */
	public void publish(String label ,String content) {
		String pack = label+" # "+content;
		channel.writeAndFlush(new TextWebSocketFrame(pack));
	}

	/**
	 * 发布内容
	 * @param label
	 * @param content
     */
	public void publish(String label,byte[] content){
		try {
			ByteBuf data = Unpooled.buffer();
			byte[] c = label.getBytes("UTF-8");
			data.writeShort(c.length);
			data.writeBytes(c);
			data.writeBytes(content);
			channel.writeAndFlush(new BinaryWebSocketFrame(data));
		} catch (UnsupportedEncodingException e) {
			logger.error("",e);
		}
	}
	class WebSocketInitializer extends ChannelInitializer<SocketChannel> {
		@Override
		public void initChannel(SocketChannel ch) throws Exception {
			ChannelPipeline pipeline = ch.pipeline();
			pipeline.addLast(new HttpClientCodec());
			pipeline.addLast(new HttpObjectAggregator(65536));		
			ClientHandler wc=new ClientHandler(WebSocketClientHandshakerFactory.newHandshaker(uri,
					WebSocketVersion.V13, null, false, new DefaultHttpHeaders()));
			pipeline.addLast(wc);
		}
	}





























}
