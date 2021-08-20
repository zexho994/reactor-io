package reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

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

    // 判断是否读完成
    boolean inputIsComplete() throws IOException {
        if (!channel.finishConnect()){
            return false;
        }
        System.out.println("input is complete");
        return true;
    }

    // 判断是否写完
    boolean outputIsComplete() {
        if (writeBuffer.hasRemaining()) {
            return false;
        }
        System.out.println("out is complete");
        return true;
    }

    void process() {
        System.out.println("process start");
        // 打印收到的
        System.out.println("received : " + StandardCharsets.UTF_8.decode(readBuffer));
        System.out.println("process done");
    }

    @Override
    public void run() {
        try {
            if (state == READING) {
                read();
            }
            if (state == SENDING) {
                send();
            }
        } catch (IOException ex) {
            System.out.println("Handler run error" + ex);
        }
    }

    void read() throws IOException {
        System.out.println("read...");

        // channel的数据写入到中readBuffer中
        channel.read(readBuffer);

        if (inputIsComplete()) {
            // 反转buffer
            readBuffer.flip();
            // 执行处理逻辑
            process();
            // 转换成写状态
            state = SENDING;
            // 设置channel中的key为write事件
            sk.interestOps(SelectionKey.OP_WRITE);
        }
    }

    void send() throws IOException {
        System.out.println("send...");

        // writeBuffer的数据写入到channel中
        channel.write(writeBuffer);

        // 如果写完
        if (outputIsComplete()) {
            // 清空writeBuffer
            writeBuffer.clear();
            writeBuffer.flip();
            // 处理完后，移除该事件
            sk.cancel();
        }
    }

}
