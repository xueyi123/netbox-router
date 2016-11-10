package com.iih5.route.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

public class SessionManager {
    private CopyOnWriteArraySet<Channel> clusterChannels = new CopyOnWriteArraySet<Channel>();
    private Map<String,CopyOnWriteArraySet<Channel>> subscribeMap = new ConcurrentHashMap<String, CopyOnWriteArraySet<Channel>>();
    private Map<Channel,List<String>> channelMap = new ConcurrentHashMap<Channel, List<String>>();
    private static SessionManager manger = new SessionManager();
    private SessionManager(){};
    public static SessionManager getInstance(){
        return manger;
    }
    /**
     * 【集群内部通信】添加集群节点
     * @param channel
     */
    public void addClusterNode(Channel channel){
        clusterChannels.add(channel);
    }
    /**
     * 【集群内部通信】删除集群节点
     * @param channel
     */
    public void delClusterNode(Channel channel){
        clusterChannels.remove(channel);
    }
    /**
     * 【集群内部通信】获取集群列表
     * @return
     */
    public CopyOnWriteArraySet<Channel> getClusterNodeList(){
        return clusterChannels;
    }
    /**
     * 【集群内部通信】当数据传输协议为text时，调用此方法
     * @param fullPackData
     */
    public void broadcastToAllCluster(String fullPackData){
        CopyOnWriteArraySet<Channel> channels =  SessionManager.getInstance().getClusterNodeList();
        if (channels != null){
            for (Channel channel:channels) {
                channel.writeAndFlush(new TextWebSocketFrame(fullPackData));
            }
        }
    }
    public void broadcastToAllClusterNotMe(String fullPackData,Channel myChannel){
        CopyOnWriteArraySet<Channel> channels =  SessionManager.getInstance().getClusterNodeList();
        if (channels != null){
            for (Channel channel:channels) {
                if (!channel.equals(myChannel)){
                    channel.writeAndFlush(new TextWebSocketFrame(fullPackData));
                }
            }
        }
    }
    /**
     * 【集群内部通信】当数据传输协议未binary时，调用此方法
     * @param fullPackData
     */
    public void broadcastToAllCluster(ByteBuf fullPackData){
        CopyOnWriteArraySet<Channel> channels =  SessionManager.getInstance().getClusterNodeList();
        if (channels != null){
            for (Channel channel:channels) {
                channel.writeAndFlush(new BinaryWebSocketFrame(fullPackData.copy()));
            }
        }
    }
    public void broadcastToAllClusterNotMe(ByteBuf fullPackData,Channel myChannel){
        CopyOnWriteArraySet<Channel> channels =  SessionManager.getInstance().getClusterNodeList();
        if (channels != null){
            for (Channel channel:channels) {
                if (!channel.equals(myChannel)){
                    channel.writeAndFlush(new BinaryWebSocketFrame(fullPackData.copy()));
                }
            }
        }
    }
    /**
     * 添加一个session连接
     * @param channel
     * @param labels
     */
    public synchronized void  createSession(Channel channel, List<String> labels){
        for (String key:labels) {
            CopyOnWriteArraySet<Channel> set = subscribeMap.get(key);
            if (set == null){
                set = new CopyOnWriteArraySet<Channel>();
                subscribeMap.put(key,set);
            }
            set.add(channel);
        }
        channelMap.put(channel,labels);
    }

    /**
     * 删除一个session连接
     * @param channel
     */
    public synchronized void deleteSession(Channel channel){
        List<String> labels = channelMap.get(channel);
        if (labels == null){
            return;
        }
        for (String label:labels) {
            CopyOnWriteArraySet<Channel> chs = subscribeMap.get(label);
            if (chs != null){
                boolean d= chs.remove(channel);
            }
        }
        Object dd = channelMap.remove(channel);
    }

    /**
     * 获取整个订阅的session列表
     * @param label
     * @return
     */
    public CopyOnWriteArraySet<Channel> getSubscribeSessionList(String label){
        return subscribeMap.get(label);
    }

    /**
     * 广播给订阅此标签的客户端
     * @param label 订阅标签
     * @param fullPackData 完整的数据包数据
     */
    public void  broadcastToAllSession(String label,String fullPackData){
        CopyOnWriteArraySet<Channel> channels = getSubscribeSessionList(label);
        if (channels != null ){
            for (Channel channel:channels) {
                channel.writeAndFlush(new TextWebSocketFrame(fullPackData));
            }
        }
    }

    /**
     * 广播给订阅此标签的客户端
     * @param label 订阅的标签
     * @param fullPackData  完整的内容（必须用copy1份）
     */
    public void broadcastToAllSession(String label,ByteBuf fullPackData){
        CopyOnWriteArraySet<Channel> channels = getSubscribeSessionList(label);
        if (channels != null){
            for (Channel channel:channels) {
                channel.writeAndFlush(new BinaryWebSocketFrame(fullPackData.copy()));
            }
        }
    }

    /**
     * 是否包含次Session
     * @param channel
     * @return
     */
    public boolean containSession(Channel channel){
        return channelMap.containsKey(channel);
    }

}
