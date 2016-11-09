# netbox-router
##这是个高性能的消息转发路由器，具备HA高可用，集群，简易等优点，采用websocket协议，支持基于‘订阅/发布’形式文本和二进制传输，非常方便支持其他语言
##Router集群原理
##Router集群式由若干个router实例组成，客户端连接任何某个节点消息都是互通的。采用‘接班人模式’，集群运行中任何时候必须有一个实例同时作为集群内部各个节点之间消息的中转分发中心，这个节点我们称之为领袖节点。当领袖节点宕机时接班人节点会自动顶替上去成为领袖，保持集群正常运转。如果故障的领袖节点恢复运行时，它会自动的变为接班人节点，依次循环工作。

##安装|单例运行
###1.下载router.jar包
###2.运行 java -jar router.jar 或 java -jar router.jar 9980 即可启动一个单例router

####启动集群
比如router集群由7个实例组成,其中带HA标识这俩个会作为集群内部分发中心，命令第一个参数表示监听端口，第二个参数表示是否最为集群模式启动，1表示集群模式，第三个参数HA
表示这个实例本身又同时承担集群内部分发中心。
<br/>java -jar router.jar 9981 1 HA  （注：这两个HA实例启动时必须保证其中一个启动成功之后再启动另外一个）
<br/>java -jar router.jar 9982 1 HA  （注：这两个HA实例启动时必须保证其中一个启动成功之后再启动另外一个）
<br/>java -jar router.jar 9983 1
<br/>java -jar router.jar 9984 1
<br/>java -jar router.jar 9985 1
<br/>java -jar router.jar 9986 1
<br/>java -jar router.jar 9987 1

####配置说明：
<br/>服务密码
<br/>SERVER.PWD = KY^KD($^%RFGKD%^FJGJPO(#^*
<br/>高可用配置,HA.IP配置的IP就是java -jar router.jar 9981 1 HA 和 java -jar router.jar 9981 1 HA 所在物理机的IP,这两个IP可以相同
<br/>HA.IP = 192.168.51.115;192.168.163.92


###Java Client API 已提供，其他语言的后面陆续给出































