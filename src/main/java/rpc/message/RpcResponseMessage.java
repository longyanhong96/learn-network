package rpc.message;

import imserver.message.AbstractResponseMessage;
import lombok.Data;

@Data
public class RpcResponseMessage extends AbstractResponseMessage {

    private String returnResult;

    public RpcResponseMessage(boolean success, String reason) {
        super(success, reason);
    }

    public RpcResponseMessage(String returnResult) {
        this.returnResult = returnResult;
    }

    @Override
    public int getMessageType() {
        return 0;
    }
}
