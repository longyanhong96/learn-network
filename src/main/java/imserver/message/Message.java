package imserver.message;


import java.io.Serializable;

public abstract class Message implements Serializable {

    private int sequenceId;

    public int getSequenceId() {
        return this.sequenceId;
    }

    private int messageType;

    public abstract int getMessageType();

    public static final int LoginRequestMessage = 0;
    public static final int LoginResponseMessage = 1;
}
