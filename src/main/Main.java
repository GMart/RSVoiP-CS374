package main;

/*
 * Copyright (c) 2016.
 * By Garrett Martin (GMart on Github),
 *    Patrick Gephart (ManualSearch),
 *  & Matt Macke (BanishedAngel)
 * Class: main.Main
 * Last modified: 4/14/16 3:02 AM
 */

/**
 * Created by Garrett on 2/11/2016.
 */

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;

public class Main {
    static mainForm contentForm;
    static clientUIThread client;
    private static Server server;
    static int port = 1200; // For chatting
    static int controlPort = 1199; // BuddyServer / proxy server control port
    static int audioPort = 1201;   // Audio send/receive port, may change when buddy server code is done
    static String Username; // User's own Name
    static String serverIP = "127.0.0.1";
    static String userID;

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
        //(new Thread(new serverThread())).start();

        //client = new clientUIThread("127.0.0.1");

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                contentForm = new mainForm();
            }
        });

        System.out.println("GUI set up!");
        serverIP = JOptionPane.showInputDialog("Enter IP address to connect to:", serverIP);
        //sendIPToServer();
        //(new receiveAudioThread(new ServerSocket(1201))).start();
        //(new sendAudioThread(new Socket("localhost", 1201))).start();

        //System.out.println("IP sent to server");

        //sendAudioThread(socket);         // Testing audio sending and receiving
    }

    /**
     * This method will connect the Buddy Server and update the IP address for the user.
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
            out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            out.write(str, 0, str.length());
            out.write(str2, 0, str2.length());
            out.close();
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

/**
 * Sends audio from the first mic to the socket, buffered.
 * Thread created when "Call" button pressed and no call is currently in progress.
 */
class sendAudioThread extends Thread {
    private boolean running = true;
    Socket socket;

    sendAudioThread(Socket socket) {
        this.socket = socket;
    }

    synchronized void setRunning(boolean running) { // Lets us control this Thread from another
        this.running = running;
    }

    @Override
    public void run() {
        AudioFormat audioFormat = new AudioFormat(48000, 16, 1, true, false); //Audio stream format, may tweak
        setRunning(true);
        try {
            socket.setTcpNoDelay(true);
            socket.setTrafficClass(SocketOptions.IP_TOS);
            socket.setPerformancePreferences(1, 5, 2);
            System.out.println("Setting up sending audio!");

            DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
            TargetDataLine sendLine = AudioSystem.getTargetDataLine(info.getFormats()[0]);
            sendLine.open(audioFormat);
            sendLine.start();                   // Opens the mic and starts to get data from it
            System.out.println("Opened mic");
            int bufferSize = (int) audioFormat.getSampleRate() / 4;// * audioFormat.getFrameSize();
            byte buffer[] = new byte[bufferSize];
            BufferedOutputStream bufferedStream = new BufferedOutputStream(socket.getOutputStream(), bufferSize);
            System.out.println("Buffersize for sending:" + bufferSize);
            while (sendLine.isOpen() && running) {
                int count = sendLine.read(buffer, 0, buffer.length);
                if (count > 0 || (sendLine.getLevel() > 0)) { // Arbitrary volume level (not working) to reduce data sent
                    bufferedStream.write(buffer, 0, count);
                    System.out.println("Sending audio: " + sendLine.getBufferSize());
                    //System.out.println("Sending: " + Arrays.toString(buffer));

                }
            }
            bufferedStream.close();
            sendLine.close();
            socket.close();
            System.out.println("Stopped sending!");
        } catch (IOException | LineUnavailableException e) {
            System.out.println("Error in capturing audio with connection " + socket.toString());
            //e.printStackTrace();
        } finally {
            setRunning(false);
        }
    }
}

/**
 * This class starts a thread with a ServerSocket and waits to connect,
 * then just plays the audio from the socket connection out of the main sound line.
 */
class receiveAudioThread extends Thread {
    private boolean running = true;
    private ServerSocket socket;

    receiveAudioThread(ServerSocket socket) {
        this.socket = socket;
    }

    synchronized void setRunning(boolean running) { // Lets us control this Thread from another
        this.running = running;
    }

    public void run() {
        System.out.println("Starting audio!");
        AudioFormat format = new AudioFormat(48000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        BufferedInputStream bufferedInputStream;
        try (SourceDataLine soundData = AudioSystem.getSourceDataLine(format)) {    // Try with resource
            soundData.open(format);
            System.out.println("Opened Sound");
            Socket connection = socket.accept();                // Get the connection from other person
            bufferedInputStream = new BufferedInputStream(connection.getInputStream()); // Get Input from socket
            System.out.println("Received connection!!");
            soundData.start();
            int bufferSize = (int) format.getSampleRate() / 4;// * format.getFrameSize();
            byte buffer[] = new byte[bufferSize];
            try {
                while (soundData.isOpen() && running) {
                    int count = bufferedInputStream.read(buffer, 0, bufferSize);
                    if (count > 0) {
                        soundData.write(buffer, 0, count);
                        System.out.println("Getting audio at: " + soundData.getBufferSize());
                    }
                }
                bufferedInputStream.close();        // Close the stream when done
                socket.close();
            } catch (IOException e) {
                System.out.println("Stream problem");
                e.printStackTrace();
            }
        } catch (LineUnavailableException e) {
            System.out.println("Line unavailable");
        } catch (IOException e) {
            System.out.println("IO exception!");
        }
    }
}

class actionCall implements ActionListener {    // Fires when "Call" button is pressed.
    private String name;
    private boolean endTheCall;
    static sendAudioThread audioSendThread = null; // The Thread used for sending audio.
    static receiveAudioThread audioRecvThread = null; // Static so this class doesn't have to manage the threads all the time

    /**
     * Gets the name, ?Address?, and status of the call. First button press will start call, second will end.
     *
     * @param user The username of the person currently selected
     * @param endCall Whether we should end the call instead of starting one.
     */
    actionCall(User user, boolean endCall) {
        name = user.toString();         // Grab the name of the user
        endTheCall = endCall;
    }

    /**
     * Runs when the ActionListener on the "Call" button is pressed.
     *
     * @param e Not used here
     */
    @Override
    public synchronized void actionPerformed(ActionEvent e) {
        Socket audioSendSocket;

        JOptionPane.showMessageDialog(main.mainForm.getFrames()[0], "Trying to call: " + name);
        if (!endTheCall) {
            //TODO: Initiate the call - Query and set up the correct socket, using test socket for now
            try {
                audioRecvThread = new receiveAudioThread(new ServerSocket(Main.audioPort));  // Receive audio firstly
                audioRecvThread.start();
                audioSendSocket = new Socket(Main.serverIP, Main.audioPort);         // Change localhost back to Main.serverIP
                audioSendThread = new sendAudioThread(audioSendSocket);

                audioSendThread.start();// Start that Thread
            } catch (IOException e1) {
                System.out.println("Calling or socket problem");
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
