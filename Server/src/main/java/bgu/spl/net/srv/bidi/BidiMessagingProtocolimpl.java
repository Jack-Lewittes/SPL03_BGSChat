package bgu.spl.net.srv.bidi;
import bgu.spl.net.srv.connections.Connections;
import bgu.spl.net.srv.connections.ConnectionsImpl;
import bgu.spl.net.srv.msg.Message;

public class BidiMessagingProtocolimpl<T> implements BidiMessagingProtocol<T> {
    Connections<T> connections= new ConnectionsImpl<>();
    int connectionId;

    public BidiMessagingProtocolimpl(){}

    @Override
    public void start(int connectionId, Connections<T> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
    }

    @Override
    public void process(T message) {
        bgu.spl.net.srv.msg.Message msg = (bgu.spl.net.srv.msg.Message)message;
        msg.process(connectionId, connections);
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }
}
