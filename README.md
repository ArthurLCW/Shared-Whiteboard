# Shared Whiteboard
### 1. Brief Intro:
A shared whiteboard that allows multiple users to draw at the same time. 

### 2. Key Technologies:
Java, Socket, Thread

### 3. Key Functions:
1. Allow multiple people to draw (free draw, line, triangle, rectangle, circle, words) at the same time. 
2. Enable users to chat in the chat room. 
3. Administrator function. The user needs to have approval from the administrator to enter the shared whiteboard room. The administrator can kick out any user at ant time. 

### 4. Key Architecture Features:
1. This project uses a hybrid model that combines the client-server architecture and the P2P architecture. Client-server architecture is used when users get approval from the administrator. P2P architecture is used to send users' drawings to other users. Please notice that the server used in this project is 2c4g (2 cores, 4G RAM), which is weaker than most PCs. Therefore, we choose the P2P for drawing sharing. 
2. The TCP socket used in this project is request-based rather than connection-based. The request-based socket means that the sockets are created every time there is a request. For example, the user will build a socket to send a request to the server, and the socket is closed once the request is finished. The socket connection will not last long. We assume that the frequency of message passing, including user list updates, whiteboard synchronization, and other operations, is low. Users are idle most of the time. Under this presumption, it will be unwise to hold the socket connection to waste system resources
waiting for information.

### Structure of This Repo
1. Source code:
    In folder [/Shared-Whiteboard-main](https://github.com/ArthurLCW/Shared-Whiteboard/tree/main/Shared-Whiteboard-main)
    How to compile: mvn clean package
    How to execute: 
    1. java -jar target/server-jar-with-dependencies.jar
    2. java -jar target/client-jar-with-dependencies.jar $username 3200 $serverIP $userPort. e.g. java -jar target/client-jar-with-dependencies.jar admin 3200 localhost 3201
2. Jar files:
    In folder [JAR_files](https://github.com/ArthurLCW/Shared-Whiteboard/tree/main/JAR_files)
3. Report:
    DS [report2.pdf](https://github.com/ArthurLCW/Shared-Whiteboard/blob/main/DS%20report2.pdf)
