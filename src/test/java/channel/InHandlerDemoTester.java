package channel;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * @Description
 * @Author longyh
 * @Date 2023/1/16 10:40
 */
@Slf4j
public class InHandlerDemoTester {

    @Test
    public void testInHandlerLifeCircle() {
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInitializer<EmbeddedChannel>() {
            @Override
            protected void initChannel(EmbeddedChannel embeddedChannel) throws Exception {
                embeddedChannel.pipeline().addLast(new InHandlerDemo());
            }
        });

        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(1);
        //模拟入站，向嵌入式通道写一个入站数据包
        channel.writeInbound(buf);
        channel.flush();
        //模拟入站，再写一个入站数据包
        channel.writeInbound(buf);
        channel.flush();
        //通道关闭
        channel.close();
    }

    public class InHandlerDemo extends ChannelInboundHandlerAdapter{

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            log.info("被调用：handlerAdded()");
            super.handlerAdded(ctx);
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            log.info("被调用：channelRegistered()");
            super.channelRegistered(ctx);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("被调用：channelActive()");
            super.channelActive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.info("被调用：channelRead()");
            super.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            log.info("被调用：channelReadComplete()");
            super.channelReadComplete(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.info("被调用：channelInactive()");
            super.channelInactive(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            log.info("被调用：channelUnregistered()");
            super.channelUnregistered(ctx);
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
            log.info("被调用：handlerRemoved()");
            super.handlerRemoved(ctx);
        }
    }
}
