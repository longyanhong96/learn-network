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
        ServerWorker serverWorker = new ServerWorker();
        serverWorker.init();
        new Thread(serverWorker).start();

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

                    serverWorker.selector.wakeup();
                    socketChannel.register(serverWorker.selector, SelectionKey.OP_READ);

                }
            }
        }
    }

    public class ServerWorker implements Runnable{

        private boolean isInit = false;
        private Selector selector;


        public void init() throws IOException {
            if (!isInit) {
                this.selector = Selector.open();
            }
        }

        public void process() throws IOException {
            while (true) {
                selector.select();
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
                        log.info("read str : {}", str);
                    }
                }
            }
        }

        @Override
        public void run() {
            try {
                process();
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
