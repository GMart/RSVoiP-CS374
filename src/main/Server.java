package main;
/*
 * Copyright (c) 2016.
 * By Garrett Martin (GMart on Github),
 *    Patrick Gephart (ManualSearch),
 *  & Matt Macke (BanishedAngel)
 * Class: main.Server
 * Last modified: 5/1/16 10:30 PM
 */

import javax.swing.*;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;

public class Server {
    public static int numOfConnections = 0; // Number of clients connected to server
    private ServerSocket ss; // ServerSocket used for new connections

    // Link sockets with DataOutPutStreams
    private Hashtable outputStreams = new Hashtable();

    /**
     * Constructor
     *
     * @param port - The port the server is using
     * @throws IOException - Try to avoid this
     */
    public Server(int port) throws IOException {

        listen(port);
    }

    /**
     * This method listens to all connections
     *
     * @param port - The port the server is using
     * @throws IOException - Try to avoid this
     */
    private void listen(int port) throws IOException {
        // Create the ServerSocket on the specified port
        ss = new ServerSocket(port);
        // Confirm ServerSocket is listening
        System.out.println("Listening to port " + ss.getLocalPort());
        // Keep accepting connections
        while (true && numOfConnections < 10) {
            // Make a connection
            Socket s = ss.accept();

            // Print success
            System.out.println("New connection from " + s);

            // Create a DataOutputStream
            DataOutputStream dout = new DataOutputStream(s.getOutputStream());

            // Increase numOfConnections by 1
            numOfConnections++;

            // Save this stream
            outputStreams.put(s, dout);

            // Create a new thread for this connection
            new ServerThread(this, s);
        }
    }

    // Get an enumeration of all the OutputStreams, one for each client
    Enumeration getOutputStreams() {
        return outputStreams.elements();
    }

    // Send a message to all clients
    void sendToAll(String message) {
        // Synchronize this in case RemoveConnection() is called
        synchronized (outputStreams) {
            // For every client connected
            for (Enumeration e = getOutputStreams(); e.hasMoreElements(); ) {
                // Obtain the output stream
                DataOutputStream dout = (DataOutputStream) e.nextElement();
                // Send a message
                try {
                    dout.writeUTF(message);
                } catch (IOException ie) {
                    System.out.println(ie);
                } catch (NullPointerException npe) {
                    npe.printStackTrace();
                }
            }
        }
    }

    // Terminate a connection by removing the socket and stream
    void removeConnection(Socket s) {
        // Synchronize so sendToAll is not affected
        synchronized (outputStreams) {
            // Print connection status
            System.out.println("Removing connection to " + s);

            // Remove it from hashtable
            outputStreams.remove(s);

            // Close connection to socket
            try {
                s.close();
            } catch (IOException ie) {
                System.out.println("Error closing " + s);
                ie.printStackTrace();
            }
        }
    }

    /**
     * Main method - Deprecated
     *
     * @param args - Not used
     * @throws Exception - Should not happen
     */
    static public void main(String args[]) throws Exception {
        // Get the port #
        String strPort = JOptionPane.showInputDialog("Enter the port number:");
        int port = 8080;
        boolean boolPort = true;
        if (strPort == null) {
            port = 8888;
        }

        // Create a Server object
        new Server(port);
    }
}