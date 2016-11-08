/**
 * ---------------------------------------------------------------------------
 * 类名称   ：ClientClusterHandler
 * 类描述   ：
 * 创建人   ： xue.yi
 * 创建时间： 2016/11/4 16:50
 * 版权拥有：星电商科技
 * ---------------------------------------------------------------------------
 */
package com.iih5.router.handler;

import com.iih5.route.client.Handler;
import com.iih5.route.server.RouterServer;
import com.iih5.route.server.SessionManager;
import com.iih5.route.server.SimpleServer;
import com.iih5.router.constant.Constant;
import com.iih5.router.constant.HAMode;
import com.iih5.router.utils.PropertyConf;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;

public class ClientClusterHandler extends Handler {
    Logger logger = LoggerFactory.getLogger(ClientClusterHandler.class);

    @Override
    public void connect(Channel channel) {
       logger.info("连接成功");
    }

    @Override
    public void connectError(Exception e, Timer timer,int connectErrorCount) {
        logger.warn("连接异常， 已尝试连接次数："+connectErrorCount);
        //如果是高可用代理节点，当无法连接对方时关闭连接，然后启动内部服务，让别人来连接自己
        if (Constant.CLUSTER_HA.equals(HAMode.HA) && connectErrorCount>=2 && !Constant.SPECIAL_NODE_START){
            logger.debug("检测到自己是特殊节点!");
            logger.debug("》》》》无法检测到集群内部高可用监听，启动本集群高可用代理节点");
            timer.cancel();
            RouterServer clusterServer = SimpleServer.createNewServer(Constant.HA_PORT);
            clusterServer.addHandler(new ServerClusterHandler());
            clusterServer.start();
            Constant.SPECIAL_NODE_START = true;
        }
    }

    @Override
    public void disconnect(Channel channel) {
        logger.info("断开连接");
    }

    public void onMessage(String label, String message) {
        SessionManager.getInstance().broadcastToAllSession(label,message);
    }

    @Override
    public void onMessage(String label, byte[] message) {



    }
}
