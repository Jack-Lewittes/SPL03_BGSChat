package bgu.spl.net.srv;

import bgu.spl.net.srv.bidi.BidiMessagingProtocol;
import bgu.spl.net.srv.bidi.BidiMessagingProtocolimpl;
import bgu.spl.net.srv.msg.Filter;

public class ReactorMain {
    public static void main (String[] args){
        Server.reactor(
                Integer.parseInt(args[1]), //threads - submission
                Integer.parseInt(args[0]), //port - submission
                BidiMessagingProtocolimpl::new,
                BGSEncDec::new
        ).serve();
    }
}
