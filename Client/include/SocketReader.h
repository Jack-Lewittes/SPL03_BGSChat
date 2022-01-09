//
// Created by spl211 on 04/01/2022.
//

#ifndef BOOST_ECHO_CLIENT_SOCKETREADER_H
#define BOOST_ECHO_CLIENT_SOCKETREADER_H

#include "../include/connectionHandler.h"
#include <mutex>
#include <condition_variable>

class SocketReader {

private:
    ConnectionHandler& connectionHandler;
    std::condition_variable& conditionVariable;
    std::mutex& mutex;
    bool& terminated;
    short bytesToShort(char* bytesArr);
public:
    SocketReader(ConnectionHandler& ch, std::mutex& mtx, std::condition_variable& cv, bool& termionated );
    void operator()();

};


#endif //BOOST_ECHO_CLIENT_SOCKETREADER_H
