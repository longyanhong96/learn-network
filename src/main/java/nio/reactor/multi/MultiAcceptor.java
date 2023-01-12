package nio.reactor.multi;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/**
 * @Description
 * @Author longyh
 * @Date 2023/1/12 13:35
 */
public class MultiAcceptor implements Runnable {

    private boolean first = true;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    private SubReactor[] subReactors;

    public MultiAcceptor(Selector selector, ServerSocketChannel serverSocketChannel) {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;
    }

    public void init() throws IOException {
        if (first) {
            first = false;
            subReactors = new SubReactor[2];

            for (int i = 0; i < subReactors.length; i++) {
                SubReactor subReactor = new SubReactor();
                subReactor.init();
                subReactors[i] = subReactor;
                new Thread(subReactor).start();
            }
        }
    }

    @Override
    public void run() {
        try {
            init();

            SocketChannel socketChannel = serverSocketChannel.accept();
            socketChannel.configureBlocking(false);
            SubReactor subReactor = subReactors[socketChannel.getRemoteAddress().hashCode() % 2];
            subReactor.process(socketChannel);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
