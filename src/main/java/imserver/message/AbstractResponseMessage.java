package imserver.message;

import lombok.Data;

import java.io.Serializable;

@Data
public abstract class AbstractResponseMessage extends Message {

    private boolean success;
    private String reason;

    public AbstractResponseMessage() {
    }

    public AbstractResponseMessage(boolean success,String reason){
        this.success = success;
        this.reason = reason;
    }

}
