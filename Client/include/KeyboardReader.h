//
// Created by spl211 on 04/01/2022.
//

#ifndef BOOST_ECHO_CLIENT_KEYBOARDREADER_H
#define BOOST_ECHO_CLIENT_KEYBOARDREADER_H
#include <mutex>
#include <condition_variable>
#include <map>
#include "../include/connectionHandler.h"
using namespace std;


class KeyboardReader {
private:
    ConnectionHandler& connectionHandler;
    std::map<std::string, short> opcodeToNameMap;
    std::mutex& mtxLock;
    std::condition_variable& conditionVariable;
    bool& terminated;
    void shortToBytes(short num, char* bytesArr);
    int copyBytesToArray(char *destArray, const char *srcArray, int indexBegin, size_t numOfBytesToCopy);

public:
    void operator()();
    KeyboardReader(ConnectionHandler& connectionHandler, mutex& mtxLock, condition_variable& conditionVariable, bool& terminated);
    //TODO: ANER: Rule of 5 as default?

};


#endif //BOOST_ECHO_CLIENT_KEYBOARDREADER_H
