package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/**
 * @author Zexho
 * @date 2021/8/19 1:47 下午
 */
public class PollClient {
    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            final int k = i;
            new Thread(() -> {
                try {
                    createClient("client " + k);
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        }
    }

    static void createClient(String str) throws IOException, InterruptedException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 6666));

        ByteBuffer writeBuffer = ByteBuffer.allocate(32);
        ByteBuffer readBuffer = ByteBuffer.allocate(32);

        writeBuffer.put(str.getBytes(StandardCharsets.UTF_8));
        writeBuffer.flip();

        while (true) {
            writeBuffer.rewind();
            socketChannel.write(writeBuffer);
            readBuffer.clear();
            socketChannel.read(readBuffer);
            Thread.sleep(100);
        }
    }

}
