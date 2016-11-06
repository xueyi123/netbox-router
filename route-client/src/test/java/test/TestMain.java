package test;

import com.iih5.route.client.Client;

import java.util.LinkedList;

public class TestMain {
    public static void main(String[] args) throws InterruptedException {
        Client client = new Client(new TestHandler());
        LinkedList linkedList = new LinkedList();
        linkedList.add("ws://127.0.0.1:9988/websocket");
        linkedList.add("ws://127.0.0.1:9977/websocket");
        client.setUrls(linkedList);
    }

}
