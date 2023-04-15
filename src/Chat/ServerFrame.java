package Chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The GUI class of the server window which also contains Server class and ClientThread class to run
 * the server of the chat.
 * @author Eli Eliyahu 312328016 Noam Gruber 312325384
 */
public class ServerFrame extends javax.swing.JFrame {

   private int serverPort = 18524; // Default port
    private Server myServer;

   
    public ServerFrame(int port) {
        this.serverPort = port;
                initComponents();
       
    }

    public ServerFrame() {
        setUndecorated(true); // Set decoration style to undecorated
        initComponents();
        setLocationRelativeTo(null); 
        setVisible(true); // Call setVisible after setting undecorated
       
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToolBar1 = new javax.swing.JToolBar();
        btnStart = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        txtLog = new javax.swing.JTextArea();
        portf = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();

        jToolBar1.setRollover(true);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(641, 331));
        setResizable(false);
        getContentPane().setLayout(null);

        btnStart.setText("Start");
        btnStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnStartActionPerformed(evt);
            }
        });
        getContentPane().add(btnStart);
        btnStart.setBounds(200, 270, 189, 52);

        txtLog.setColumns(20);
        txtLog.setRows(5);
        jScrollPane1.setViewportView(txtLog);

        getContentPane().add(jScrollPane1);
        jScrollPane1.setBounds(80, 100, 460, 149);

        portf.setText("1234");
        getContentPane().add(portf);
        portf.setBounds(260, 50, 81, 47);

        jLabel1.setFont(new java.awt.Font("Yu Gothic UI", 0, 24)); // NOI18N
        jLabel1.setText("PORT :");
        getContentPane().add(jLabel1);
        jLabel1.setBounds(110, 86, 185, 51);

        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icon/641_331.jpg"))); // NOI18N
        jLabel2.setText("jLabel2");
        getContentPane().add(jLabel2);
        jLabel2.setBounds(0, -30, 640, 410);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Calls the functions that handles starting and stopping the server.
     *
     * @param evt This parameter isn't used.
     */
    private void btnStartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnStartActionPerformed
                                     
    if (btnStart.getText().equals("Start")) {
        serverPort = Integer.parseInt(portf.getText()); // Get port from text field
        myServer = new Server();
        myServer.setPort(serverPort); // Set the server port
        new Thread(myServer).start();
        btnStart.setText("Stop");
    } else {
        btnStart.setText("Start");
        if (myServer != null) {
            myServer.stopServer();
        }
    }


    }//GEN-LAST:event_btnStartActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(ServerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(ServerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(ServerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(ServerFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new ServerFrame().setVisible(true);
            }
        });
    
}


    public class Server implements Runnable {

        //Hold the references to all the clients as threads
        private final ArrayList<ClientThread> connectedClients;
        //All the clients connect to this server socket
        private ServerSocket serverSocket;
        private int serverPort;

        /**
         * Initializing the list of the clients.
         */
        public Server() {
            connectedClients = new ArrayList<>();

        }


        public void stopServer() {
            txtLog.append("Closed server socket.\n");
            sendMessageAllClient("<Server disconnected>");
            closeAllClients();
            connectedClients.clear();
            try {
                serverSocket.close();
            } catch (IOException ex) {
                Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        private void closeAllClients() {
            synchronized (connectedClients) {
                for (ClientThread current : connectedClients) {
                    current.closeConnection();
                }
            }
        }
        public boolean isLegalName(String name) {
            synchronized (connectedClients) {
                if (name.equals("all")) {
                    return false;
                }
                for (ClientThread current : connectedClients) {
                    if (current.info.name.equals(name)) {
                        return false;
                    }
                }
                return true;
            }
        }
        public void sendMessageAllClient(String msg) {
            synchronized (connectedClients) {
                for (ClientThread current : connectedClients) {
                    current.out.println(msg);
                }
            }
        }
        public synchronized boolean sendPrivateMsg(String senderName, String recipientName, String msg) {
            if (senderName.equals(recipientName)) {
                return false;
            }
            ClientThread recipient = getClientThread(recipientName);
            if (recipient == null) {
                return false;
            }
            recipient.out.println("<Private message from: " + senderName + "> " + msg);
            return true;
        }
        private ClientThread getClientThread(String clientsName) {
            synchronized (connectedClients) {
                for (ClientThread current : connectedClients) {
                    if (current.info.name.equals(clientsName)) {
                        return current;
                    }
                }
                return null;
            }
        }
        @Override
        public void run() {
            try {
                //Start listening to connections at the specified port number
                serverSocket = new ServerSocket(serverPort);
                txtLog.append("Chat server is up and running and listening on port " + serverPort + ".\n");
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    //Some client connected. start a new thread only for this client
                    if (btnStart.getText().equals("Stop")) {
                        ClientThread clientThread = new ClientThread(clientSocket);
                        clientThread.start();
                    } else {
                        return;
                    }
                }
            } catch (IOException e) {
                if (btnStart.getText().equals("Stop")) {
                    txtLog.setText("Error has been occured while starting the server. \n\tError: " + e.toString() + "\n");
                    btnStart.setText("Start");
                }
            }
        }
        public boolean isLegalName(String name, ArrayList<ClientThread> connectedClients) {
            synchronized (connectedClients) {
                if (name.equals("all")) {
                    return false;
                }
                for (ClientThread current : connectedClients) {
                    if (current.info.name.equals(name)) {
                        return false;
                    }
                }
                return true;
            }
        }

        private void setPort(int port) {
            this.serverPort = port;
        }
        public class ClientThread extends Thread {
            /**
             * The socket which is the connection between the server and the client
             */
            public Socket curClient; 
            public PrintWriter out; //
            public BufferedReader in; 
            ClientInfoSeirialized info;
            public ClientThread(Socket curClient) {
                this.curClient = curClient;
                info = new ClientInfoSeirialized();
            }
             public ClientThread(Socket curClient, String name) {
                this.curClient = curClient;
                info = new ClientInfoSeirialized();
                info.name = name;
            }

            @Override
 
           public void run() {
    try {
        // Socket output to the client - for sending data through the socket to the client
        out = new PrintWriter(curClient.getOutputStream(), true);
        // Socket input from client - for reading client's data
        in = new BufferedReader(new InputStreamReader(curClient.getInputStream()));
        // Start listening to messages from the client

        // First message is only the name of the client
        String name = in.readLine();
        if (isLegalName(name)) {
            txtLog.append("Client " + name + " connected.\n");
            sendMessageAllClient("<Client " + name + " has entered>");
            out.println("Welcome, " + name);
            // Add to the list of all connected clients
            connectedClients.add(this);

            info.name = name;

            // Send or 'who's online' was clicked so now there is data in the InputStream
            String receivedFromClient;
            while ((receivedFromClient = in.readLine()) != null) {
                System.out.println("Received from client: " + receivedFromClient);
                info = parseReceivedData(receivedFromClient);
                // who's online was clicked
                if (info.showOnline) {
                    out.println(getConnectedClients());
                }
                // The user is sending a message to all the clients
                else if (info.recipient.equals("all")) {
                    System.out.println("gg");
                    sendMessageAllClient(info.name + ": " + info.msg);
                }
                // The user wants to send a private message
                else {
                    // Failed sending the private message
                    if (!sendPrivateMsg(info.name, info.recipient, info.msg)) {
                        out.println("<Couldn't send your message to " + info.recipient + ">");
                    } else { // Succeeded sending the private message
                        out.println("<Sent: " + info.msg + " Only to: " + info.recipient + ">");
                    }
                }
            }
            txtLog.append("Client " + info.name + " disconnected\n");
            sendMessageAllClient("<Client " + info.name + " disconnected>");
        } else {
            out.println("<Connection rejected because your name is 'all' or your name is already taken>");
        }
    } // Unexpectedly lost connection with the client
    catch (IOException e) {
        if (btnStart.getText().equals("Stop")) {
            txtLog.append("Lost connection with " + info.name + ".\n\tError: + " + e.toString() + "\n");
            sendMessageAllClient("<Client " + info.name + " has been disconnected>");
        }
    } finally {
        synchronized (connectedClients) {
            connectedClients.remove(this);
        }
        closeConnection();
    }
} 
            
            
            
            
            
            
            
            

            
            
            
            
            
  private ClientInfoSeirialized parseReceivedData(String receivedData) {
    ClientInfoSeirialized info = new ClientInfoSeirialized();
    String[] data = receivedData.split(",");
    info.name = data[0];
System.out.println(info.name);
    info.msg = data[1];
    System.out.println(info.name);
    info.recipient = data[2];
    System.out.println(info.name);
    info.showOnline = Boolean.parseBoolean(data[3]);
    System.out.println(info.name);
    return info;
}
            
            
            
            
            
            
            
            
            
            
            
            
            
            
      private String getConnectedClients() {
                synchronized (connectedClients) {
                    String allConnected = "";
                    allConnected = "<Now online:";
                    for (ClientThread current : connectedClients) {
                        allConnected += current.info.name + ", ";
                    }
                    return allConnected.substring(0, allConnected.length() - 2) + ">";
                }
            }
            public void closeConnection() {
                try {
                    if (out != null) {
                        out.close();
                    }
                    if (in != null) {
                        in.close();
                    }
                    if (curClient != null) {
                        curClient.close();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(ServerFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    

    
    
    
    
    
    

    // Setter method for port
    public void setPort(int port) {
        this.serverPort = port;
    }

    // Getter method for port
    public int getPort() {
        return serverPort;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnStart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JTextField portf;
    private javax.swing.JTextArea txtLog;
    // End of variables declaration//GEN-END:variables


}
