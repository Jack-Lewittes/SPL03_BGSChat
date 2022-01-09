package bgu.spl.net.srv.msg;

import bgu.spl.net.srv.connections.Connections;
import bgu.spl.net.srv.database.BGSDatabase;
import java.text.SimpleDateFormat;

public class PM implements Message {
    private final short opCode;
    private String date;
    private String userToReceive;
    private String content;
    private Filter filter;
    BGSDatabase database = BGSDatabase.getInstance();
    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    public PM(String _userToReceive, String _content, String _date) {
        this.filter = new Filter();
        opCode = 6;
        this.userToReceive = _userToReceive;
        this.content = filterContent(_content);
        this.date=_date;
    }

    private String filterContent(String content){
        String output = "";
        String[] splitted = content.split("\\s+");
        for(int i =0; i < splitted.length; i++){
            if(filter.shouldBeFiltered(splitted[i]))
                splitted[i]= "<filtered>";
        }
        for(int i =0; i < splitted.length; i++){
            if(i == 0)
                output = splitted[i];
            else{
                output = output.concat(" ").concat(splitted[i]);
            }
        }
        return output;
    }

    public String typeAsString(){
        return "PM";
    }

    @Override
    public <T> void process(int connectionID, Connections<T> connections) {
        boolean attemptPM = database.sendPM(connectionID, connections, content, date, userToReceive);
        if(attemptPM)
            connections.send(connectionID, (T) new ACK(opCode));
        else{
            connections.send(connectionID, (T) new ERROR(opCode));
        }
    }

    @Override
    public short getOpCode() {
        return 6;
    }
}
