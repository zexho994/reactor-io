package reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Set;

/**
 * @author Zexho
 * @date 2021/8/19 7:04 下午
 */
public class Reactor implements Runnable {

    final Selector reactor_selector;
    final ServerSocketChannel reactor_channel;

    public Reactor(int port) throws IOException {
        // 打开selector
        reactor_selector = Selector.open();
        // 打开一个通道
        reactor_channel = ServerSocketChannel.open();
        // 绑定端口
        reactor_channel.socket().bind(new InetSocketAddress(port));
        // 设置非阻塞
        reactor_channel.configureBlocking(false);
        // 注册一个接受连接的事件
        SelectionKey sk = reactor_channel.register(reactor_selector, SelectionKey.OP_ACCEPT);
        // 事件上添加一个Acceptor对象
        sk.attach(new Acceptor(reactor_channel, reactor_selector));
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                // 阻塞等待事件
                reactor_selector.select();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 获取所有事件
            Set<SelectionKey> events = reactor_selector.selectedKeys();
            // 分发事件
            for (SelectionKey event : events) {
                dispatch(event);
            }
            events.clear();
        }
    }

    void dispatch(SelectionKey key) {
        Runnable r = (Runnable) (key.attachment());
        if (r != null) {
            r.run();
        }
    }

}
