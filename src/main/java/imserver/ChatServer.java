package imserver;


import imserver.handler.ChatRequestMessageHandler;
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
import lombok.extern.slf4j.Slf4j;


@Slf4j
public class ChatServer {

    private NioEventLoopGroup boss;
    private NioEventLoopGroup worker;

    private LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
    private MessageCodecSharable messageCodecSharable = new MessageCodecSharable();

    /**
     * handler
     */
    private LoginRequestMessageHandler loginHandler = new LoginRequestMessageHandler();
    private ChatRequestMessageHandler chatRequestMessageHandler = new ChatRequestMessageHandler();


    private void init() {
        this.boss = new NioEventLoopGroup();
        this.worker = new NioEventLoopGroup();
    }

    public void process() {
        try {
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
                    socketChannel.pipeline().addLast(loginHandler);
                    socketChannel.pipeline().addLast(chatRequestMessageHandler);
                }
            });

            Channel channel = serverBootstrap.bind(8888).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.init();
        chatServer.process();
    }
}
