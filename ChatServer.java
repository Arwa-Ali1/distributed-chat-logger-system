package cpcs371.chatlogger;
/*
CPCS371
EAR
Group 2
Jood Faisal Al-Zahrani (2307623), Rahaf Rajab Al-Zahrani (2307168), Arwa Ali Al-Qahtani (2306986), 
Bayan Ahmed Hatari (2331252), Wesal Majed Aljohani(2307116) 
*/

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 ChatServer
 ----------
 Listens for incoming TCP client connections, manages connected users,
 broadcasts messages and alerts, and logs all activities to a file.
 */
public class ChatServer {

    private final int port;
    private ServerSocket serverSocket;

    // Thread-safe list of connected clients
    private final List<ClientHandler> clients =
            Collections.synchronizedList(new ArrayList<>());

    // Writer for the log file
    private PrintWriter logWriter;

    // Number of messages (including alerts) logged
    private int messageCount = 0;

    // Formatter for timestamps in the log file
    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ChatServer(int port) {
        this.port = port;
    }

    /*
     Starts the chat server: opens the server socket, log file,
     and begins accepting client connections.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            logWriter = new PrintWriter(new FileWriter("chatlog.txt", true), true);

            log("Server started and listening on port " + port);
            System.out.println("ChatServer started on port " + port);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket, this);
                clients.add(handler);
                Thread t = new Thread(handler);
                t.start();
            }
        } catch (IOException e) {
            System.err.println("Error in ChatServer: " + e.getMessage());
        } finally {
            shutdown();
        }
    }

    /*
     Gracefully shuts down the server and all client connections.
     */
    public void shutdown() {
        try {
            if (logWriter != null) {
                log("Server shutting down.");
                logWriter.close();
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }

            synchronized (clients) {
                for (ClientHandler c : clients) {
                    c.close();
                }
                clients.clear();
            }
        } catch (IOException e) {
            System.err.println("Error during shutdown: " + e.getMessage());
        }
    }

    /*
      Broadcasts a message to ALL connected clients (including the sender).
      Used for system messages such as join/leave and alerts.
      @param message
     */
    public void broadcast(String message) {
        System.out.println(message);
        synchronized (clients) {
            for (ClientHandler c : clients) {
                c.send(message);
            }
        }
    }

    /*
     Broadcasts a message to all clients EXCEPT the given one.
     Used for normal chat messages so the sender does not see a duplicate.
     */
    public void broadcastExcept(ClientHandler exclude, String message) {
        System.out.println(message);
        synchronized (clients) {
            for (ClientHandler c : clients) {
                if (c != exclude) {
                    c.send(message);
                }
            }
        }
    }

    /*
      Removes a client from the server's list when it disconnects.
     */
    public void removeClient(ClientHandler handler) {
        clients.remove(handler);
    }

    /*
     Increments the logged message counter.
     */
    public synchronized void incrementMessageCount() {
        messageCount++;
    }

    /*
      Returns the current number of logged messages.
     */
    public synchronized int getMessageCount() {
        return messageCount;
    }

    /*
      Builds a string listing all connected users.
     */
    public String getUserList() {
        StringBuilder sb = new StringBuilder();
        sb.append("Connected users: ");

        synchronized (clients) {
            if (clients.isEmpty()) {
                sb.append("(none)");
            } else {
                for (int i = 0; i < clients.size(); i++) {
                    ClientHandler c = clients.get(i);
                    if (c.getUsername() != null) {
                        sb.append(c.getUsername());
                    } else {
                        sb.append("Unknown");
                    }
                    if (i < clients.size() - 1) {
                        sb.append(", ");
                    }
                }
            }
        }
        return sb.toString();
    }

    /*
      Writes a timestamped entry to the log file and prints it on the server console.
     */
    public synchronized void log(String text) {
        String timestamp = LocalDateTime.now().format(timeFormatter);
        String entry = "[" + timestamp + "] " + text;
        System.out.println(entry);
        if (logWriter != null) {
            logWriter.println(entry);
        }
    }

    /*
      Main entry point.
      Usage: java cpcs371.chatlogger.ChatServer [port]
     */
    public static void main(String[] args) {
        int port = 5000;
        if (args.length >= 1) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port, using default 5000");
            }
        }

        ChatServer server = new ChatServer(port);
        server.start();
    }
}