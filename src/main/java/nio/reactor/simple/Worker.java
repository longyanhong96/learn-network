package nio.reactor.simple;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * @Description 其实socketchannel就是serverSocketChannel.appect得到的，得到后，和新的worker一一绑定；
 * 如果触发事件，完全可以不用selectorKey.channel来获取对应的socketChannel，直接获取附带的attachment里面的worker就行，里面包含了socketChannel
 * @Author longyh
 * @Date 2023/1/12 10:46
 */
@Slf4j
public class Worker implements Runnable{

    private SocketChannel socketChannel;

    public Worker(SocketChannel socketChannel){
        this.socketChannel = socketChannel;
    }

    @Override
    public void run() {
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(32);
            socketChannel.read(byteBuffer);

            byteBuffer.flip();
            String str = new String(byteBuffer.array());
            log.info("read {} str : {}", socketChannel.getRemoteAddress(), str);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
