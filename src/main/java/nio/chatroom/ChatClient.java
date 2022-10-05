package nio.chatroom;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @Description
 * @Author longyh
 * @Date 2022/9/1 16:13
 */
@Slf4j
public class ChatClient implements Runnable {

    Selector selector = null;
    SocketChannel socketChannel = null;

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        new Thread(chatClient).start();
    }

    @Override
    public void run() {

        try {
            selector = Selector.open();
            socketChannel = SocketChannel.open();
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_CONNECT, ByteBuffer.allocate(1024));
            // connect要在注册之后，不然没有connect事件发生
            socketChannel.connect(new InetSocketAddress("localhost", 8888));

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Scanner scanner = new Scanner(System.in);
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    while (true){
                        System.out.print("输入字符串：");
                        String inputString = scanner.next();
                        byteBuffer.put(inputString.getBytes(StandardCharsets.UTF_8));
                        try {
                            byteBuffer.flip();
                            socketChannel.write(byteBuffer);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        byteBuffer.clear();
                    }
                }
            }).start();

            while (true) {
                selector.select();
                log.info("socketchannel start!!!!");
                Set<SelectionKey> selectionKeys = selector.selectedKeys();

                Iterator<SelectionKey> selectionKeyIterator = selectionKeys.iterator();
                while (selectionKeyIterator.hasNext()) {
                    SelectionKey selectionKey = selectionKeyIterator.next();
                    if (selectionKey.isConnectable()) {
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        channel.finishConnect();

                        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                        String format = String.format("%s 上线了!!!", channel.getLocalAddress());
                        byteBuffer.put(format.getBytes(StandardCharsets.UTF_8));
                        byteBuffer.flip();
                        channel.write(byteBuffer);
                        channel.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024));
                    } else if (selectionKey.isReadable()) {
                        SocketChannel channel = (SocketChannel) selectionKey.channel();
                        ByteBuffer byteBuffer = (ByteBuffer) selectionKey.attachment();
                        processSocketRead(channel, byteBuffer);
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

            if (socketChannel != null) {
                try {
                    socketChannel.close();
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
}
