package bgu.spl.net.srv.msg;

import bgu.spl.net.srv.connections.Connections;
import bgu.spl.net.srv.database.BGSDatabase;
import bgu.spl.net.srv.database.User;

import java.util.LinkedList;
import java.util.List;

public class POST implements Message {
    short opCode;
    String content;
    BGSDatabase database;


    public POST(String content) {
        opCode = 5;
        this.content = content;
        database = BGSDatabase.getInstance();
    }

    public <T> void process(int connectionsID, Connections<T> connections){
        List<String> sendToUsers = findTaggedUsers(content, connectionsID);
        boolean attemptPost = database.post(connectionsID, connections,  sendToUsers, content);
        if(attemptPost)
            connections.send(connectionsID, (T) new ACK(opCode));
        else{
            connections.send(connectionsID, (T) new ERROR(opCode));
        }
    }

    private <T> List<String> findTaggedUsers(String msg, int connectionsID){
        List<String> output = new LinkedList<>();
        int indexOfa = msg.indexOf('@');
        while (indexOfa != -1){
            int indexOfSpace = msg.indexOf(" ", indexOfa);
            String atUsername = indexOfSpace == -1 ? msg.substring(indexOfa+1) : msg.substring(indexOfa+1, indexOfSpace);
            //check if @username is registered
            if(database.getUserMap().containsKey(atUsername)){
                //check if @username blocked sender (connID)
                User sender = database.getUserMapByConn().get(connectionsID);
                User receiver = database.getUserMap().get(atUsername);
                if(!database.checkBlock(sender, receiver))
                    output.add(atUsername);
            }
            if(indexOfSpace !=-1)
                indexOfa = msg.indexOf("@", indexOfSpace); //more tagged users to find
            else{//if no space, then at end of message and terminate while loop
                indexOfa = -1;
            }
        }
        //cut message
        int index = msg.indexOf('@');
        while(index !=-1){
            int indexOfSpace = msg.indexOf(" ", index);
            if(indexOfSpace != -1)
                msg = msg.substring(0, index) + msg.substring(indexOfSpace);
            else{
                msg = msg.substring(0,index);
            }
            index=msg.indexOf('@');
        }
        return output;
    }

    public String typeAsString(){
        return "POST";
    }

    @Override
    public short getOpCode() {
        return 5;
    }
}
