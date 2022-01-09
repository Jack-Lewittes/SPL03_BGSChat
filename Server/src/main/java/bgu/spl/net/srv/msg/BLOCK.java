package bgu.spl.net.srv.msg;

import bgu.spl.net.srv.connections.Connections;
import bgu.spl.net.srv.database.BGSDatabase;

public class BLOCK implements Message {
    final short opCode;
    final String blockUser;
    BGSDatabase database = BGSDatabase.getInstance();

    public BLOCK(String _blockUser) {
        opCode = 12;
        this.blockUser = _blockUser;
    }
    public String typeAsString(){
        return "BLOCK";

    }

    @Override
    public short getOpCode() {
        return 12;
    }

    public <T> void process(int connectionID, Connections<T> connections) {
        String userToBlock = database.getUserMap().get(blockUser).getUsername();
        boolean attemptBlock = database.block(connectionID, userToBlock);
        if(attemptBlock) {
            connections.send(connectionID, (T) new ACK(opCode));
            database.performBlock(connectionID, userToBlock);
        }

        else{
            connections.send(connectionID, (T) new ERROR(opCode));
        }
    }
}
