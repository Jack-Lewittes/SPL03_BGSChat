package bgu.spl.net.srv.connections;

import bgu.spl.net.srv.msg.Message;

import java.io.IOException;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void broadcast(T msg);

    void disconnect(int connectionId);
}
