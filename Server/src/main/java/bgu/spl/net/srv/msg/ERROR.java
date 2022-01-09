package bgu.spl.net.srv.msg;

import bgu.spl.net.srv.connections.Connections;

public class ERROR implements Message {
    final short opCode;
    final short messageOptCode;
    public ERROR(short _messageOptCode) {
        opCode = 11;
        messageOptCode = _messageOptCode;
    }
    public String typeAsString(){
        return "ERROR";

    }
    @Override
    public short getOpCode() {
        return opCode;
    }

    public short getMessageOptCode() {
        return messageOptCode;
    }

    @Override
    public <T> void process(int connectionID, Connections<T> connections) {

    }
}
