package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Zexho
 * @date 2021/8/19 9:11 上午
 */
public class PollServer {
    static final int KB = 1024;
    static final int MB = 1024 * 1024;

    public static void main(String[] args) throws IOException, InterruptedException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress("127.0.0.1", 8000));
        ssc.configureBlocking(false);

        Selector selector = Selector.open();
        // selector感兴趣的客户端发起连接的事件
        ssc.register(selector, SelectionKey.OP_ACCEPT);

        ByteBuffer readBuff = ByteBuffer.allocate(MB);
        ByteBuffer writeBuff = ByteBuffer.allocate(KB);
        writeBuff.put("received".getBytes());
        writeBuff.flip();

        while (true) {
            // 使用select阻塞住线程，当有新连接的时候再唤醒
            selector.select();
            Set<SelectionKey> keys = selector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();

            while (it.hasNext()) {
                SelectionKey key = it.next();
                it.remove();
                if (key.isAcceptable()) {
                    SocketChannel socketChannel = ssc.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    readBuff.clear();
                    socketChannel.read(readBuff);
                    readBuff.flip();
                    System.out.println("received : " + StandardCharsets.UTF_8.decode(readBuff));
                    key.interestOps(SelectionKey.OP_WRITE);
                } else if (key.isWritable()) {
                    writeBuff.rewind();
                    SocketChannel socketChannel = (SocketChannel) key.channel();
                    socketChannel.write(writeBuff);
                    key.interestOps(SelectionKey.OP_READ);
                }
            }
            Thread.sleep(100);
        }
    }
}
