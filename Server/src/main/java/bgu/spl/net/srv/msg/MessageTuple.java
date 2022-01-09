package bgu.spl.net.srv.msg;

import bgu.spl.net.srv.database.User;

import java.util.LinkedList;

public class MessageTuple {
    private User sender;
    private LinkedList<User> recipients;
    private NOTIFICATION.Type type;
    private String content;
    private String date;

    public MessageTuple(User _sender, LinkedList<User> _recipients, String _msg, String _date, NOTIFICATION.Type _type){
        this.sender = _sender;
        this.recipients = _recipients;
        this.content = _msg;
        this.date = _date;
        this.type = _type;
    }
}
