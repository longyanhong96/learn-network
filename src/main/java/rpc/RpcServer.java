package rpc;

import imserver.handler.LoginRequestMessageHandler;
import imserver.protocol.MessageCodecSharable;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class RpcServer {

    private NioEventLoopGroup boss;
    private NioEventLoopGroup worker;

    private LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
    private MessageCodecSharable messageCodecSharable = new MessageCodecSharable();

    /**
     * handler
     */


    public void process(){
        try {
            this.boss = new NioEventLoopGroup();
            this.worker = new NioEventLoopGroup();

            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(this.boss, this.worker);
            serverBootstrap.channel(NioServerSocketChannel.class);

            serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0));
                    socketChannel.pipeline().addLast(loggingHandler);
                    socketChannel.pipeline().addLast(messageCodecSharable);
                    /**
                     * handler
                     */


                }
            });

            Channel channel = serverBootstrap.bind(8888).sync().channel();
            channel.closeFuture().sync();

        }catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }
}
