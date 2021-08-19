package server;

import reactor.Reactor;

import java.io.IOException;

/**
 * @author Zexho
 * @date 2021/8/19 7:23 下午
 */
public class ReactorServer {
    public static void main(String[] args) throws IOException {
        Reactor reactor = new Reactor(6666);
        reactor.run();
    }
}
