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

    void process() {
        System.out.println("request : " + StandardCharsets.UTF_8.decode(readBuffer));
    }

    @Override
    public void run() {
        try {
            if (state == READING) {
                handleRequest();
            }
            if (state == SENDING) {
                handleResponse();
            }
        } catch (IOException ex) {
            System.out.println("Handler run error" + ex);
        }
    }

    void handleRequest() throws IOException {
        // 读取数据
        channel.read(readBuffer);
        // 反转buffer
        readBuffer.flip();
        // 执行处理逻辑
        process();
        // 清理读缓存
        readBuffer.clear();
        // 转换成写状态
        state = SENDING;
        // 设置channel中的key为write事件
        sk.interestOps(SelectionKey.OP_WRITE);
    }

    void handleResponse() throws IOException {
        writeBuffer.put("success".getBytes(StandardCharsets.UTF_8));
        writeBuffer.flip();
        // 写response到channel中
        channel.write(writeBuffer);
        // 处理完后，移除该事件
        sk.cancel();
    }

}
