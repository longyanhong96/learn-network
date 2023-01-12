package nio.reactor.multi;

import nio.reactor.simple.Reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;

/**
 * @Description 这个是simple reactor 河 multiThreadServer结合
 * @Author longyh
 * @Date 2023/1/12 13:16
 */
public class MainReactor {

    private final int port = 8888;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public void init() throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);

        this.selector = Selector.open();
        SelectionKey sscKey = this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);
        sscKey.attach(new MultiAcceptor(this.selector, this.serverSocketChannel));

    }

    public void process() throws IOException {
        while (true) {
            selector.select();
            Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();

            while (selectionKeyIterator.hasNext()) {
                SelectionKey selectionKey = selectionKeyIterator.next();
                selectionKeyIterator.remove();

                if (selectionKey.isValid()) {
                    dispatch(selectionKey);
                }
            }
        }
    }

    private void dispatch(SelectionKey selectionKey) {
        Runnable attachment = (Runnable) selectionKey.attachment();
        attachment.run();
    }

    public static void main(String[] args) throws IOException {
        MainReactor reactor = new MainReactor();
        reactor.init();
        reactor.process();
    }
}
