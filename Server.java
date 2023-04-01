package FB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static List<ClientHandler> clients = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(1234);
        System.out.println("Server started on port 1234");
        while (true) {
            Socket clientSocket = serverSocket.accept();
            System.out.println("New client connected");
            ClientHandler clientHandler = new ClientHandler(clientSocket);
            clients.add(clientHandler);
            clientHandler.start();
        }
    }

    private static class ClientHandler extends Thread {
        private String name;
        private String password;
        private BufferedReader in;
        private PrintWriter out;
        private Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                String credentials = in.readLine();
                String[] parts = credentials.split(" ");
                name = parts[0];
                password = parts[1];

                if (!authenticate(name, password)) {
                    out.println("Authentication failed. Closing connection.");
                    socket.close();
                    return;
                }

                else {
                    out.println("authenticated");
                }

                System.out.println(name + " joined the chat");

                out.println("Welcome to the chat, " + name + "!");

                synchronized (clients) {

                    if (clients.size() > 1) {
                        out.println("Currently connected clients:");
                        for (ClientHandler client : clients) {
                            if (client != this) {
                                out.println("- " + client.name);
                            }
                        }
                    }
                }

                broadcast(name + " joined the chat");

                String message;
                while ((message = in.readLine()) != null) {
                    if (message.equals("quit")) {
                        System.out.println(name + " left the chat");
                        clients.remove(this);
                        broadcast(name + " left the chat");
                        break;
                    } else if (message.startsWith("@")) {
                        String[] parts2 = message.split(" ", 2);
                        if (parts2.length == 2) {
                            String recipient = parts2[0];
                            String privateMessage = parts2[1];
                            sendPrivateMessage(recipient.substring(1), "[" + name + "]: " + privateMessage);
                        } else {
                            out.println("Invalid private message command. Use @[recipient] [message]");
                        }
                    } else {
                        broadcast("[" + name + "]: " + message);
                    }
                }
            } catch (SocketException e) {
                System.out.println("Server closed the connection.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcast(String message) {
            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }

        private void sendPrivateMessage(String recipient, String message) {
            for (ClientHandler client : clients) {
                if (client.name.equalsIgnoreCase(recipient)) {
                    client.out.println("[PRIVATE from " + name + "]: " + message);
                    break;
                }
            }
        }

        private boolean authenticate(String name, String password) {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            String url = "jdbc:mysql://localhost:3306/basedata";
            String usernamee = "root";
            String passwordd = "admin";

            try {

                Class.forName("com.mysql.cj.jdbc.Driver");

                conn = DriverManager.getConnection(url, usernamee, passwordd);

                String sql = "SELECT COUNT(*) FROM clientcon WHERE username2=? AND password2=?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, password);

                rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {

                    return true;
                } else {

                    return false;
                }
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    rs.close();
                } catch (Exception e) {
                    /* ignored */ }
                try {
                    stmt.close();
                } catch (Exception e) {
                    /* ignored */ }
                try {
                    conn.close();
                } catch (Exception e) {
                    /* ignored */ }
            }
        }

    }
}