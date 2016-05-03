package main;

/*
 * Copyright (c) 2016.
 * By Garrett Martin (GMart on Github),
 *    Patrick Gephart (ManualSearch),
 *  & Matt Macke (BanishedAngel)
 * Class: main.Main
 * Last modified: 5/2/16 2:08 PM
 */

/**
 * Created by Garrett on 2/11/2016.
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

import static main.Main.serverIP;

/**
 * Where the program starts from.
 *
 * @author Garrett Martin
 * @author Matt Macke
 */
public class Main {
    static mainForm contentForm;
    static clientUIThread client;
    //static receiveAudioThread mainRecvThread = null;    // Listens for incoming calls
    private static Server server;
    static int port = 1200; // For chat function
    static int controlPort = 1199; // BuddyServer / proxy server control port
    static int audioPort = 1201;   // Audio send/receive port, may change when buddy server code is done
    static String Username; // User's own Name
    static String serverIP = "192.168.1.2";
    static boolean serverMode = false;
    private static String userID;

    public static void main(String[] args) {

        class serverThread implements Runnable {
            @Override
            public synchronized void run() {
                try {
                    server = new Server(port);
                } catch (Exception ex) {
                    System.out.println("Error in server, could not start!");
                }
            }
        }   // Different from ServerThread (That has one for each connection)

        //RtspDecoder rtspDecoder = new RtspDecoder(); // In the future use RTP?
        //RtspEncoder rtspEncoder = new RtspEncoder();
        // Start up server and client
        //(new Thread(new serverThread())).start();

        //client = new clientUIThread(serverIP);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                contentForm = new mainForm();
            }
        });

        System.out.println("GUI set up!");
        serverIP = JOptionPane.showInputDialog("Enter IP address to connect to:", serverIP);
        //sendIPToServer();

        try {
            CallingStarter.audioRecvThread = new receiveAudioThread(new ServerSocket(audioPort));
        } catch (IOException e) {
            e.printStackTrace();
        }
        CallingStarter.audioRecvThread.start();     // Start listening for incoming calls

        //sendAudioThread(socket);         // Testing audio sending and receiving
    }

    /**
     * This method will connect the Buddy Server and update the IP address for the user.
     * This method will then request a port from the server and try to store the port sent back
     * in the variable audioPort.
     *
     * @throws IOException - If there is a problem with the socket or getting local IP address.
     */
    static void sendIPToServer() throws IOException {
        OutputStreamWriter out;
        int i = 0;

        // Get public IP
        URL whatismyip;
        BufferedReader in;
        String ip;
        try {
            whatismyip = new URL("http://checkip.amazonaws.com");
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            ip = in.readLine();
            in.close();
        } catch (IOException e) {
            ip = InetAddress.getLocalHost().getHostAddress();
        }
        System.out.println("My IP: " + ip);

        // Craft string to send to server
        String str;
        userID = JOptionPane.showInputDialog("What is your userID?");
        str = "1/" + userID + "/" + ip + '\0';

        // Update your IP on server
        try (Socket socket = new Socket(serverIP, controlPort)) {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            out.write(str, 0, str.length());
            out.flush();

            out.close();
            in.close();
            System.out.println("IP sent to server.");
        } catch (IOException e) {
            System.err.print(e);
        }
    }

    /**
     * Sends the "5" command to the server, starting the proxy. Receives the port number to use,
     * which is stored in audioPort. Should only be called when in server mode.
     */
    static void startServerCall() {
        int i = 0;
        BufferedReader in;
        OutputStreamWriter out;
        String str2 = "5" + '\0';

        try (Socket socket = new Socket(serverIP, controlPort)) {
            sendIPToServer();       // Update IP on server
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            out.write(str2, 0, str2.length());
            out.flush();
            System.out.println("Getting port...");
            while (i < 20) {    // Try 20 times waiting for reply
                if (in.ready()) {
                    audioPort = Integer.parseInt(in.readLine());
                    break;      // Exit while loop
                }
                i++;
            }
            out.close();
            in.close();
            System.out.println("Got port: " + audioPort);
        } catch (IOException e) {
            System.err.print(e);
        }
    }

    /**
     * Resets the main receive Thread to the correct port (from the server)
     *
     * @param address The new IP address to send to.
     * @param portNum The port to change to.
     */
    static void changeConnection(String address, int portNum) {
        port = portNum;
        if (serverMode) {
            try {
                if (CallingStarter.audioRecvThread != null) {
                    CallingStarter.audioRecvThread.setRunning(false);   // This will close the serverSocket
                    Thread.sleep(200);                             // Wait for the sockets to close
                }
                //sendIPToServer();                                   // Update server just in case
                CallingStarter.audioRecvThread = new receiveAudioThread(new ServerSocket(audioPort));   // Make new one
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

        //client = new clientUIThread(address);     // For Chat system
    }

    public static void addChatText(String chat) {
        contentForm.addChat(chat);
    }
}

/**
 * Fires when "Call" button is pressed.
 */
class CallingStarter {
    private User user;
    private boolean endTheCall;
    static sendAudioThread audioSendThread = null;      // The Thread used for sending audio.
    static receiveAudioThread audioRecvThread = null;   // Static so all classes in this file can manage

    /**
     * Receives the name and status of the call. Will start or end a call depending on endCall.
     *
     * @param user The username of the person currently selected
     * @param endCall Whether we should end the call instead of starting one.
     */
    CallingStarter(User user, boolean endCall) {
        this.user = user;         // Grab the name of the user
        endTheCall = endCall;
    }

    /**
     * Runs when the ActionListener on the "Call" button is pressed.
     */
    synchronized void actionPerformed() {
        Socket audioSendSocket;
        if (!endTheCall) {
            try {
                if (audioRecvThread == null || !audioRecvThread.isRunning()) {  // First, reset RecvThread
                    audioRecvThread = new receiveAudioThread(new ServerSocket(Main.audioPort));  // New Receive thread
                    audioRecvThread.setMakingACall(true);
                    audioRecvThread.start();        // Start the receiving process
                }
                audioRecvThread.setMakingACall(true);   // We are making the call, so don't make another thread
                Main.contentForm.addChat("Calling " + user.name + "...");

                audioSendSocket = callSocket(Main.serverMode);
                if (audioSendSocket == null) throw new IOException("Person did not pick up.");
                audioSendThread = new sendAudioThread(audioSendSocket);
                audioSendThread.start();// Start that Thread

                //System.out.println("Server Mode: " + Main.serverMode + ", IP sending to: " + (Main.serverMode ? serverIP : user.address)
                //   + ":" + audioSendSocket.getPort());
                Main.contentForm.addChat("Call connected.");
            } catch (IOException e1) {
                System.out.println("Problem IP address or starting threads:\n" + e1.getMessage());
                Main.contentForm.addChat("Failed to connect: Person did not pick up.");
            }
        } else {
            if (audioSendThread != null && audioSendThread.isAlive()) {
                audioSendThread.setRunning(false);
            }
            if (audioRecvThread != null && audioRecvThread.isAlive()) {
                audioRecvThread.setRunning(false);
                try {       // Restart listening for calls on the receive port
                    audioRecvThread = new receiveAudioThread(new ServerSocket(Main.audioPort));
                } catch (IOException ignored) {
                }
            }
        }
    }

    /**
     * Set up socket to actually send, using serverIP if server or individual IP if not.
     *
     * @param withServer Whether to use the proxy server to connect through,
     * @return The Socket to use with the send Thread, or null if nobody picks up.
     */
    private Socket callSocket(boolean withServer) {
        int i = 0;
        Socket socket = new Socket();
        InetSocketAddress socketAddress;

        if (withServer) {
            socketAddress = new InetSocketAddress(Main.serverIP, Main.audioPort);
            Main.startServerCall(); // Get server connection here, but the call might fail so the server needs to deal with that.
        } else
            socketAddress = new InetSocketAddress(user.address, Main.audioPort);

        //Main.contentForm.addChat("Connecting to " + socketAddress.toString() + "...");
        while (i < 40) {
            try {
                i++;
                socket.connect(socketAddress, 1000);    // This way we set the timeout for the socket connection
                i = 100;      // If connecting succeeds, then don't loop
            } catch (SocketTimeoutException e) {
                Main.contentForm.addChat(".");  // "Ringing"
            } catch (IOException e) {
                e.printStackTrace();
                Main.contentForm.addChat(".");  // "Ringing"
            }
        }
        return socket.isConnected() ? socket : null;
    }
}

class clientUIThread implements Runnable, ActionListener {
    private DataOutputStream dout;
    private DataInputStream din;
    private Socket socket;

    clientUIThread(String address) {

        try {       // Initiate the connection
            InetAddress IPAddr = InetAddress.getByName(address.trim());

            socket = new Socket(IPAddr, Main.port);

            // Grab* the streams
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());

            // Start a background thread for receiving messages
            Thread runningThread = new Thread(this);
            runningThread.start();  // Separated in case we need to shutdown thread in the future
        } catch (IOException ie) {  // This will catch if the address is invalid
            System.out.println("Unusable IP address!\n" + ie);
        }
    }

    public void actionPerformed(ActionEvent e) {

    }

    /**
     * Gets called when the user types something
     *
     * @param message Message string to send
     */
    void processMessage(String message) {
        try {
            if (message.trim().isEmpty())
                return;         // Don't send nothing

            // Send it to the server
            dout.writeUTF(message);
            // Clear out text input field
            Main.contentForm.clearChatText();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            // Receive messages as long as it exists
            while (true) {
                // Get the message
                String message = din.readUTF();
                // Print it to the text window
                Main.contentForm.addChat(message);
            }
        } catch (IOException ie) {
            //System.out.println(ie);
        } catch (Exception ex) {
            System.out.println("Error in client!");
        }
    }
}
