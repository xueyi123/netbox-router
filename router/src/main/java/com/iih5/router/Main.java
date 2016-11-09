package com.iih5.router;

import com.iih5.route.client.Client;
import com.iih5.route.server.RouterServer;
import com.iih5.route.server.SimpleServer;
import com.iih5.router.constant.Constant;
import com.iih5.router.constant.HAMode;
import com.iih5.router.constant.ServiceMode;
import com.iih5.router.handler.ClientClusterHandler;
import com.iih5.router.handler.ServerSessionHandler;
import com.iih5.router.utils.LogUtil;
import com.iih5.router.utils.PropertyConf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;


public class Main {
    private static int PORT = 9869;
    private static Logger logger = LoggerFactory.getLogger(Main.class);
    public static Client client = null;
    //args[]=[port,模式，HA] 启动
    public static void main(String[] args) throws Exception {
        //【】处理输入命令
        mainCMD(args);
        //【】启动主服务监听
        logger.info("》》》启动路由主服务监听");
        RouterServer sessionServer = SimpleServer.createNewServer(PORT, 1, 8);
        sessionServer.addHandler(new ServerSessionHandler());
        sessionServer.start();
        //【】
        if (Constant.SERVICE_MODE.equals(ServiceMode.CLUSTER)){
            client = new Client(new ClientClusterHandler());
            String str = PropertyConf.get("HA.IP");
            String[] arr = str.split(";");
            LinkedList<String> urls = new LinkedList<String>();
            for (String addr:arr) {
                urls.add("ws://"+addr+":"+Constant.HA_PORT+"/websocket");
            }
            client.setUrls(urls);
            client.connect();
        }
        //【3】进入循环
        while (true) Thread.sleep(5000000);
    }
    static void mainCMD(String[] args){
        LogUtil.init();
        if (args.length == 0 || args.length == 1){
            Constant.SERVICE_MODE = ServiceMode.SIMPLE;
            if (args.length == 1){
                PORT = Integer.valueOf( args[0]);
            }
        }else if (args.length == 2 ){
            PORT = Integer.valueOf( args[0]);
            if (args[1].equals(ServiceMode.CLUSTER) ){
                Constant.SERVICE_MODE = ServiceMode.CLUSTER;
            }
            Constant.CLUSTER_HA = "";
        }else if (args.length ==3 ){
            PORT = Integer.valueOf( args[0]);
            if (args[1].equals(ServiceMode.CLUSTER) ){
                Constant.SERVICE_MODE = ServiceMode.CLUSTER;
            }
            if (args[2].equals(HAMode.HA)){
                Constant.CLUSTER_HA = HAMode.HA;
            }
        }
        String pwd = PropertyConf.get("SERVER.PWD");
        Constant.SERVER_PWD = pwd;
    }
}