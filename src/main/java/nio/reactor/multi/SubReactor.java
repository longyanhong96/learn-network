package nio.reactor.multi;

import nio.reactor.simple.Worker;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @Description
 * @Author longyh
 * @Date 2023/1/12 13:30
 */
public class SubReactor implements Runnable {

    private Selector selector;
    private LinkedBlockingDeque<SocketChannel> deque = new LinkedBlockingDeque<>();

    public void init() throws IOException {
        this.selector = Selector.open();
    }

    public void process(SocketChannel socketChannel) throws IOException {
        //
        deque.add(socketChannel);
        this.selector.wakeup();
    }

    @Override
    public void run() {
        while (true) {
            try {
                selector.select();
                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();

                SocketChannel sc = deque.poll();
                if (sc != null) {
                    SelectionKey scKey = sc.register(this.selector, SelectionKey.OP_READ);
                    scKey.attach(new Worker(sc));
                }

                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    selectionKeyIterator.remove();

                    if (selectionKey.isValid()) {
                        dispatch(selectionKey);
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void dispatch(SelectionKey selectionKey) {
        Runnable attachment = (Runnable) selectionKey.attachment();
        attachment.run();
    }
}
