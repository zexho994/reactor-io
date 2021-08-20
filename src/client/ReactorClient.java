package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class ReactorClient {
    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
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
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress("127.0.0.1", 6666));
        channel.configureBlocking(true);

        ByteBuffer writeBuffer = ByteBuffer.allocate(32);
        ByteBuffer readBuffer = ByteBuffer.allocate(32);

        writeBuffer.put(str.getBytes(StandardCharsets.UTF_8));
        writeBuffer.flip();
        channel.write(writeBuffer);

        channel.read(readBuffer);
        readBuffer.flip();
        System.out.println("request : " + StandardCharsets.UTF_8.decode(readBuffer));
        readBuffer.clear();
    }
}
