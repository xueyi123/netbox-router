package com.iih5.router.constant;

public class Constant {

    /**
     * 特殊节点监听是否已经启动
     */
    public static Boolean SPECIAL_NODE_START = false;

    /**
     * 高可用监听端口
     */
    public static Integer HA_PORT = 23596;

    /**
     *服务启动模式， "0"=简单模式，"1"=集群模式
     */
    public static String SERVICE_MODE = "0";
    /**
     * 以高可用的方式启动,默认不是高可用节点，而是个普通集群节点
     */
    public static String CLUSTER_HA = "";

}
