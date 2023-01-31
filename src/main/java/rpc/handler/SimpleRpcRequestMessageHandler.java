package rpc.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import rpc.message.RpcRequestMessage;
import rpc.message.RpcResponseMessage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class SimpleRpcRequestMessageHandler extends SimpleChannelInboundHandler<RpcRequestMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequestMessage rpcRequestMessage) throws Exception {
        Object result = getResultWithParameters(rpcRequestMessage);

        RpcResponseMessage rpcResponseMessage = new RpcResponseMessage(result.toString());
        channelHandlerContext.writeAndFlush(rpcResponseMessage);
    }

    private Object getSimpleResult(RpcRequestMessage rpcRequestMessage) throws Exception {
        Class<?> clazz = Class.forName(rpcRequestMessage.getClassName());
        Object instance = clazz.newInstance();
        Method method = clazz.getMethod(rpcRequestMessage.getMethodName());
        Object result = method.invoke(instance);

        return result;
    }

    private Object getResultWithParameters(RpcRequestMessage rpcRequestMessage) throws Exception {
        Class<?> clazz = Class.forName(rpcRequestMessage.getClassName());
        Object instance = clazz.newInstance();

        List<Class> methodParameterTypes = rpcRequestMessage.getMethodParameterTypes();
        Class[] classes = methodParameterTypes.toArray(new Class[2]);
        Method method = clazz.getMethod(rpcRequestMessage.getMethodName(), classes);
        Object invoke = method.invoke(instance, rpcRequestMessage.getMethodParameterArgs().toArray());
        return invoke;
    }
}
