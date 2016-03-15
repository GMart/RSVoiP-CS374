package main;
/**
 * Created by Garrett on 2/11/2016.
 */

//import java.awt.event.*;

import io.netty.handler.codec.rtsp.RtspDecoder;
import io.netty.handler.codec.rtsp.RtspEncoder;
import io.netty.handler.codec.rtsp.RtspHeaders;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.awt.event.ActionListener;
import java.net.Socket;

public class Main {
    public static mainForm contentForm;

    public static void main(String[] args) throws IOException {
        int port = 8080;
        System.out.println("Test");
        //RtspDecoder rtspDecoder = new RtspDecoder();
        //RtspEncoder rtspEncoder = new RtspEncoder();

        contentForm = new main.mainForm();
        System.out.println("GUI set up!");

        class serverThread extends Thread {
            @Override
            public void run() {
                try {
                    Server server = new Server(port, contentForm);
                    ServerThread sThread = new ServerThread(server, (new Socket("localhost", port)));
                    server.sendToAll("Hello");
                } catch (Exception ex) {
                    System.out.println("Error in server!");
                }
            }
        }
        class clientUIThread implements Runnable, ActionListener {
            private DataOutputStream dout;
            private DataInputStream din;
            private Socket socket;

            public clientUIThread() {
                contentForm.sendButton.addActionListener(this);
                try {
                    // Initiate the connection
                    socket = new Socket("localhost", port);

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
                if (e.getSource() == contentForm.sendButton)
                    processMessage(contentForm.chatText.getText());
            }

            // Gets called when the user types something
            protected void processMessage(String message) {
                try {
                    // Send it to the server
                    dout.writeUTF(message);
                    // Clear out text input field
                    contentForm.chatText.setText("");
                } catch (IOException ie) {
                    System.out.println(ie);
                }
            }

            @Override
            public void run() {
                try {
                    //ClientUI clientUI = new ClientUI("localhost", port, contentForm);
                    //clientUI.setVisible(true);
                    //clientUI.main(args);
                    clientUIThread thread = new clientUIThread();
                    try {
                        // Receive messages as long as it exists
                        while (true) {
                            // Get the message
                            String message = din.readUTF();
                            // Print it to the text window

                            contentForm.chatArea.append(message + "\n");
                        }
                    } catch (IOException ie) {
                        //System.out.println(ie);
                    }


                } catch (Exception ex) {
                    System.out.println("Error in client!");
                }
            }
        }
        //Socket socket = new Socket("localhost", port);
        //actionCall callButton = new actionCall();
        (new serverThread()).start();
        // Start up server and client
        SwingUtilities.invokeLater(new clientUIThread());


    }
}

class actionCall implements ActionListener {
    private String name;

    public actionCall(String user, String addr) {
        name = user.trim();
        String address = addr;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        JOptionPane.showMessageDialog(main.mainForm.getFrames()[0], "Will call: " + name);

    }
}
