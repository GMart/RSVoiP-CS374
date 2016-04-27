package main;

/*
 * Copyright (c) 2016.
 * By Garrett Martin (GMart on Github),
 *    Patrick Gephart (ManualSearch),
 *  & Matt Macke (BanishedAngel)
 * Class: main.Main
 * Last modified: 4/27/16 12:31 PM
 */

/**
 * Created by Garrett on 2/11/2016.
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

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
    static int port = 1200; // For chatting
    static int controlPort = 1199; // BuddyServer / proxy server control port
    static int audioPort = 1201;   // Audio send/receive port, may change when buddy server code is done
    static String Username; // User's own Name
    static String serverIP = "127.0.0.1";
    private static String userID;

    public static void main(String[] args) throws IOException {

        class serverThread implements Runnable {
            @Override
            public synchronized void run() {
                try {
                    server = new Server(port);
                    wait(100);
                } catch (Exception ex) {
                    System.out.println("Error in server, could not start!");
                }
            }
        }   // Different from ServerThread (That has one for each connection)

        //RtspDecoder rtspDecoder = new RtspDecoder(); // In the future use RTP?
        //RtspEncoder rtspEncoder = new RtspEncoder();
        // Start up server and client
        (new Thread(new serverThread())).start();

        client = new clientUIThread("serverIP");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                contentForm = new mainForm();
            }
        });

        System.out.println("GUI set up!");
        serverIP = JOptionPane.showInputDialog("Enter IP address to connect to:", serverIP);
        sendIPToServer();

        CallingStarter.audioRecvThread = new receiveAudioThread(new ServerSocket(audioPort));
        CallingStarter.audioRecvThread.start();     // Start listening for incoming calls

        //sendAudioThread(socket);         // Testing audio sending and receiving
    }

    /**
     * This method will connect the Buddy Server and update the IP address for the user.
     * This method will then request a port from the server and store the port sent back
     * in the variable audioPort.
     *
     * @throws IOException - If there is a problem with the socket or getting local IP address.
     */
    public static void sendIPToServer() throws IOException {
        Socket socket = null;
        OutputStreamWriter out;

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

        // Craft string to send to server
        String str;
        userID = JOptionPane.showInputDialog("What is your userID?");
        str = "1/" + userID + "/" + ip;
        String str2 = "5";

        // Update your IP on server
        try {
            socket = new Socket(serverIP, controlPort);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            out.write(str, 0, str.length());
            out.write(str2, 0, str2.length());
            audioPort = Integer.parseInt(in.readLine());
            out.close();
            in.close();
            System.out.println("IP sent to server");
        } catch (IOException e) {
            System.err.print(e);
        } finally {
            socket.close();
        }
    }

    /**
     * @param address The new IP address to send to.
     * @param portNum The port to change to.
     */
    public static void changeConnection(String address, int portNum) {
        port = portNum;

        //client = new clientUIThread(address);
        //TODO: Don't know why this breaks everything, should look into it!

    }

    public void sendPressed(String message) {
        client.processMessage(message);
        contentForm.clearChatText();
    }

    public static void addChatText(String chat) {
        contentForm.addChat(chat);
    }
}

class CallingStarter {    // Fires when "Call" button is pressed.
    private String name;
    private boolean endTheCall;
    static sendAudioThread audioSendThread = null; // The Thread used for sending audio.
    static receiveAudioThread audioRecvThread = null; // Static so this class doesn't have to manage the threads all the time

    /**
     * Receives the name and status of the call. Will start or end a call depending on endCall.
     *
     * @param user The username of the person currently selected
     * @param endCall Whether we should end the call instead of starting one.
     */
    CallingStarter(User user, boolean endCall) {
        name = user.toString();         // Grab the name of the user
        endTheCall = endCall;
    }

    /**
     * Runs when the ActionListener on the "Call" button is pressed.
     */
    synchronized void actionPerformed() {
        Socket audioSendSocket;

        if (!endTheCall) {
            //TODO: Initiate the call - Query and set up the correct socket, using test socket for now
            try {
                if (!audioRecvThread.isRunning()) { // First, reset RecvThread
                    audioRecvThread = new receiveAudioThread(new ServerSocket(Main.audioPort));  // New Receive thread
                    audioRecvThread.setMakingACall(true);
                    audioRecvThread.start();        // Start the receiving process
                }
                audioRecvThread.setMakingACall(true);   // We are making the call, so don't make another thread

                audioSendSocket = new Socket(Main.serverIP, Main.audioPort);    // Set up socket to actually send
                audioSendThread = new sendAudioThread(audioSendSocket);
                audioSendThread.start();// Start that Thread
                Main.contentForm.addChat("Calling user at " + audioSendThread.socket.getInetAddress().getHostAddress() + ":" + audioSendSocket.getPort());
            } catch (IOException e1) {
                System.out.println("Problem IP address or starting threads  ");
                e1.printStackTrace();
            }
        } else {
            // TODO: End the call
            if (audioSendThread != null && audioSendThread.isAlive()) {
                audioSendThread.setRunning(false);
            }
            if (audioRecvThread != null && audioRecvThread.isAlive()) {
                audioRecvThread.setRunning(false);
            }
        }
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
