#Java Chat Application#

This Java Chat Application is a basic multi-client chat application that allows multiple clients to connect and chat in real-time. The chat application consists of two parts, a client-side and server-side. The client-side of the chat application is developed using Java, and the server-side is developed using Java and MySQL.

The application allows users to enter their username and password to authenticate and join the chat. Once authenticated, the user can send and receive messages to and from other connected clients. The application also supports private messaging, where a user can send a message to a specific user in the chat.

Prerequisites:

Java Development Kit (JDK) 8 or higher
MySQL

Getting Started:

1- Clone the project to your local machine using the following command:
   git clone https://github.com/<username>/java-chat-app.git

2- Import the project into your preferred IDE.

3- Create a MySQL database and execute the following SQL script to create the required tables:
   CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE,
    password VARCHAR(255)
  );

  CREATE TABLE messages (
   id INT PRIMARY KEY AUTO_INCREMENT,
   sender VARCHAR(255),
   receiver VARCHAR(255),
   message TEXT
  );
  
4- Modify the database connection details in the Server.java file to match your database configuration.

5- Run the Server.java file to start the chat server.

6- Run the Client.java file to connect to the chat server.

7- Enter your username and password when prompted to authenticate and start chatting.

Usage:

 -> Enter your username and password to authenticate and join the chat.

 -> To send a message to all connected clients, simply type your message and press enter.

 -> To send a private message to a specific user, type @[recipient] [message] and press enter. Replace [recipient] with the username of the user you want to send the message to, and [message] with the message you want to send.

 -> To quit the chat, type quit and press enter.
 
Contributing:

 If you would like to contribute to this project, please fork the repository and submit a pull request. 
 
