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
import java.sql.Timestamp;
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

                List<String> friends = retrieveFriends(name);
                if (!friends.isEmpty()) {
                    out.println("Your friends:");
                    for (String friend : friends) {
                        out.println("- " + friend);
                    }
                }

                List<String> oldMessages = retrieveMessages(name);
                if (!oldMessages.isEmpty()) {
                    out.println("Old messages:");
                    for (String message : oldMessages) {
                        out.println(message);
                    }
                }

                broadcast(name + " joined the chat");

                String message;
                String privateMessage = "";
                while ((message = in.readLine()) != null) {
                    if (message.equals("quit")) {
                        System.out.println(name + " left the chat");
                        clients.remove(this);
                        broadcast(name + " left the chat");
                        break;
                    } else if (message.startsWith("@")) {
                        ArrayList<String> myList = new ArrayList<String>();
                        String[] parts2 = message.split(" ");

                        for (int i = 0; i < parts2.length; i++) {
                            if (parts2[i].startsWith("@")) {
                                myList.add(parts2[i].substring(1));
                            } else {
                                privateMessage = privateMessage + " " + parts2[i];

                            }
                        }
                        for (String recipient : myList) {
                            sendPrivateMessage(recipient, privateMessage);
                        }
                    }

                    else if (message.startsWith("/add")) {
                        String[] parts2 = message.split(" ", 2);
                        if (parts2.length == 2) {
                            String friendname = parts2[1];

                            saveFriendRequest(name, friendname);

                        } else {
                            out.println("Invalid private message command. Use /add  name");
                        }
                    }

                    else {
                        broadcast("[" + name + "]: " + message);
                        saveMessage(name, null, message, "broadcast");
                    }
                }
            } catch (SocketException e) {
                System.out.println("Server closed the connection.");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void broadcast(String message) {

            saveMessage(name, null, message, "broadcast");

            for (ClientHandler client : clients) {
                client.out.println(message);
            }
        }

        private void sendPrivateMessage(String recipient, String message) {

            saveMessage(name, recipient, message, "private");

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

                String sql = "SELECT COUNT(*) FROM clientcon WHERE username2=?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                rs = stmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {

                    sql = "SELECT COUNT(*) FROM clientcon WHERE username2=? AND password2=?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, name);
                    stmt.setString(2, password);
                    rs = stmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {

                        return true;
                    } else {

                        return false;
                    }
                } else {

                    sql = "INSERT INTO clientcon (username2, password2) VALUES (?, ?)";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, name);
                    stmt.setString(2, password);
                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {

                        return true;
                    } else {

                        return false;
                    }
                }
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    rs.close();
                } catch (Exception e) {
                }
                try {
                    stmt.close();
                } catch (Exception e) {
                }
                try {
                    conn.close();
                } catch (Exception e) {
                }
            }
        }

        public static void saveMessage(String sender, String recipient, String messageText, String messageType) {
            Connection conn = null;
            PreparedStatement stmt = null;
            String url = "jdbc:mysql://localhost:3306/basedata";
            String username = "root";
            String password = "admin";
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(url, username, password);
                String sql = "INSERT INTO messages (sender, recipient, message_text, message_type, sent_at) VALUES (?, ?, ?, ?, NOW())";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, sender);
                stmt.setString(2, recipient);
                stmt.setString(3, messageText);
                stmt.setString(4, messageType);
                stmt.executeUpdate();
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    stmt.close();
                } catch (Exception e) {
                }
                try {
                    conn.close();
                } catch (Exception e) {
                }
            }
        }

        public static List<String> retrieveMessages(String name) {
            List<String> messages = new ArrayList<>();
            Connection conn = null;
            PreparedStatement stmt = null;
            PreparedStatement stmt1 = null;
            ResultSet rs = null;
            ResultSet rs1 = null;
            String url = "jdbc:mysql://localhost:3306/basedata";
            String username = "root";
            String password = "admin";
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(url, username, password);
                String sql = "SELECT * FROM messages WHERE  message_type = 'private' and (sender=? or recipient=?) ORDER BY sent_at ASC";
                String sql2 = "SELECT * FROM messages WHERE message_type = 'broadcast' ORDER BY sent_at ASC";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                stmt.setString(2, name);
                stmt1 = conn.prepareStatement(sql2);

                rs = stmt.executeQuery();
                rs1 = stmt1.executeQuery();
                while (rs.next()) {
                    String sender = rs.getString("sender");
                    String messageText = rs.getString("message_text");
                    String receive = rs.getString("recipient");
                    Timestamp sentAt = rs.getTimestamp("sent_at");
                    if (messageText.contains("[") && messageText.contains("]")) {
                        messages.add("[Pivate] from " + sender + " to " + receive + "(" + sentAt.toString() + "): "
                                + messageText);
                    }
                }
                while (rs1.next()) {
                    String sender = rs1.getString("sender");
                    String messageText = rs1.getString("message_text");
                    String type = rs1.getString("message_type");
                    Timestamp sentAt = rs1.getTimestamp("sent_at");
                    if (type.equals("broadcast")) {
                        if (!messageText.contains("[") || !messageText.contains("]")) {
                            messages.add("[all] " + sender + " (" + sentAt.toString() + "): " + messageText);
                        }
                    }
                }
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    rs.close();
                } catch (Exception e) {
                }
                try {
                    stmt.close();
                } catch (Exception e) {
                }
                try {
                    conn.close();
                } catch (Exception e) {
                }
            }
            return messages;
        }

        private void saveFriendRequest(String clientName, String friendName) {
            Connection conn = null;
            PreparedStatement stmt = null;
            String url = "jdbc:mysql://localhost:3306/basedata";
            String usernamee = "root";
            String passwordd = "admin";

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(url, usernamee, passwordd);

                String sql = "INSERT INTO friends (clientName, friendName) VALUES (?, ?)";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, clientName);
                stmt.setString(2, friendName);
                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    out.println(friendName + "  adde a friends list");
                } else {
                    out.println("Failed to send friend request to " + friendName);
                }
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    stmt.close();
                } catch (Exception e) {
                }
                try {
                    conn.close();
                } catch (Exception e) {
                }
            }
        }

        private List<String> retrieveFriends(String name) {
            List<String> friends = new ArrayList<>();
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            String url = "jdbc:mysql://localhost:3306/basedata";
            String usernamee = "root";
            String passwordd = "admin";

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conn = DriverManager.getConnection(url, usernamee, passwordd);

                String sql = "SELECT friendName FROM friends WHERE clientName = ?";
                stmt = conn.prepareStatement(sql);
                stmt.setString(1, name);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    friends.add(rs.getString("friendname"));
                }
            } catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    rs.close();
                } catch (Exception e) {
                }
                try {
                    stmt.close();
                } catch (Exception e) {
                }
                try {
                    conn.close();
                } catch (Exception e) {
                }
            }

            return friends;
        }

    }
}