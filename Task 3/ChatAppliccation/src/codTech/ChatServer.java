package codTech;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class ChatServer {

    private static final int PORT = 5000;

    private static Set<ClientHandler> clients =
            Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("Chat Server Started on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");

                ClientHandler clientHandler = new ClientHandler(socket);
                clients.add(clientHandler);

                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Broadcast to ALL clients (including sender)
    public static void broadcast(String message) {
        synchronized (clients) {
            for (ClientHandler client : clients) {
                client.sendMessage(message);
            }
        }
    }

    static class ClientHandler implements Runnable {

        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(
                        socket.getOutputStream(), true);

                out.println("Enter your username:");
                username = in.readLine();

                // Username validation
                if (username == null || username.trim().isEmpty()) {
                    username = "User" + new Random().nextInt(1000);
                }

                System.out.println(username + " joined.");
                broadcast(">>> " + username + " joined the chat.");

                String message;
                while ((message = in.readLine()) != null) {

                    // Exit condition
                    if (message.equalsIgnoreCase("exit")) {
                        break;
                    }

                    String time = new SimpleDateFormat("HH:mm:ss")
                            .format(new Date());

                    String formatted = "[" + time + "] " + username + ": " + message;

                    System.out.println(formatted);
                    broadcast(formatted);
                }

            } catch (IOException e) {
                System.out.println(username + " disconnected.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {}

                clients.remove(this);
                broadcast("<<< " + username + " left the chat.");
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }
    }
}