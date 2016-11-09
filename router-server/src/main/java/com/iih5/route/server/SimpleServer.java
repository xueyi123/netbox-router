package com.iih5.route.server;

public class SimpleServer {

    public static RouterServer createNewServer(int port){
        return  new RouterServer(port,1,4);
    }
    public static RouterServer createNewServer(int port,int bossNum,int workNum){
        return  new RouterServer(port,bossNum,workNum);
    }


}
