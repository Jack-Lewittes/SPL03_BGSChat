package bgu.spl.net.srv.msg;

import bgu.spl.net.srv.connections.Connections;
import bgu.spl.net.srv.database.BGSDatabase;

public class REGISTER implements Message {
    final short opCode;
    final String username;
    final String password;
    final String birthday;
    BGSDatabase database = BGSDatabase.getInstance();

    public REGISTER(String _name, String _password, String _birthday) {
        this.opCode = 1;
        this.username = _name;
        this.password = _password;
        this.birthday = _birthday;
    }

    @Override
    public <T> void process(int connectionID, Connections<T> connections) {
        boolean attemptRegister = database.register(username, password, birthday, connectionID);
        if(attemptRegister) {
            connections.send(connectionID, (T) new ACK(opCode));
        }
        else{
            connections.send(connectionID, (T) new ERROR(opCode));
        }
    }

    public String typeAsString(){
        return "REGISTER";

    }

    @Override
    public short getOpCode() {
        return 1;
    }

}

