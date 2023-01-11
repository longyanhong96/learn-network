package nio.reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @Description
 * @Author longyh
 * @Date 2023/1/11 18:08
 */
public class Reactor {

    private final int port = 8888;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public void init() throws IOException {
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);

        this.selector = Selector.open();
        SelectionKey sscKey = this.serverSocketChannel.register(this.selector, SelectionKey.OP_ACCEPT);

    }

    public void process() throws IOException {
        while (true){
            this.selector.select();
            Iterator<SelectionKey> selectionKeyIterator = this.selector.selectedKeys().iterator();

            while (selectionKeyIterator.hasNext()){
                SelectionKey selectionKey = selectionKeyIterator.next();
                selectionKeyIterator.remove();

                if (selectionKey.isValid()){
                    dispatch(selectionKey);
                }
            }
        }
    }

    private void dispatch(SelectionKey selectionKey) {

        selectionKey.attachment();
    }

    public class Acceptor{

        public void process(SelectionKey selectionKey) throws IOException {
            ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();
            SocketChannel socketChannel = ssc.accept();
        }
    }
}
