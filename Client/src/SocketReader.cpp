//
// Created by spl211 on 04/01/2022.
//

#include "../include/SocketReader.h"
using namespace std;

short SocketReader::bytesToShort(char *bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}

SocketReader::SocketReader(ConnectionHandler &ch, std::mutex &mtx, std::condition_variable &cv, bool &terminated)
    : connectionHandler(ch), conditionVariable(cv), mutex(mtx), terminated(terminated){}

void SocketReader::operator()(){
    while (!terminated){
        char opCodeBytes[2]; //ack , error, notification
        char msgOpBytes[2];  //ack, error
        short opCode = -1;
        short msgOpCode = -1;
        std::string line;
        std::string msgType;
        if (connectionHandler.getBytes(opCodeBytes, 2)){
            opCode = bytesToShort(opCodeBytes);
            if (opCode == 10){
                msgType = "ACK ";
                if (connectionHandler.getBytes(msgOpBytes, 2))
                    msgOpCode = bytesToShort(msgOpBytes);
                connectionHandler.getFrameAscii(line, ';'); //if no optional, then remains empty
                line = line.substr(0,line.length()-1); // remove ';'
                if (msgOpCode == 3){ //LOGOUT case
                    std::unique_lock<std::mutex> sockLock(mutex);
                    terminated = true;
                    conditionVariable.notify_one();
                }
                std::cout << msgType << msgOpCode <<" "<< line << std::endl;
            }
            else if (opCode == 11){
                msgType = "ERROR ";
                if (connectionHandler.getBytes(msgOpBytes, 2))
                    msgOpCode = bytesToShort(msgOpBytes);
                if (msgOpCode == 3)
                {
                    conditionVariable.notify_one(); // failed logout -> other thread should try again
                }
                std::cout << msgType << msgOpCode << std::endl;
            }
            else if (opCode == 9){
                string pType;
                string postingUser;
                msgType = "NOTIFICATION ";
                connectionHandler.getFrameAscii(line, ';');
                pType = line[0] == '0' ? "PM " : "Public ";
                int ind = line.find('0', 1);
                postingUser = line.substr(1, ind - 1); // find posting user
                line = line.substr(ind + 1);              // take whatever is left in line
                line = line.substr(0, line.length() - 2); // drop '0' delimiter at the end of content
                std::cout << msgType << pType << postingUser << " " << line << std::endl;
            }
        }
        else{ // socketReader thread killed if no connection
            terminated = true;
        }
    }
}
