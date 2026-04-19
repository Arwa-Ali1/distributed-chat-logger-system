package cpcs371.chatlogger;
/*
CPCS371
EAR
Group 2
Jood Faisal Al-Zahrani (2307623), Rahaf Rajab Al-Zahrani (2307168), Arwa Ali Al-Qahtani (2306986), 
Bayan Ahmed Hatari (2331252), Wesal Majed Aljohani(2307116) 
*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/*
 ClientHandler
 -------------
 Handles communication with a single client in its own thread.
 Reads commands/messages from the client, processes them, and
 interacts with the ChatServer to broadcast and log events.
 */
public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ChatServer server;

    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;
        try {
            in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error creating client handler I/O: " + e.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }

    /**
     Sends a single line of text to this client.
     * @param msg
     */
    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    /**
     Closes the socket and related streams.
     * @throws java.io.IOException
     */
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
        if (out != null) {
            out.close();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    @Override
    public void run() {
        try {
            // Ask client for username
            send("Enter your username:");
            username = in.readLine();
            if (username == null || username.trim().isEmpty()) {
                // Fallback if client didn't send a proper name
                username = "User-" + socket.getPort();
            }

            server.log("Client connected: " + socket.getInetAddress()
                    + " (User: " + username + ")");
            server.broadcast("** " + username + " joined the chat **");

            String line;
            while ((line = in.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                handleInput(line);
            }
        } catch (IOException e) {
            System.err.println("Connection lost with "
                    + socket.getInetAddress() + ": " + e.getMessage());
        } finally {
            cleanup();
        }
    }

    /**
     Handles a single line of input from the client:
      /users
      /log
      /alert <message>
      /quit
      normal chat message
     */
    private void handleInput(String msg) {
        try {
            if (msg.equalsIgnoreCase("/users")) {

                String users = server.getUserList();
                send(users);
                server.log("Command /users requested by " + socket.getInetAddress());

            } else if (msg.equalsIgnoreCase("/log")) {

                int count = server.getMessageCount();
                send("Total messages logged: " + count);
                server.log("Command /log requested by " + socket.getInetAddress());

            } else if (msg.equalsIgnoreCase("/alert")) {
                // User typed /alert with no message
                send("Usage: /alert <message>");

            } else if (msg.startsWith("/alert ")) {

                String alertMsg = msg.substring(7).trim();
                if (alertMsg.isEmpty()) {
                    send("Usage: /alert <message>");
                    return;
                }

                String formatted = "ALERT: " + alertMsg;
                // Alerts go to EVERYONE including sender
                server.broadcast(formatted);
                server.incrementMessageCount();
                server.log("Alert broadcasted: '" + alertMsg + "'");

            } else if (msg.equalsIgnoreCase("/quit")) {

                send("Goodbye!");
                close(); // this will cause readLine() to return null and exit run()

            } else {
                // Normal text message
                String formatted = username + ": " + msg;

                // Send to everyone EXCEPT the sender, so sender doesn't see a duplicate
                server.broadcastExcept(this, formatted);

                // Optional: if you want the sender to see a formatted version, you can:
                // send("You: " + msg);

                server.incrementMessageCount();
                server.log("Message received from " + username + ": " + msg);
            }
        } catch (IOException e) {
            System.err.println("Error handling input from " + username + ": " + e.getMessage());
        }
    }

    /**
     * Cleans up resources and informs the server when the client disconnects.
     */
    private void cleanup() {
        try {
            server.removeClient(this);
            if (username != null) {
                server.broadcast("** " + username + " left the chat **");
                server.log("Client disconnected: " + socket.getInetAddress()
                        + " (User: " + username + ")");
            }
            close();
        } catch (IOException e) {
            System.err.println("Error during client cleanup: " + e.getMessage());
        }
    }
}