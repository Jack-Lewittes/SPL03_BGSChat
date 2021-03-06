package bgu.spl.net.impl.newsfeed;

import bgu.spl.net.impl.rci.ObjectEncoderDecoder;
import bgu.spl.net.impl.rci.RemoteCommandInvocationProtocol;
import bgu.spl.net.srv.BGSEncDec;
import bgu.spl.net.srv.Server;
import bgu.spl.net.srv.bidi.BidiMessagingProtocol;
import bgu.spl.net.srv.bidi.BidiMessagingProtocolimpl;
import bgu.spl.net.srv.msg.Message;

public class NewsFeedServerMain {

    public static void main(String[] args) {
        NewsFeed feed = new NewsFeed(); //one shared object

// you can use any server... 
//        Server.threadPerClient(
//                7777, //port
//                () -> new RemoteCommandInvocationProtocol<>(feed), //protocol factory
//                ObjectEncoderDecoder::new //message encoder decoder factory
//        ).serve();

        Server.reactor(
                Runtime.getRuntime().availableProcessors(),
                7777, //port
                BidiMessagingProtocolimpl::new, //protocol factory
                BGSEncDec::new //message encoder decoder factory
        ).serve();

    }
}
