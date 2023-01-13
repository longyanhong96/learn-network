package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

/**
 * @Description
 * @Author longyh
 * @Date 2023/1/13 9:42
 */
@Slf4j
public class NettyDiscardServer {

    private static final int serverPort = 8888;

    public static void main(String[] args) throws InterruptedException {
        Channel channel = new ServerBootstrap()
                // 对应reactor
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new StringDecoder());
                        socketChannel.pipeline().addLast(new NettyDiscardHandler());
                    }
                })
                .bind(serverPort)
                .sync()
                .channel();

        channel.closeFuture().sync();
    }

    public static class NettyDiscardHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            String s = (String) msg;
            log.info("{} read {}", ctx.channel(), s);
        }
    }
}
