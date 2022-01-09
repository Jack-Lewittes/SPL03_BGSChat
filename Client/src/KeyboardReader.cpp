//
// Created by spl211 on 04/01/2022.
//

#include "../include/KeyboardReader.h"
#include <ctime>
#include <iomanip>

using namespace std;

KeyboardReader::KeyboardReader(ConnectionHandler &connectionHandler, mutex &mtxLock, condition_variable &conditionVariable, bool &terminated)
    : connectionHandler(connectionHandler), opcodeToNameMap(), mtxLock(mtxLock), conditionVariable(conditionVariable), terminated(terminated)
{
    opcodeToNameMap.insert(pair<string, short>("REGISTER", 1));
    opcodeToNameMap.insert(pair<string, short>("LOGIN", 2));
    opcodeToNameMap.insert(pair<string, short>("LOGOUT", 3));
    opcodeToNameMap.insert(pair<string, short>("FOLLOW", 4));
    opcodeToNameMap.insert(pair<string, short>("POST", 5));
    opcodeToNameMap.insert(pair<string, short>("PM", 6));
    opcodeToNameMap.insert(pair<string, short>("LOGSTAT", 7));
    opcodeToNameMap.insert(pair<string, short>("STAT", 8));
    opcodeToNameMap.insert(pair<string, short>("NOTIFICATION", 9));
    opcodeToNameMap.insert(pair<string, short>("ACK", 10));
    opcodeToNameMap.insert(pair<string, short>("ERROR", 11));
    opcodeToNameMap.insert(pair<string, short>("BLOCK", 12));
}

void KeyboardReader::operator()() {
    string line;
    while (!terminated) {
        const short bufsize = 1024;
        char buf[bufsize];
        std::cin.getline(buf, bufsize);
        std::string line(buf);
        vector <string> userInput;
        stringstream start(line);
        string word;
        while (getline(start, word, ' ')) {
            userInput.push_back(word);
        }
        char opCodeBytes[2];
        if (userInput.empty() || opcodeToNameMap.find(userInput.at(0)) == opcodeToNameMap.end()) {
            std::cout << "Error: Unknown Input" << std::endl;
            continue;
        }
        short opCode = opcodeToNameMap[userInput.at(0)];
        shortToBytes(opCode, opCodeBytes);
        int index = 0;
        if (opCode == 1) {
            if (userInput.size() != 4) {
                std::cout << "ERROR " << opCode << endl;
                continue;
            }
            string username = userInput.at(1);
            string passwordOrContent = userInput.at(2);
            string birthdayOrDate = userInput.at(3);
            const char *usernameAsBytes = username.c_str();
            const char *passwordOrContentAsBytes = passwordOrContent.c_str();
            const char *birthdayOrDateAsBytes = birthdayOrDate.c_str();
            char message[2 + username.length() + 1 + passwordOrContent.length() + 1 + birthdayOrDate.length() + 1];
            index = copyBytesToArray(message, opCodeBytes, index, 2);
            index = copyBytesToArray(message, usernameAsBytes, index, username.length());
            message[index++] = '\0';
            index = copyBytesToArray(message, passwordOrContentAsBytes, index, passwordOrContent.length());
            message[index++] = '\0';
            index = copyBytesToArray(message, birthdayOrDateAsBytes, index, birthdayOrDate.length());
            message[index++] = '\0';
            connectionHandler.sendBytes(message, index);
        }
        else if (opCode == 6){
            if (userInput.size() < 3){
                std::cout << "ERROR " << opCode << "WRONG INPUT" << endl;
                continue;
            }
            string content;
            for (unsigned int i = 2; i < userInput.size(); i++){
                content += userInput.at(i);
                if (i != (userInput.size()) - 1)
                    content += " ";
            }
            string username = userInput.at(1);

            //fetch local time
            auto t = std::time(nullptr);
            auto tm = *std::localtime(&t);
            char timeString[17] = {'\0'};
            std::strftime(timeString, 17, "%d-%m-%Y %H:%M", &tm);
            int sizeOfTime = 17;
            for (const auto &timeChar : timeString)
            {
                if (timeChar == '\0')
                    --sizeOfTime;
            }
            const char *usernameAsBytes = username.c_str();
            const char *contentAsBytes = content.c_str();
            const char *timeStringAsBytes = timeString;
            char message[2 + username.length() + 1 + content.length() + 1 + sizeOfTime + 1];
            index = copyBytesToArray(message, opCodeBytes, index, 2);
            index = copyBytesToArray(message, usernameAsBytes, index, username.length());
            message[index++] = '\0';
            index = copyBytesToArray(message, contentAsBytes, index, content.length());
            message[index++] = '\0';
            index = copyBytesToArray(message, timeStringAsBytes, index, sizeOfTime);
            message[index++] = '\0';
            connectionHandler.sendBytes(message, index);
        }
        else if (opCode == 2) {
            if (userInput.size() != 4) {
                std::cout << "ERROR " << opCode << endl;
                continue;
            }
            string username = userInput.at(1);
            string password = userInput.at(2);
            string captcha = userInput.at(3);
            const char *usernameAsBytes = username.c_str();
            const char *passwordAsBytes = password.c_str();
            const char *captchaAsBytes = captcha.c_str();
            char message[2 + username.length() + 1 + password.length() + 1 + captcha.length() + 1];
            index = copyBytesToArray(message, opCodeBytes, index, 2);
            index = copyBytesToArray(message, usernameAsBytes, index, username.length());
            message[index++] = '\0';
            index = copyBytesToArray(message, passwordAsBytes, index, password.length());
            message[index++] = '\0';
            index = copyBytesToArray(message, captchaAsBytes, index, captcha.length());
            message[index++] = ';';
            connectionHandler.sendBytes(message, index);
        } else if (opCode == 4) {
            if (userInput.size() != 3) {
                std::cout << "ERROR " << opCode << endl;
                continue;
            }
            string actionType = userInput.at(1);
            string username = userInput.at(2);
            const char *actionAsBytes = actionType.c_str();
            const char *usernameAsBytes = username.c_str();
            char message[2 + actionType.length() + username.length() + 1 + 1];
            index = copyBytesToArray(message, opCodeBytes, index, 2);
            index = copyBytesToArray(message, actionAsBytes, index, actionType.length());
            index = copyBytesToArray(message, usernameAsBytes, index, username.length());
            message[index++] = '\0';
            message[index++] = ';';

            connectionHandler.sendBytes(message, index);

        } else if (opCode == 3 || opCode == 7) {
            char message[2];
            index = copyBytesToArray(message, opCodeBytes, index, 2);
            connectionHandler.sendBytes(message, index);
            if (opCode == 3) {//LOGOUT
                unique_lock <mutex> krLock(mtxLock);
                //wait until socket reader retrieves reply -> determine if terminate
                conditionVariable.wait(krLock);
            }
        } else if (opCode == 5 || opCode == 8) {
            if (userInput.size() < 2) {
                std::cout << "ERROR " << opCode << endl;
                continue;
            }
            string content;
            for(unsigned int i = 1; i < userInput.size(); i++){
                content += userInput.at(i);
                if(i != (userInput.size()) - 1)
                    content += " ";
            }
            const char *contentAsBytes = content.c_str();
            char message[2 + content.length() + 1];
            index = copyBytesToArray(message, opCodeBytes, index, 2);
            index = copyBytesToArray(message, contentAsBytes, index, content.length());
            message[index++] = '\0';
            connectionHandler.sendBytes(message, index);

        } else if (opCode == 12) {
            if (userInput.size() != 2) {
                std::cout << "ERROR " << opCode << endl;
                continue;
            }
            string content = userInput.at(1);
            const char *contentAsBytes = content.c_str();
            char message[2 + content.length() + 1];
            index = copyBytesToArray(message, opCodeBytes, index, 2);
            index = copyBytesToArray(message, contentAsBytes, index, content.length());
            message[index++] = '\0';
            connectionHandler.sendBytes(message, index);
        }
    }
}
void KeyboardReader::shortToBytes(short num, char *bytesArr) {
    bytesArr[0] = ((num >> 8) & 0xFF);
    bytesArr[1] = (num & 0xFF);
}

int KeyboardReader::copyBytesToArray(char *destArray, const char *srcArray, int indexBegin, size_t numOfBytesToCopy) {
    for (size_t counter = 0; counter < numOfBytesToCopy; counter++) {
        destArray[indexBegin++] = srcArray[counter];
    }
    return indexBegin;
}
