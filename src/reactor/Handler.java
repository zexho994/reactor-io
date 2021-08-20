package reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * @author Zexho
 * @date 2021/8/19 7:11 下午
 */
public class Handler implements Runnable {
    private final SocketChannel channel;
    private final SelectionKey sk;
    private final ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private final ByteBuffer writeBuffer = ByteBuffer.allocate(1024);
    private static final int READING = 0, SENDING = 1;
    private int state = READING;

    Handler(Selector selector, SocketChannel sc) throws IOException {
        channel = sc;
        sc.configureBlocking(false);
        sk = channel.register(selector, READING);
        // 将handler作为回调
        sk.attach(this);
        // 注册read就绪事件
        sk.interestOps(SelectionKey.OP_READ);
        // 唤醒select()阻塞的线程
        selector.wakeup();
    }

    boolean inputIsComplete() {
        System.out.println("input is complete");
        return true;
    }

    boolean outputIsComplete() {
        System.out.println("out is complete");
        return true;
    }

    void process() {
        System.out.println("process");
    }

    @Override
    public void run() {
        try {
            if (state == READING) {
                read();
            } else if (state == SENDING) {
                send();
            }
        } catch (IOException ex) {
            System.out.println("Handler run error" + ex);
        }
    }

    void read() throws IOException {
        channel.read(readBuffer);
        if (inputIsComplete()) {
            process();
            state = SENDING;
            sk.interestOps(SelectionKey.OP_WRITE);
        }
    }

    void send() throws IOException {
        channel.write(writeBuffer);
        if (outputIsComplete()) {
            sk.cancel();
        }
    }

}
