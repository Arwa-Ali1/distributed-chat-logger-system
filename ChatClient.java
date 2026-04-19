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
import java.util.Scanner;

/*
 ChatClient
 ----------
 Connects to the ChatServer via TCP, sends user input, and
 displays all messages received from the server.
 */
public class ChatClient {

    /*
     Main entry point.
     You can either:
      - Run with no arguments and enter host/port interactively, OR
      - Run with arguments: java cpcs371.chatlogger.ChatClient
     @param args
     */
    public static void main(String[] args) {
        String host = "127.0.0.1"; // default host
        int port = 5000;           // default port

        Scanner consoleScanner = new Scanner(System.in);

        if (args.length >= 1) {
            host = args[0];
        }
        if (args.length >= 2) {
            try {
                port = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port in arguments, using default 5000");
                port = 5000;
            }
        }

        // If no args given, ask interactively (nice for presentations)
        if (args.length == 0) {
            System.out.print("Enter server IP [" + host + "]: ");
            String inputHost = consoleScanner.nextLine().trim();
            if (!inputHost.isEmpty()) {
                host = inputHost;
            }

            System.out.print("Enter server port [" + port + "]: ");
            String inputPort = consoleScanner.nextLine().trim();
            if (!inputPort.isEmpty()) {
                try {
                    port = Integer.parseInt(inputPort);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid port, using default " + port);
                }
            }
        }

        ChatClient client = new ChatClient();
        client.start(host, port);
    }

    /*
      Starts the client: connects to the server, starts a listener thread
      for server messages, and reads user input from the console.
      @param host
      @param port
     */
    public void start(String host, int port) {
        try {
            try (Socket socket = new Socket(host, port)) {
                System.out.println("Connected to chat server at " + host + ":" + port);
                
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                
                // Thread to read messages from the server
                Thread readerThread = new Thread(() -> {
                    try {
                        String line;
                        while ((line = in.readLine()) != null) {
                            System.out.println(line);
                        }
                        System.out.println("Server has closed the connection.");
                        System.exit(0);
                    } catch (IOException e) {
                        System.out.println("Connection closed.");
                        System.exit(0);
                    }
                });
                readerThread.setDaemon(true);
                readerThread.start();
                
                // Main thread: read user input and send to server
                Scanner scanner = new Scanner(System.in);
                System.out.println("Type your messages.");
                System.out.println("Commands: /users, /log, /alert <message>, /quit to exit.");
                
                while (true) {
                    if (!scanner.hasNextLine()) {
                        break;
                    }
                    String msg = scanner.nextLine();
                    out.println(msg);
                    
                    if ("/quit".equalsIgnoreCase(msg.trim())) {
                        break;
                    }
                }
            }
            System.out.println("Disconnected from server.");
        } catch (IOException e) {
            System.err.println("Could not connect to server at "
                    + host + ":" + port + " - " + e.getMessage());
        }
    }
}