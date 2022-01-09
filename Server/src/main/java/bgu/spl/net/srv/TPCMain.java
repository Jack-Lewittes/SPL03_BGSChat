package bgu.spl.net.srv;

import bgu.spl.net.srv.bidi.BidiMessagingProtocolimpl;

public class TPCMain {
    public static void main(String[] args){

        Server.threadPerClient(
                Integer.parseInt(args[0]), //for submission
                BidiMessagingProtocolimpl::new,
                BGSEncDec::new
        ).serve();
    }
}
