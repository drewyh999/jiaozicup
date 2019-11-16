package json;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Date;

public class Message implements Cloneable{
    public String callType;
    public int channelId;
    public SysMessage sysMessage;
    public ExtMessage extMessage;
    public int state;
    public int errCode;
    public int channelType;
    public int targetId;
    @JsonIgnore
    public double recvTime;

    public Message() {
        recvTime = new Date().getTime() / 1000.0;
    }

    @Override
    public Message clone() throws CloneNotSupportedException {
        return (Message) super.clone();
    }
}
