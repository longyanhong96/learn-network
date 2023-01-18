package imserver;

import imserver.message.LoginRequestMessage;
import imserver.protocol.MessageCodecSharable;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {

    private NioEventLoopGroup group = new NioEventLoopGroup();
    private LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
    private MessageCodecSharable messageCodecSharable = new MessageCodecSharable();

    private AtomicBoolean EXIT = new AtomicBoolean(false);

    private void process() {
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new LengthFieldBasedFrameDecoder(1024, 12, 4, 0, 0));
                    socketChannel.pipeline().addLast(loggingHandler);
                    socketChannel.pipeline().addLast(messageCodecSharable);
                    socketChannel.pipeline().addLast("client handler", new ChannelInboundHandlerAdapter() {
                        @Override
                        public void channelActive(ChannelHandlerContext ctx) throws Exception {

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    Scanner scanner = new Scanner(System.in);
                                    System.out.println("输入登录用户名：");
                                    String username = scanner.next();
                                    System.out.println("输入登录密码：");
                                    String password = scanner.next();
                                    LoginRequestMessage loginRequestMessage = new LoginRequestMessage(username, password);
                                    log.info("loginRequestMessage : {}", loginRequestMessage);
                                    ctx.writeAndFlush(loginRequestMessage);
                                }
                            }).start();
                        }

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("msg: {}", msg);
                        }
                    });

                }
            });


            Channel channel = bootstrap.connect(new InetSocketAddress(8888)).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();
        }

    }

    public static void main(String[] args) {
        ChatClient chatClient = new ChatClient();
        chatClient.process();
    }
}
