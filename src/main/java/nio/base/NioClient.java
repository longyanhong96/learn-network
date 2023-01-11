package nio.base;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * @Description
 * @Author longyh
 * @Date 2023/1/10 13:38
 */
public class NioClient {

    private final int port = 8888;

    private Selector selector;

    private SocketChannel socketChannel;

    public void init() throws IOException {
        this.selector = Selector.open();
        this.socketChannel = SocketChannel.open();
        this.socketChannel.configureBlocking(false);
        this.socketChannel.register(this.selector, SelectionKey.OP_CONNECT);

        this.socketChannel.connect(new InetSocketAddress("localhost", port));

        System.out.println("this.socketChannel.isConnected() = " + this.socketChannel.isConnected());
    }

    public void process() throws IOException {

        while (true) {
            selector.select();
            Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();

            while (selectionKeyIterator.hasNext()) {
                SelectionKey selectionKey = selectionKeyIterator.next();
                selectionKeyIterator.remove();

                if (selectionKey.isConnectable()) {
                    SocketChannel sc = (SocketChannel) selectionKey.channel();
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    String message = "hello!";
                    byteBuffer.put(message.getBytes(StandardCharsets.UTF_8));
                    byteBuffer.flip();
                    sc.write(byteBuffer);
                }
            }
        }

    }

    public static void main(String[] args) throws IOException {
//        NioClient nioClient = new NioClient();
//        nioClient.init();
//        nioClient.process();
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        socketChannel.register(selector, SelectionKey.OP_CONNECT);

        socketChannel.connect(new InetSocketAddress("localhost", 8888));

        while (true) {
            selector.select();

            Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();

            while (selectionKeyIterator.hasNext()) {
                SelectionKey selectionKey = selectionKeyIterator.next();
                selectionKeyIterator.remove();

                if (selectionKey.isConnectable()) {
                    SocketChannel sc = (SocketChannel) selectionKey.channel();
                    sc.finishConnect();

                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    String message = "hello!";
                    byteBuffer.put(message.getBytes(StandardCharsets.UTF_8));
                    byteBuffer.flip();
                    sc.write(byteBuffer);
                }
            }
        }
    }

}
