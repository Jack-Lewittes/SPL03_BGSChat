package bgu.spl.net.srv.connections;

import java.util.HashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    //represents <ID of CH(per client), CH>
    private final HashMap<Integer, ConnectionHandler<T>> userConnectionsMap;
    private int handlerIdCounter;

    public ConnectionsImpl(){
        this.userConnectionsMap = new HashMap<>();
        handlerIdCounter = 0;
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if(userConnectionsMap.containsKey(connectionId)){
            userConnectionsMap.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(T msg) {
        for(Integer user: userConnectionsMap.keySet()){
            userConnectionsMap.get(user).send(msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        userConnectionsMap.remove(connectionId);
    }

    public int addConnectionHandler(ConnectionHandler connectionHandler){
        int currentID = handlerIdCounter;
        userConnectionsMap.put(currentID, connectionHandler);
        handlerIdCounter++;
        return currentID;
    }
}
