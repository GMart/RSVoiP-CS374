package main;
/*
 * Copyright (c) 2016.
 * By Garrett Martin (GMart on Github),
 *    Patrick Gephart (ManualSearch),
 *  & Matt Macke (BanishedAngel)
 * Class: main.ServerThread
 * Last modified: 4/14/16 3:18 PM
 */


import javax.swing.*;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * @author Matt Macke
 */
public class ServerThread extends Thread {
    private Server server; // The server
    private Socket socket; // The client socket
    private String name;   // The client's name

    /**
     * This is the constructor
     *
     * @param server - The server the socket is connected to
     * @param socket - The socket the client is using
     */
    public ServerThread(Server server, Socket socket) {
        this.server = server;
        this.socket = socket;

        // Get the client's name and store it
        this.name = JOptionPane.showInputDialog("Enter your name: ");
        if (name == null) {
            this.name = "Anonymous";
        }
        mainForm.setUserName(this.name);

        // Start the thread
        start();

    }

    public String getUserName() {
        return this.name;
    }

    /**
     * This runs in a separate thread when the constructor calls the start method
     */
    public void run() {
        try {
            // Create a DataInputStream to communicate with the client's
            // DataOutputStream
            DataInputStream din = new DataInputStream(socket.getInputStream());

            // Let everyone know a new client has joined the chat
            server.sendToAll(this.name + " has joined the chat!");

            // Infinite loop
            while (true) {
                // Read the message
                String message = din.readUTF();
                // Print the message
                System.out.println(this.name + ": " + message);
                // Send the message to the world
                server.sendToAll(this.name + ": " + message);
            }
        } catch (EOFException ie) {
        } catch (IOException ie) {
            // Print yuck
            ie.printStackTrace();
        } finally {
            // Close the connection and notify everyone that client has left
            server.sendToAll(this.name + " has left the chat!");
            server.removeConnection(socket);
        }
    }
}