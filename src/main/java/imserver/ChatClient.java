package imserver;

import imserver.message.ChatRequestMessage;
import imserver.message.GroupCreateRequestMessage;
import imserver.message.LoginRequestMessage;
import imserver.message.LoginResponseMessage;
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
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ChatClient {

    private NioEventLoopGroup group = new NioEventLoopGroup();
    private LoggingHandler loggingHandler = new LoggingHandler(LogLevel.DEBUG);
    private MessageCodecSharable messageCodecSharable = new MessageCodecSharable();

    // 两个线程互动
    private CountDownLatch WAIT_FOR_LOGIN = new CountDownLatch(1);
    private AtomicBoolean LOGIN = new AtomicBoolean(false);
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

                                    try {
                                        WAIT_FOR_LOGIN.await();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    // 如果登录失败
                                    if (!LOGIN.get()) {
                                        ctx.channel().close();
                                        return;
                                    }

                                    while (true) {
                                        System.out.println("==================================");
                                        System.out.println("send [username] [content]");
                                        System.out.println("gsend [group name] [content]");
                                        System.out.println("gcreate [group name] [m1,m2,m3...]");
                                        System.out.println("gmembers [group name]");
                                        System.out.println("gjoin [group name]");
                                        System.out.println("gquit [group name]");
                                        System.out.println("quit");
                                        System.out.println("==================================");

                                        String command = scanner.nextLine();
                                        String[] s = command.split(" ");
                                        switch (s[0]) {
                                            case "send":
                                                ctx.writeAndFlush(new ChatRequestMessage(username, s[1], s[2]));
                                                break;
                                            case "gcreate":
                                                Set<String> members = new HashSet<>(Arrays.asList(s[2].split(",")));
                                                members.add(username);
                                                ctx.writeAndFlush(new GroupCreateRequestMessage(s[1], members));
                                                break;
                                            default:
                                                break;
                                        }
                                    }

                                }
                            }).start();
                        }

                        @Override
                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                            log.debug("msg: {}", msg);
                            if (msg instanceof LoginResponseMessage) {
                                LoginResponseMessage loginResponseMessage = (LoginResponseMessage) msg;
                                if (loginResponseMessage.isSuccess()) {
                                    LOGIN.set(true);
                                }
                                // 唤醒 system in 线程
                                WAIT_FOR_LOGIN.countDown();
                            }
                        }

                        @Override
                        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
                            log.debug("连接已经断开，按任意键退出..");
                            EXIT.set(true);
                        }

                        @Override
                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                            log.debug("连接已经断开，按任意键退出..{}", cause.getMessage());
                            EXIT.set(true);
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
