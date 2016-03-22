package main;

/*
 Copyright (c) 2016.
 By Garrett Martin (GMart on Github),
    Patrick Gephart (ManualSearch),
  & Matt Macke (BanishedAngel)
 Class: main.Main
 Last modified: 3/22/16 10:45 AM
 */

/**
 * Created by Garrett on 2/11/2016.
 */

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class Main {
    public static mainForm contentForm;
    static clientUIThread client;
    public static Server server;
    static int port = 8080;

    public static void main(String[] args) throws IOException {

        class serverThread implements Runnable {
            @Override
            public synchronized void run() {
                try {
                    server = new Server(port);
                    wait(622);
                    server.sendToAll("Hello");
                } catch (Exception ex) {
                    System.out.println("Error in server!");
                }
            }
        }

        //RtspDecoder rtspDecoder = new RtspDecoder();
        //RtspEncoder rtspEncoder = new RtspEncoder();
        // Start up server and client
        (new Thread(new serverThread())).start();

        client = new clientUIThread("localhost");

        synchronized (client) {
            javax.swing.SwingUtilities.invokeLater(() -> contentForm = new mainForm());
        }
        //socket = new Socket("localhost", port);

        System.out.println("GUI set up!");


    }

    public void changeConnection(String address, int portNum) {
        port = portNum;
        client.getClass();
        client = new clientUIThread(address);

    }

    public void sendPressed(String message) {
        client.processMessage(message);
        contentForm.clearChatText();
    }

    public static void addChatText(String chat) {
        contentForm.addChat(chat);
    }
}

class actionCall implements ActionListener {
    private String name;

    /**
     * @param user The username of the person currently selected
     */
    public actionCall(User user) {
        name = user.toString();
        InetAddress address = user.address;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        JOptionPane.showMessageDialog(main.mainForm.getFrames()[0], "Will call: " + name);
    }
}

class clientUIThread implements Runnable, ActionListener {
    private DataOutputStream dout;
    private DataInputStream din;
    private Socket socket;

    public clientUIThread(String address) {

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
            System.out.println("Unusable IP address!" + ie);
        }
    }

    public void actionPerformed(ActionEvent e) {

    }

    // Gets called when the user types something
    protected void processMessage(String message) {
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
            //clientUI.setVisible(true);
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
            }


        } catch (Exception ex) {
            System.out.println("Error in client!");
        }
    }
}