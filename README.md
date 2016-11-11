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

###客户端SDK使用
<br/>    <dependency>
<br/>        <groupId>com.iih5</groupId>
<br/>        <artifactId>router-client</artifactId>
<br/>        <version>1.0</version>
<br/>    </dependency>

<br/>    Client client = new Client(new Handler() {
<br/>                         @Override
<br/>                         public void connect(Channel channel) {}
<br/>                         @Override
<br/>                         public void connectError(Exception e, Timer timer, int i) {}
<br/>                         @Override
<br/>                         public void disconnect(Channel channel) {}
<br/>                         @Override
<br/>                         public void onMessage(String label, String message) {//接受到的文本消息}
<br/>                         @Override
<br/>                         public void onMessage(String label, byte[] message) {//接受到的二进制消息}
<br/>                     },"BROADCAST");//订阅消息
<br/>                     client.setServerPwd("KY^KD($^%RFGKD%^FJGJPO(#^*");//如果router服务设置密码的话，必须添加密码链接
<br/>                     LinkedList<String> list = new LinkedList<String>();
<br/>                     list.add("ws://192.168.4.221:9988/websocket"); //router集群地址，可以添加多个
<br/>                     client.setUrls(list);
<br/>                     client.connect();//会断线重连，如果多个地址则轮询多个地址链接。。起到负载均衡作用
<br/>                     Thread.sleep(300);//注：因为是connect是异步的，所以如果希望立刻发布的话，必须先等300毫秒左右
<br/>                     client.publish("BROADCAST","hello,router !!!!!");//发布消息



























