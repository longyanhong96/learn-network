package imserver.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequestMessage extends Message{

    private String from;
    private String to;
    private String content;



    @Override
    public int getMessageType() {
        return ChatRequestMessage;
    }
}
