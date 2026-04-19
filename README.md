# 💬 Distributed Chat Logger System

A distributed chat application built using Java TCP sockets, allowing multiple clients to communicate in real-time with centralized logging and command support.

## 📌 Overview
This system follows a client-server architecture where multiple clients connect to a central server to exchange messages. The server manages communication, processes commands, and logs all activities.

## 🚀 Features
- Multi-client real-time chat system
- Client-server architecture using TCP
- Multi-threading for handling concurrent users
- Command support:
  - /users → list active users
  - /log → show message count
  - /alert → broadcast alert messages
  - /quit → disconnect safely
- Centralized logging system (chatlog.txt)

## 🛠️ Technologies Used
- Java (OOP)
- TCP Sockets (ServerSocket, Socket)
- Multi-threading (Thread, Runnable)
- Java I/O (BufferedReader, PrintWriter, FileWriter)
- Collections & Synchronization

## ⚙️ How It Works
- The server listens for incoming connections.
- Each client connects using IP and port.
- A dedicated thread handles each client.
- Messages are broadcasted to all clients.
- Commands are processed by the server.

## 🎯 What I Learned
- Implementing client-server architecture
- Handling concurrency using multi-threading
- Designing communication protocols
- Debugging network and connection issues
- Building real-time distributed systems


## 📂 Project Files
- ChatServer.java
- ClientHandler.java
- ChatClient.java
- Project Report (PDF)

---
