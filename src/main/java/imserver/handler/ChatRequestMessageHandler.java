package imserver.handler;

import imserver.message.ChatRequestMessage;
import imserver.message.ChatResponseMessage;
import imserver.session.SessionFactory;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@ChannelHandler.Sharable
@Slf4j
public class ChatRequestMessageHandler extends SimpleChannelInboundHandler<ChatRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ChatRequestMessage chatRequestMessage) throws Exception {
        String from = chatRequestMessage.getFrom();
        String to = chatRequestMessage.getTo();
        String content = chatRequestMessage.getContent();

        Channel channel = SessionFactory.getSession().getChannel(to);
        log.info("channel : {}", channel);
        // 在线
        if (channel != null) {
            channel.writeAndFlush(new ChatResponseMessage(from, content));
        } else {
            ctx.writeAndFlush(new ChatResponseMessage(false, "对方用户不存在或者不在线"));
        }
    }
}
