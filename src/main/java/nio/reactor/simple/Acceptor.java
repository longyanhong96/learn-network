package nio.reactor.simple;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @Description
 * @Author longyh
 * @Date 2023/1/12 10:24
 */
@Slf4j
public class Acceptor implements Runnable {

    private Selector selector;

    private ServerSocketChannel serverSocketChannel;

    public Acceptor(Selector selector, ServerSocketChannel serverSocketChannel) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
    }

    @Override
    public void run() {
        try {
            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);

            log.info("accept : {}", socketChannel.getRemoteAddress());
            SelectionKey scKey = socketChannel.register(selector, SelectionKey.OP_READ);
            scKey.attach(new Worker(socketChannel));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
