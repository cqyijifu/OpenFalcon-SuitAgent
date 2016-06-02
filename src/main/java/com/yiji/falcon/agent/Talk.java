package com.yiji.falcon.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class Talk extends Thread {

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private Selector selector;
    private Agent agent;

    /**
     * 传递客户端连接的SocketChannel,然后用非阻塞模式进行读事件和写事件的相应
     *
     * @param socketChannel 客户端SocketChannel
     * @throws IOException
     */
    public Talk(SocketChannel socketChannel,Agent agent) throws IOException {
        selector = Selector.open();
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        //创建用于存放用户发来的数据的缓冲区，并将其作为通道附件的形式进行保存
        socketChannel.configureBlocking(false);//设置非阻塞模式
        //向Selector注册读就绪事件和写就绪事件，以便响应客户端发来的数据
        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, buffer);
        this.agent = agent;
    }

    @Override
    public void run() {
        log.info("线程[" + getName() + "]:启动");
        try {
            while (selector.select() > 0) {
                Set readyKeys = selector.selectedKeys();
                Iterator it = readyKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = null;
                    try {
                        key = (SelectionKey) it.next();
                        it.remove();
                        if (key.isReadable()) {
                            //获取与SelectionKey关联的附件
                            ByteBuffer buffer = (ByteBuffer) key.attachment();
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            ByteBuffer readBuffer = ByteBuffer.allocate(100);//用于存放读到的数据
                            socketChannel.read(readBuffer);//读取数据
                            readBuffer.flip();//把极限设为位置，再把位置设为0
                            buffer.limit(buffer.capacity());//将buffer的极限设置为容量
                            buffer.put(readBuffer);//将readBuffer中内容拷贝到buffer中，暂不考虑缓冲区溢出
                        }
                        if (key.isWritable()) {
                            //读取通道附件中的buffer数据
                            ByteBuffer buffer = (ByteBuffer) key.attachment();
                            SocketChannel socketChannel = (SocketChannel) key.channel();
                            buffer.flip();//把极限设为位置，位置设为0
                            String data = Agent.decode(buffer);
                            if (data.contains("\r\n")) {//若读到数据
                                String outputData = data.substring(0, data.indexOf("\n") + 1);//截取一行数据

                                //响应客户端服务器已接受数据请求
                                ByteBuffer outputBuffer = Agent.encode("echo:" + outputData);
                                while (outputBuffer.hasRemaining()){
                                    socketChannel.write(outputBuffer);//发出数据
                                }

                                ByteBuffer temp = Agent.encode(outputData);
                                buffer.position(temp.limit());//把buffer的位置设为temp的极限
                                buffer.compact();//删除已经处理的数据

                                if ("exit\r\n".equals(outputData)) {
                                    //处理exit命令,关闭服务器线程
                                    key.cancel();
                                    socketChannel.close();
                                    agent.shutdown();
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.info("线程[" + getName() + "]:通道监听异常",e);
                        if (key != null) {
                            // 是此SelectionKey失效，并且selector不再监控此SelectionKey事件
                            key.cancel();
                            // 关闭于此SelectionKey关联的通道
                            key.channel().close();
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("agent运行异常",e);
        }
    }
}