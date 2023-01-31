package rpc.message;

import imserver.message.Message;
import lombok.Data;

import java.util.List;

@Data
public class RpcRequestMessage extends Message {

    private String className;

    private String methodName;

    private List<Class> methodParameterTypes;

    private List<Object> methodParameterArgs;

    public RpcRequestMessage(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    public RpcRequestMessage(String className, String methodName, List<Class> methodParameterTypes, List<Object> methodParameterArgs) {
        this(className, methodName);
        this.methodParameterTypes = methodParameterTypes;
        this.methodParameterArgs = methodParameterArgs;
    }

    @Override
    public int getMessageType() {
        return 0;
    }
}
