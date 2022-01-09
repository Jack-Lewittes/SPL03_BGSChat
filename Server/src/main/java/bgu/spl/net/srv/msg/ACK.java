package bgu.spl.net.srv.msg;

import bgu.spl.net.srv.connections.Connections;

import java.util.Optional;

public class ACK implements Message {
    final short opCode;
    final short messageOptCode;
    Optional<String> additionalData;

    public ACK(short _messageOptCode) {
        opCode = 10;
        messageOptCode = _messageOptCode;
        additionalData = Optional.empty();
    }

    public ACK(short _messageOptCode, String _additionalData){
        opCode = 10;
        messageOptCode = _messageOptCode;
        additionalData = Optional.of(_additionalData);
    }
    public String typeAsString(){
        return "ACK";
    }

    @Override
    public short getOpCode() {
        return opCode;
    }

    public short getMessageOptCode() {
        return messageOptCode;
    }

    public String getAdditionalData() {
        return additionalData.orElse(null);
    }

    @Override
    public <T> void process(int connectionID, Connections<T> connections) {}
}
