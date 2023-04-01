package FB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter your username:");
        String username = scanner.nextLine();

        System.out.println("Enter your password:");
        String password = scanner.nextLine();

        Socket socket = new Socket("localhost", 1234);
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            out.println(username + " " + password);

            String authResponse = in.readLine();
            if (authResponse.equals("authenticated")) {
                System.out.println("You are now authenticated.");
            } else {
                System.out.println("Authentication failed. ");
                socket.close();
                return;
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String message;
                        while ((message = in.readLine()) != null) {
                            System.out.println(message);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            while (true) {
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase("quit")) {
                    out.println("quit");
                    System.out.println("Vous avez quiter lma conversation");
                    break;
                } else if (message.startsWith("@")) {
                    String[] parts = message.split(" ", 2);
                    if (parts.length == 2) {
                        String recipient = parts[0];
                        String privateMessage = parts[1];
                        out.println(recipient + " " + privateMessage);
                    } else {
                        System.out.println("Invalid private message command. Use @[recipient] [message]");
                    }
                } else {
                    out.println(message);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.close();
        }
    }
}
