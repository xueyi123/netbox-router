/**
 * ---------------------------------------------------------------------------
 * 类名称   ：Utils
 * 类描述   ：
 * 创建人   ： xue.yi
 * 创建时间： 2016/11/8 11:09
 * 版权拥有：星电商科技
 * ---------------------------------------------------------------------------
 */
package com.iih5.route.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class Utils {
   private static Logger logger = LoggerFactory.getLogger(Utils.class);
    /**
     * 二进制转化为string
     * @param binary
     * @return
     */
    public static String binaryToString(byte[] binary){
       ByteBuf buf =  Unpooled.copiedBuffer(binary);
        int len = buf.readShort();
        byte[] cnt = buf.readBytes(len).array();
        return new String(cnt, Charset.forName("UTF-8"));
    }

    /**
     * 二进制转化为string
     * @param buf
     * @return
     */
    public static String binaryToString(ByteBuf buf){
        int len = buf.readShort();
        byte[] cnt = buf.readBytes(len).array();
        return new String(cnt, Charset.forName("UTF-8"));
    }

    /**
     * 字符串转换为二进制
     * @param text
     * @return
     */
    public static byte[] stringToBinary(String text){
        try {
            byte[] d = text.getBytes("UTF-8");
            ByteBuf data = Unpooled.buffer();
            data.writeShort(d.length);
            data.writeBytes(d);
            return data.array();
        } catch (UnsupportedEncodingException e) {
           logger.error("",e);
        }
      return null;
    }

}
