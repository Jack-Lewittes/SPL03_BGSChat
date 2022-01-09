package bgu.spl.net.srv.msg;

import bgu.spl.net.srv.connections.Connections;
import bgu.spl.net.srv.database.BGSDatabase;

public class LOGOUT implements Message {
    final short opCode;
    BGSDatabase database = BGSDatabase.getInstance();

    public LOGOUT() {
        this.opCode = 3;
    }

    @Override
    public <T> void process(int connectionID, Connections<T> connections) {
        boolean attemptLogOut = database.logOut(connectionID);
        if(attemptLogOut)
            connections.send(connectionID, (T) new ACK(opCode));
        else{
            connections.send(connectionID, (T) new ERROR(opCode));
        }
    }

    public String typeAsString(){
        return "LOGOUT";

    }

    @Override
    public short getOpCode() {
        return 3;
    }

}

