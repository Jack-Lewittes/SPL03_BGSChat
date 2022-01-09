package bgu.spl.net.srv.msg;

import bgu.spl.net.srv.connections.Connections;
import bgu.spl.net.srv.database.BGSDatabase;
import bgu.spl.net.srv.database.User;

import java.util.HashMap;

public class LOGSTAT implements Message {
    private final short opCode;
    BGSDatabase database = BGSDatabase.getInstance();


    public LOGSTAT() {
        opCode = 7;
    }

    @Override
    public <T> void process(int connectionID, Connections<T> connections) {
        HashMap<User, String> logStatInfo = database.logStat(connectionID);
        if(logStatInfo != null) {
            if(logStatInfo.isEmpty())
                connections.send(connectionID, (T) new ACK(opCode)); // empty response
            for (User user : logStatInfo.keySet()) {
                String msg = user.logStatInfo();
                connections.send(connectionID, (T) new ACK(opCode, msg));
            }
        }
        else{
            connections.send(connectionID, (T) new ERROR(opCode));
        }
    }

    public String typeAsString(){
        return "LOGSTAT";
    }

    @Override
    public short getOpCode() {
        return 7;
    }
}
