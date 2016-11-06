/**
 * ---------------------------------------------------------------------------
 * 类名称   ：TestHandler
 * 类描述   ：
 * 创建人   ： xue.yi
 * 创建时间： 2016/11/4 16:50
 * 版权拥有：星电商科技
 * ---------------------------------------------------------------------------
 */
package test;

import com.iih5.route.client.Handler;
import io.netty.channel.Channel;

import java.util.Timer;

public class TestHandler extends Handler {

    @Override
    public void connect(Channel channel) {
        System.out.println("- - - - - connect");
    }

    @Override
    public void connectError(Exception e, Timer timer) {

    }

    @Override
    public void disconnect(Channel channel) {
        System.out.println("- - - - - disconnect");
    }

    public void onMessage(String channel, String message) {
        System.out.println(channel+"===="+message);
    }

    @Override
    public void onMessage(byte[] channel, byte[] message) {



    }
}
