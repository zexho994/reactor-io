package reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * @author Zexho
 * @date 2021/8/19 7:04 下午
 */
public class Reactor implements Runnable {

    final Selector selector;
    final ServerSocketChannel serverSocket;

    public Reactor(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        SelectionKey sk = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        sk.attach(new Acceptor());
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                // 阻塞等待事件
                selector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 获取所有事件
            Set<SelectionKey> events = selector.selectedKeys();
            // 分发事件
            for (SelectionKey event : events) {
                dispatch(event);
            }
            events.clear();
        }
    }

    void dispatch(SelectionKey key) {
        Acceptor r = (Acceptor) (key.attachment());
        if (r != null) {
            r.run();
        }
    }

    class Acceptor implements Runnable {
        @Override
        public void run() {
            try {
                //等待连接
                SocketChannel accept = serverSocket.accept();
                //如果有新的连接
                if (accept != null) {
                    new Handler(selector, accept);
                }
            } catch (Exception ex) {
                System.out.println("acceptor run error" + ex);
            }
        }
    }

}
