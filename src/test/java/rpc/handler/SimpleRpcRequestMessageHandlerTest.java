package rpc.handler;

import io.netty.channel.embedded.EmbeddedChannel;
import junit.framework.TestCase;
import rpc.message.RpcRequestMessage;
import rpc.message.RpcResponseMessage;

import java.util.Arrays;

public class SimpleRpcRequestMessageHandlerTest extends TestCase {

    public void test1() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel();
        embeddedChannel.pipeline().addLast(new SimpleRpcRequestMessageHandler());

        embeddedChannel.writeInbound(new RpcRequestMessage("rpc.service.RpcServiceImpl", "result"));
        RpcResponseMessage rpcResponseMessage = (RpcResponseMessage) embeddedChannel.readOutbound();
        System.out.println("rpcResponseMessage = " + rpcResponseMessage);
    }

    public void test2() {
        EmbeddedChannel embeddedChannel = new EmbeddedChannel();
        embeddedChannel.pipeline().addLast(new SimpleRpcRequestMessageHandler());

        embeddedChannel.writeInbound(new RpcRequestMessage("rpc.service.RpcServiceImpl", "say", Arrays.asList(String.class, String.class), Arrays.asList("zhangsan", "hello")));
        RpcResponseMessage rpcResponseMessage = (RpcResponseMessage) embeddedChannel.readOutbound();
        System.out.println("rpcResponseMessage = " + rpcResponseMessage);
    }
}