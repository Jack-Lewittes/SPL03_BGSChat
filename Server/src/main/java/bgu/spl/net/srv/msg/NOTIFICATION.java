package bgu.spl.net.srv.msg;

import bgu.spl.net.srv.connections.Connections;

public class NOTIFICATION implements Message {
    public enum Type{PM, Post};

    private final short opCode;
    private final Type notificationType;
    private String content;
    private String postingUser;

    public NOTIFICATION(byte _notificationType, String _postingUser, String _content) {
        this.opCode = 9;
        if(_notificationType == 0)
            notificationType = Type.PM;
        else{
            notificationType = Type.Post;
        }
        this.content = _content;
        this.postingUser=_postingUser;
    }

    public int typeAsInt(){
        if(this.notificationType.equals(Type.Post))
            return 1;
        else{
            return 0;
        }
    }

    @Override
    public short getOpCode() {
        return opCode;
    }

    public char getNotificationType() {
        if (notificationType == Type.PM)
            return '0';
        return '1';
    }

    public String getContent() {
        return content;
    }

    public String getPostingUser() {
        return postingUser;
    }

    @Override
    public <T> void process(int connectionID, Connections<T> connections) {}

    public String typeAsString(){
        return "NOTIFICATION";
    }
}
