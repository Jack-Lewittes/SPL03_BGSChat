package bgu.spl.net.srv.msg;

import bgu.spl.net.srv.connections.Connections;
import bgu.spl.net.srv.database.BGSDatabase;

import java.util.LinkedList;
import java.util.regex.Pattern;

public class STAT implements Message {
    private final short opCode;
    final LinkedList<String> otherUsersList;
    BGSDatabase database = BGSDatabase.getInstance();

    public STAT(String otherUsers) {
        opCode = 8;
        otherUsersList = splitInput(otherUsers);
    }

    private LinkedList<String> splitInput(String otherUsers){
        LinkedList<String> output = new LinkedList<>();
        String[] temp = otherUsers.split(Pattern.quote("|"));
        for(String user: temp){
            output.add(user);
        }
        return output;
    }

    @Override
    public <T> void process(int connectionID, Connections<T> connections) {
        LinkedList<String> userStat = database.stat(otherUsersList, connectionID);
        if(userStat != null){
            if(userStat.isEmpty())
                connections.send(connectionID, (T) new ACK(opCode)); // Empty return
            for(String logstat : userStat){
                connections.send(connectionID, (T) new ACK(opCode, logstat));
            }
        }
        else{
            connections.send(connectionID, (T) new ERROR(opCode));
        }
    }

    public String typeAsString(){
        return "STAT";
    }

    @Override
    public short getOpCode() {
        return 8;
    }
}
