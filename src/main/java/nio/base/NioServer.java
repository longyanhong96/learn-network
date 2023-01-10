package nio.base;

import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * @Description
 * @Author longyh
 * @Date 2022/8/30 16:22
 */
@Slf4j
public class NioServer {
    public static void main(String[] args) throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress("localhost", 8888));
        serverSocketChannel.configureBlocking(false);

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            int select = selector.select();

            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

            while (keyIterator.hasNext()) {
                SelectionKey selectionKey = keyIterator.next();
                keyIterator.remove();

                switch (selectionKey.interestOps()) {
                    case SelectionKey.OP_ACCEPT:
                        ServerSocketChannel channel = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel socketChannel = channel.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector,SelectionKey.OP_READ);
                        break;
                    case SelectionKey.OP_READ:
                        SocketChannel sc = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(16);

                        int read = sc.read(byteBuffer);
                        System.out.println("read = " + read);

                        byteBuffer.flip();
                        String str = new String(byteBuffer.array(), 0, byteBuffer.limit());
                        System.out.println("str = " + str);

                        byteBuffer.clear();
                        break;
                    case SelectionKey.OP_WRITE:
                        break;
                    default:
                        break;
                }
            }
        }
    }
}
