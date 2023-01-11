package nio.multithread;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description
 * @Author longyh
 * @Date 2023/1/10 13:12
 */
@Slf4j
public class MultiThreadServer {

    private final int port = 8888;
    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    private ThreadPoolExecutor threadPoolExecutor;

    public void init() throws IOException {
        this.threadPoolExecutor = new ThreadPoolExecutor(1, 2, 1, TimeUnit.MINUTES, new LinkedBlockingDeque<>());

        this.selector = Selector.open();
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(port));
        this.serverSocketChannel.configureBlocking(false);

        SelectionKey sscKey = this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void process() throws IOException {
        ServerWorker[] serverWorkers = new ServerWorker[2];
        while (true) {
            selector.select();
            Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();

            while (selectionKeyIterator.hasNext()) {
                SelectionKey selectionKey = selectionKeyIterator.next();
                selectionKeyIterator.remove();

                if (selectionKey.isAcceptable()) {
                    ServerSocketChannel ssc = (ServerSocketChannel) selectionKey.channel();

                    SocketChannel socketChannel = ssc.accept();
                    socketChannel.configureBlocking(false);
                    log.info("sc : {}", socketChannel);

                    ServerWorker worker = serverWorkers[socketChannel.getRemoteAddress().hashCode() % 2];
                    if (worker == null) {
                        worker = new ServerWorker();
                        worker.init();
                        new Thread(worker).start();
                        serverWorkers[socketChannel.getRemoteAddress().hashCode() % 2] = worker;
                    }
                    worker.process(socketChannel);

                }
            }
        }
    }

    public class ServerWorker implements Runnable {

        private boolean isInit = false;
        private Selector selector;
        private LinkedBlockingDeque<SocketChannel> deque = new LinkedBlockingDeque<>();


        public void init() throws IOException {
            if (!isInit) {
                this.selector = Selector.open();
            }
        }

        public void process(SocketChannel socketChannel) throws IOException {
            //
            deque.add(socketChannel);
            this.selector.wakeup();
        }

        @Override
        public void run() {
            try {
                while (true) {
                    selector.select();

                    SocketChannel sc = deque.poll();
                    if (sc != null) {
                        sc.register(this.selector, SelectionKey.OP_READ);
                    }

                    Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();

                    while (selectionKeyIterator.hasNext()) {
                        SelectionKey selectionKey = selectionKeyIterator.next();
                        selectionKeyIterator.remove();

                        if (selectionKey.isReadable()) {
                            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                            ByteBuffer byteBuffer = ByteBuffer.allocate(32);

                            socketChannel.read(byteBuffer);
                            byteBuffer.flip();
                            String str = new String(byteBuffer.array());
                            log.info("read {} str : {}", socketChannel.getRemoteAddress(), str);
                        }
                    }


                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        MultiThreadServer multiThreadServer = new MultiThreadServer();
        multiThreadServer.init();
        multiThreadServer.process();
    }
}
