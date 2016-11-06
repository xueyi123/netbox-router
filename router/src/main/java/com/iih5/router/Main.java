package com.iih5.router;

import com.iih5.route.client.Client;
import com.iih5.route.server.RouterServer;
import com.iih5.route.server.SimpleServer;
import com.iih5.router.constant.Constant;
import com.iih5.router.handler.ClientClusterHandler;
import com.iih5.router.handler.ServerClusterHandler;
import com.iih5.router.handler.ServerSessionHandler;
import com.iih5.router.utils.PropertyConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.LinkedList;


public class Main {
    private static int PORT = 9869;
    private static Logger logger = LoggerFactory.getLogger(Main.class);
    public static Client client = null;
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && args[0] != null) {
            String portStr = args[0];
            PORT = Integer.valueOf(portStr);
        }
        String haPortStr = PropertyConf.get("HA.MY.PORT");
        if (!StringUtils.isEmpty(haPortStr)){
            Constant.SPECIAL_NODE = true;
            client = new Client(new ClientClusterHandler());
            String haAddr = PropertyConf.get("HA.OHTER.URL");
            LinkedList<String> urls = new LinkedList<String>();
            urls.add("ws://"+haAddr+"/websocket");
            client.setUrls(urls);
            client.connect();
        }else {
            client = new Client(new ClientClusterHandler());
            String str = PropertyConf.get("CLUSTER.URL");
            String[] arr = str.split(";");
            LinkedList<String> urls = new LinkedList<String>();
            for (String addr:arr) {
                urls.add("ws://"+addr+"/websocket");
            }
            client.setUrls(urls);
            client.connect();
        }
        //【】对外服务监听
        logger.info("》》》启动集群外部服务监听");
        RouterServer sessionServer = SimpleServer.createNewServer(PORT, 1, 8);
        sessionServer.addHandler(new ServerSessionHandler());
        sessionServer.start();

        //【3】进入循环
        while (true) Thread.sleep(5000000);
    }
}