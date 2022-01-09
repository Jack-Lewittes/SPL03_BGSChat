package bgu.spl.net.srv;

import bgu.spl.net.api.MessageEncoderDecoder;
import bgu.spl.net.srv.msg.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;


public class BGSEncDec implements MessageEncoderDecoder<Message> {

    private short opCode = -1;
    private final ByteBuffer opcodeBuffer = ByteBuffer.allocate(2);
    private byte[] bytesArr = null;
    private int length = 0;
    private int stringCount = 0;

    @Override
    public Message decodeNextByte(byte nextByte) {
        if (bytesArr == null){
            opcodeBuffer.put(nextByte);
            if (!opcodeBuffer.hasRemaining()){
                opcodeBuffer.flip();
                bytesArr = new byte[1 << 10];
                length = 0;
                opCode = bytesToShort(opcodeBuffer.array());
                opcodeBuffer.clear();
            }
            switch (opCode){
                case 3:
                case 7:
                    return initMsg();
                default:
                    return null;
            }
        }
        else{
            switch (opCode){
                case 1:
                case 2:
                case 6:
                    if (nextByte == 0 || nextByte == ';'){
                        if (stringCount == 2){
                            return initMsg();
                        }
                        else{stringCount++;}
                    }
                    pushByte(nextByte);
                    break;
                case 4:
                    if (nextByte == 0 || nextByte == ';') {
                        if (stringCount == 1)
                            return initMsg();
                        else {stringCount++;}
                    }
                    pushByte(nextByte);
                    break;
                case 5:
                case 8:
                case 12:
                    if (nextByte == 0)
                        return initMsg();
                    pushByte(nextByte);
                    break;
            }
        }
        return null;
    }

    @Override
    public byte[] encode(Message message) {
        if (message != null) {
            opCode = message.getOpCode();
            switch ((opCode)) {
                case (9):
                    NOTIFICATION notification = (NOTIFICATION) message;
                    byte[] notifopcode = shortToBytes(notification.getOpCode());
                    byte[] type = {(byte)notification.getNotificationType()};
                    byte[] poster = notification.getPostingUser().getBytes(StandardCharsets.UTF_8);
                    byte[] content = notification.getContent().getBytes(StandardCharsets.UTF_8);
                    byte[] zero = shortToBytes((short)0);
                    int notiLength = notifopcode.length + type.length + poster.length + (2 * zero.length) + content.length + 1;
                    ByteBuffer bb = ByteBuffer.wrap(new byte[notiLength]);
                    bb.put(notifopcode);
                    bb.put(type);
                    bb.put(poster);
                    bb.put((byte) '0');
                    bb.put(content);
                    bb.put((byte) '0');
                    bb.put((byte)';');
                    opCode = -1;
                    return bb.array();

                case (10):
                    ACK ack = (ACK) message;
                    byte[] ackOpcode = shortToBytes(ack.getOpCode());
                    byte[] messageopcode = shortToBytes(ack.getMessageOptCode());
                    byte[] additionalInfo = null;
                    int adiLength = 0;
                    if(ack.getAdditionalData() != null) {
                        additionalInfo = ack.getAdditionalData().getBytes(StandardCharsets.UTF_8);
                        adiLength = additionalInfo.length;
                    }
                    int ackLength = ackOpcode.length + messageopcode.length+adiLength+1;
                    ByteBuffer bb2 = ByteBuffer.wrap(new byte[ackLength]);
                    bb2.put(ackOpcode);
                    bb2.put(messageopcode);
                    if(additionalInfo != null)
                        bb2.put(additionalInfo);
                    bb2.put((byte)';');
                    opCode = -1;
                    return bb2.array();

                case(11):
                    ERROR error = (ERROR) message;
                    byte[] erOpcode = shortToBytes(error.getOpCode());
                    byte[] erMsgCode = shortToBytes(error.getMessageOptCode());
                    int errLength = erMsgCode.length + erOpcode.length;
                    ByteBuffer bb3 = ByteBuffer.wrap(new byte[errLength]);
                    bb3.put(erOpcode);
                    bb3.put(erMsgCode);
                    opCode = -1;
                    return bb3.array();
            }
        }
        opCode = -1;
        return null;
    }

    public byte[] shortToBytes(short num) {
        byte[] bytesArr = new byte[2];
        bytesArr[0] = (byte)((num >> 8) & 0xFF);
        bytesArr[1] = (byte)(num & 0xFF);
        return bytesArr;
    }

    public short bytesToShort(byte[] byteArr) {
        short result = (short)((byteArr[0] & 0xff) << 8);
        result += (short)(byteArr[1] & 0xff);
        return result;
    }

    private Message initMsg(){
        Message msg = null;
        switch(opCode) {
            case 1:
                String[] userInfo = parser();
                msg = new REGISTER(userInfo[0], userInfo[1], userInfo[2]);
                break;
            case 2:
                String[] loginInfo = parser();
                msg = new LOGIN(loginInfo[0], loginInfo[1], Byte.parseByte(loginInfo[2]));
                break;
            case 3:
                msg = new LOGOUT();
                break;
            case 4:
                String[] followInfo = parser();
                String byteString = followInfo[0].substring(0,1);
                String nameString = followInfo[0].substring(1);
                String[] follow = {byteString, nameString};
                msg = new FOLLOW(
                        Byte.parseByte(follow[0]),
                        follow[1]);
                break;
            case 5:
                String[] postInfo = parser();
                msg = new POST((postInfo[0]));
                break;
            case 6:
                String[] PMInfo = parser();
                msg = new PM((PMInfo[0]), PMInfo[1], PMInfo[2]);
                break;
            case 7:
                msg = new LOGSTAT();
                break;
            case 8:
                String[] statInfo = parser();
                msg = new STAT(statInfo[0]);
                break;
            case 9:
                String[] notifInfo = parser();
                msg = new NOTIFICATION(Byte.parseByte(notifInfo[0]), notifInfo[1], notifInfo[2]);
                break;
            case 10:
                String[] ackInfo = parser();
                if (ackInfo.length == 2)
                    msg = new ACK(Byte.parseByte(ackInfo[0]), ackInfo[1]);
                else {
                    msg = new ACK(Byte.parseByte(ackInfo[0]));
                }
                break;
            case 11:
                String[] errInfo = parser();
                msg = new ERROR(Byte.parseByte(errInfo[0]));
                break;
            case 12:
                String[] blockInfo = parser();
                msg = new BLOCK(blockInfo[0]);
                break;
        }
        bytesArr = null;
        opCode = -1;
        stringCount = 0;
        return msg;
    }

    private String[] parser(){
        String output = new String(bytesArr, 0, length, StandardCharsets.UTF_8);
        return output.split("\0");
    }

    private void pushByte(byte next){
        if(length >= bytesArr.length)
            bytesArr = Arrays.copyOf(bytesArr, length * 2);
        bytesArr[length++] = next;
    }
}
