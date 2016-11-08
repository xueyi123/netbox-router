package com.iih5.route.client;

import io.netty.channel.Channel;

import java.util.Timer;

public abstract class Handler {
    public abstract void connect(Channel channel);
    public abstract void connectError(Exception e, Timer timer,int connectErrorCount);
    public abstract void disconnect(Channel channel);
    public abstract void onMessage(String label, String message);
    public abstract void onMessage(String label, byte[] message);
}
