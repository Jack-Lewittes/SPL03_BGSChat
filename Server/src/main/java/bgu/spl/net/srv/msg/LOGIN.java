package bgu.spl.net.srv.msg;

import bgu.spl.net.srv.connections.Connections;
import bgu.spl.net.srv.database.BGSDatabase;

public class LOGIN implements Message {
    final short opCode;
    final String username;
    final String password;
    final byte captcha;
    BGSDatabase database = BGSDatabase.getInstance();

    public LOGIN(String _username, String _password, byte _captcha) {
        this.opCode = 2;
        this.username = _username;
        this.password = _password;
        this.captcha = _captcha;
    }
    public String typeAsString(){
        return "LOGIN";
    }

    @Override
    public short getOpCode() {
        return 2;
    }

    @Override
    public <T> void process(int connectionID, Connections<T> connections) {
        if (captcha == 0) {
            connections.send(connectionID, (T) new ERROR(opCode));
        } else {
            boolean loginAttempt = database.logIn(connectionID, connections, username, password);
            if (loginAttempt) {
                //update CH_id (if not first login)
                database.getUserMapByConn().get(connectionID).setConnectionHandlerID(connectionID);
                connections.send(connectionID, (T) new ACK(opCode));
            }
            else {
                connections.send(connectionID, (T) new ERROR(opCode));
            }
        }
    }
}
