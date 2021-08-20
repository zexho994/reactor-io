package reactor;

import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @author Zexho
 * @date 2021/8/20 12:50 下午
 */
class Acceptor implements Runnable {
    final ServerSocketChannel serverSocket;
    final Selector selector;

    public Acceptor(ServerSocketChannel channel, Selector selector) {
        this.serverSocket = channel;
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            //等待连接
            SocketChannel channel = this.serverSocket.accept();
            //如果有新的连接
            if (channel != null) {
                new Handler(this.selector, channel);
            }
        } catch (Exception ex) {
            System.out.println("acceptor run error" + ex);
        }
    }
}
