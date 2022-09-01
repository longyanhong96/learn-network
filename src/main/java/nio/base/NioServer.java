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

        Selector selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            int select = selector.select();

            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();

            while (keyIterator.hasNext()) {
                SelectionKey selectionKey = keyIterator.next();

                switch (selectionKey.interestOps()) {
                    case SelectionKey.OP_ACCEPT:
                        SocketChannel accept = serverSocketChannel.accept();
                        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

                        int read = accept.read(byteBuffer);
                        Buffer flip = byteBuffer.flip();

                        break;
                    case SelectionKey.OP_CONNECT:
                        break;
                    case SelectionKey.OP_READ:
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
