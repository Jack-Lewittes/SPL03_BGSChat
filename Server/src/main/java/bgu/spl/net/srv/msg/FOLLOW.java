package bgu.spl.net.srv.msg;

import bgu.spl.net.srv.connections.Connections;
import bgu.spl.net.srv.database.BGSDatabase;

public class FOLLOW implements Message {
    enum Action{follow, unfollow};
    final Action action;
    final short opCode;
    final String userToFollow;
    BGSDatabase database = BGSDatabase.getInstance();

    public FOLLOW(Byte actionType, String _userToFollow) {
        opCode = 4;
        this.userToFollow = _userToFollow;
        if(actionType == 0)
            action = Action.follow;
        else{
            action = Action.unfollow;
        }

    }
    public String typeAsString(){
        return "FOLLOW";

    }
    public short subTypeAsShort(){
        if(action.equals(Action.follow))
            return 0;
        return 1;
    }

    @Override
    public <T> void process(int connectionID, Connections<T> connections) {
        boolean attemptFollow;
        if(action == Action.follow)
            attemptFollow = database.follow(connectionID, userToFollow);
        else {
            attemptFollow = database.unfollow(connectionID, userToFollow);
        }
        if(attemptFollow) {
            String output = subTypeAsShort() + " " + userToFollow;
            connections.send(connectionID, (T) new ACK(opCode, output));
        }
        else{
            connections.send(connectionID, (T) new ERROR(opCode));
        }
    }

    @Override
    public short getOpCode() {
        return 4;
    }
}
