/*
 * www.yiji.com Inc.
 * Copyright (c) 2016 All Rights Reserved
 */
/*
 * 修订记录:
 * guqiu@yiji.com 2016-07-25 15:20 创建
 */

import org.junit.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author guqiu@yiji.com
 */
public class SocketTest {

    @Test
    public void test(){
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(InetAddress.getByName("www.baidu.com"),80),5000);
            if(socket.isConnected()){
                String s = "I am is Falcon Agent Client";
                OutputStream outputStream = socket.getOutputStream();
                outputStream.write(s.getBytes());
                outputStream.close();
                System.out.println("connected");
            }else{
                System.out.println("not connected");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(socket != null && socket.isConnected()){
                try {
                    System.out.println("closed");
                    socket.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

}
