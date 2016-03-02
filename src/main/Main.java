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
        class serverThread extends Thread {
            @Override
            public void run() {
                try {
                    Server server = new Server(port);
                    //ServerThread sThread = new ServerThread(server, (new Socket("localhost",port)));

                } catch (Exception ex) {
                    System.out.println("Error in server!");
                }
            }
        }
        class clientUIThread implements Runnable {
            @Override
            public void run() {
                try {
                    ClientUI clientUI = new ClientUI("localhost", port, contentForm);

                    clientUI.setVisible(true);
                    clientUI.main(args);
                } catch (Exception ex) {

                }
            }
        }
        //Socket socket = new Socket("localhost", port);
        //actionCall callButton = new actionCall();
        (new serverThread()).start();
        // Start up server and client
        SwingUtilities.invokeLater(new clientUIThread());


        System.out.println("GUI set up!");


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
