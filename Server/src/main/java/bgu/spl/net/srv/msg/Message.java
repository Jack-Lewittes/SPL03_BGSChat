package bgu.spl.net.srv.msg;

import bgu.spl.net.srv.connections.Connections;

public interface Message  {

     <T> void process(int connectionID, Connections<T> connections);

     short getOpCode();

}

