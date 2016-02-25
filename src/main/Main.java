package main;
/**
 * Created by Garrett on 2/11/2016.
 */

import javax.swing.*;
//import java.awt.event.*;
import main.Server;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        System.out.println("Test");


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
        class clientUIThread extends Thread {
            @Override
            public void run() {
                try {
                    ClientUI clientUI = new ClientUI("localhost", port);

                    clientUI.setVisible(true);
                    //clientUI.main(args);
                } catch (Exception ex) {
                }
            }
        }
        //  Socket socket = new Socket("localhost", port);
        (new serverThread()).start();
        (new clientUIThread()).start();

        mainForm contentForm = new main.mainForm();
        System.out.println("GUI set up!");

        while (true)
        {
            contentForm.getComponent(1);
        }
    }
}

