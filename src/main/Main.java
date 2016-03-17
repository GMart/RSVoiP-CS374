package main;
/**
 * Created by Garrett on 2/11/2016.
 */


import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Main {
    public static mainForm contentForm;
    public static clientUIThread client;
    public static Server server;
    static int port = 8080;

    public static void main(String[] args) throws IOException {
        //Socket socket;
        class serverThread implements Runnable {
            @Override
            public synchronized void run() {
                try {
                    server = new Server(port);

                    server.sendToAll("Hello");
                } catch (Exception ex) {
                    System.out.println("Error in server!");
                }
            }
        }

        System.out.println("Test");
        //RtspDecoder rtspDecoder = new RtspDecoder();
        //RtspEncoder rtspEncoder = new RtspEncoder();

        (new Thread(new serverThread())).start();
        //ServerThread sThread = new ServerThread(server, (new Socket("localhost", port)));
        //servert = new ServerThread(server, (new Socket("localhost", port)));
        client = new clientUIThread();
        synchronized (client) {
            contentForm = new main.mainForm();
        }
        server.sendToAll("Hello");
        //socket = new Socket("localhost", port);

        System.out.println("GUI set up!");

        // Start up server and client

        //SwingUtilities.invokeLater(new serverThread());

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
     * @param addr The IP address of the person
     */
    public actionCall(String user, String addr) {
        name = user.trim();
        String address = addr;
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

    public clientUIThread() {

        try {
            // Initiate the connection
            socket = new Socket("localhost", Main.port);

            // Gram the streams
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());

            // Start a background thread for receiving messages
            new Thread(this).start();
        } catch (IOException ie) {
            System.out.println(ie);
        }
    }

    public void actionPerformed(ActionEvent e) {

    }

    // Gets called when the user types something
    protected void processMessage(String message) {
        try {
            if (message != "") {
                // Send it to the server
                dout.writeUTF(message);
                // Clear out text input field
                Main.contentForm.clearChatText();
            }

        } catch (IOException ie) {
            System.out.println(ie);
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