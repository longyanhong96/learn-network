package nio.chatroom;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * @Description
 * @Author longyh
 * @Date 2022/9/1 14:43
 */
@Slf4j
public class ChatServer implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ChatServer.class);

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        new Thread(chatServer).start();
    }

    @Override
    public void run() {
        Selector selector = null;
        ServerSocketChannel serverSocketChannel = null;
        try {
            selector = Selector.open();

            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.bind(new InetSocketAddress(8888));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (true) {
                selector.select();

                Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next();

                    if (selectionKey.isAcceptable()) {
                        ServerSocketChannel serverSocket = (ServerSocketChannel) selectionKey.channel();
                        SocketChannel socketChannel = serverSocket.accept();
                        socketChannel.configureBlocking(false);
                        socketChannel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                        log.info("socket address connect : {}", socketChannel);
                    } else if (selectionKey.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                        String message = processSocketRead(socketChannel, byteBuffer);
                        log.info("socketChannel : {} , message : {}", socketChannel, message);
                        sendMessageToOther(message, selector.keys().iterator(), socketChannel);
                    }

                    selectionKeyIterator.remove();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (selector != null) {
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (serverSocketChannel != null) {
                try {
                    serverSocketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    private String processSocketRead(SocketChannel socketChannel, ByteBuffer byteBuffer) throws IOException {
        StringBuffer stringBuffer = new StringBuffer();
        while (socketChannel.read(byteBuffer) > 0) {
            byteBuffer.flip();

            String s = new String(byteBuffer.array(), 0, byteBuffer.limit());
            stringBuffer.append(s);

            byteBuffer.clear();
        }
        log.info("socket address : {},message : {}", socketChannel.getRemoteAddress(), stringBuffer.toString());

        return stringBuffer.toString();
    }

    private void sendMessageToOther(String message, Iterator<SelectionKey> selectionKeyIterator, SocketChannel socketChannel) throws IOException {
        while (selectionKeyIterator.hasNext()) {
            SelectionKey selectionKey = selectionKeyIterator.next();
            if (selectionKey.channel() instanceof SocketChannel) {
                SocketChannel channel = (SocketChannel) selectionKey.channel();
                if (channel == socketChannel) {
                    continue;
                }
                ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
                buffer.put(message.getBytes(StandardCharsets.UTF_8));
                buffer.flip();
                channel.write(buffer);
                buffer.clear();
            }
        }
    }
}
