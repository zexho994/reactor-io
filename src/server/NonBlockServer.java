package server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

/**
 * @author Zexho
 * @date 2021/8/18 7:39 下午
 */
public class NonBlockServer {
    public static void main(String[] args) throws IOException {
        // 1. 获取通道
        ServerSocketChannel server = ServerSocketChannel.open();
        // 2. 切换成非阻塞
        server.configureBlocking(false);
        // 3. 绑定链接
        server.bind(new InetSocketAddress(6666));
        // 4. 获取选择器
        Selector selector = Selector.open();
        // 5. 注册选择器,指定接受"监听socket"事件
        server.register(selector, SelectionKey.OP_ACCEPT);

        ByteBuffer readBuffer = ByteBuffer.allocate(1024);

        // 轮询的获取选择器上已就绪的事件，只要select>0说明已就绪
        while (selector.select() > 0) {
            // 获取所有已注册的监听事件
            Iterator<SelectionKey> selectionKeys = selector.selectedKeys().iterator();
            while (selectionKeys.hasNext()) {
                SelectionKey selectionKey = selectionKeys.next();
                // 移除处理过的事件
                selectionKeys.remove();
                // 如果是连接事件
                if (selectionKey.isAcceptable()) {
                    System.out.println("=> selector accept key");
                    // 获取客户端连接
                    SocketChannel client = server.accept();
                    // 配置非阻塞
                    client.configureBlocking(false);
                    // 注册到选择器上
                    client.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    System.out.println("=> selector readable key");
                    // 获取读就绪的通道
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    // 获取到文件通道
                    FileChannel channel = FileChannel.open(Paths.get("static/get.jpg"), StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                    while (socketChannel.read(readBuffer) > 0) {
                        // buffer切换成读模式，允许channel读取数据
                        readBuffer.flip();
                        // 写数据到channel中，实际输出到目标文件中
                        channel.write(readBuffer);
                        // channel读完,buffer切换成写模式,以便接受新数据
                        readBuffer.clear();
                    }
                }
            }
        }
    }
}
