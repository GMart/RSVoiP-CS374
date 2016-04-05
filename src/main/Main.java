package main;

/*
 * Copyright (c) 2016.
 * By Garrett Martin (GMart on Github),
 *    Patrick Gephart (ManualSearch),
 *  & Matt Macke (BanishedAngel)
 * Class: main.Main
 * Last modified: 4/5/16 12:23 AM
 */

/**
 * Created by Garrett on 2/11/2016.
 */

import javafx.scene.media.AudioClip;

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
    static int port = 1200; // Temporary
    static String Username; // User's own Name
    static String serverIP = "HIDDEN--REPLACE";
    static String userID;

    public static void main(String[] args) throws IOException {

        class serverThread implements Runnable {
            @Override
            public synchronized void run() {
                try {
                    server = new Server(port);
                    wait(1000);

                } catch (Exception ex) {
                    System.out.println("Error in server, could not start!");
                }
            }
        }   // Different from ServerThread (That has one for each connection)

        //RtspDecoder rtspDecoder = new RtspDecoder(); // In the future use RTP?
        //RtspEncoder rtspEncoder = new RtspEncoder();
        // Start up server and client
        (new Thread(new serverThread())).start();

        client = new clientUIThread("localhost");

        synchronized (client) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    contentForm = new mainForm();
                }
            });
        }

        System.out.println("GUI set up!");

        sendIPToServer();

        System.out.println("IP sent to server");

        //Socket socket = new Socket("null", port);
        //sendAudioThread(socket);         // Testing audio sending and receiving
        //TODO: Currently this doesn't work because there is nowhere to send the audio to - the socket can't connect
    }

    /**
     * This method will connect the Buddy Server and update the IP address for the user.
     *
     */
    public static void sendIPToServer() throws IOException {
        Socket socket = null;
        OutputStreamWriter out;

        // Get public IP
        URL whatismyip = new URL("http://checkip.amazonaws.com");
        BufferedReader in = new BufferedReader(new InputStreamReader(
                whatismyip.openStream()));
        String ip = in.readLine();

        // Craft string to send to server
        String str;
        userID = JOptionPane.showInputDialog("What is your userID?");
        str = "1/" + userID + "/" + ip;

        try {
            socket = new Socket("68.39.45.194", 1199);
            out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
            out.write(str, 0, str.length());
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

    /**
     * Play audio - TODO: Modify to receive a socket parameter and play that directly
     *
     * @param audioClip Audio to play
     */
    static void startPlayingAudio(AudioClip audioClip) {
        AudioFormat format;
        TargetDataLine targetDataLine;
        format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        try {
            targetDataLine = AudioSystem.getTargetDataLine(format);
            SourceDataLine soundData = AudioSystem.getSourceDataLine(format);
            soundData.open(format);
            targetDataLine.open(format);

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
        if (!audioClip.isPlaying())
            audioClip.play(0.9);

    }

    /**
     * Sends audio from the first mic to the socket, buffered.
     * Called when "Call" button pressed and no call is currently in progress.
     *
     * @param socket Socket to send audio through.
     */
    static Thread sendAudioThread(Socket socket) {
        Runnable runnable = new Runnable() {
            boolean running = true;

            public void setRunning(boolean running) { // Lets us control this Thread from another
                this.running = running;
            }

            public void run() {

                AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false);
                try {
                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
                    TargetDataLine sendLine = (TargetDataLine) AudioSystem.getLine(info);
                    sendLine.open(audioFormat);
                    sendLine.start();
                    int bufferSize = (int) audioFormat.getSampleRate() * audioFormat.getFrameSize() * 2;
                    byte buffer[] = new byte[bufferSize];
                    BufferedOutputStream bufferedStream = new BufferedOutputStream(socket.getOutputStream(), bufferSize);
                    while (sendLine.isRunning() && running) {
                        int count = sendLine.read(buffer, 0, buffer.length);
                        if (count > 0 && (sendLine.getLevel() > 0.01)) { // Arbitrary volume level to reduce bandwidth
                            bufferedStream.write(buffer, 0, count);
                            InputStream input = new ByteArrayInputStream(buffer);
                            final AudioInputStream ais = new AudioInputStream(input, audioFormat,
                                                                              buffer.length / audioFormat.getFrameSize());

                        }
                    }
                    bufferedStream.close();

                } catch (LineUnavailableException | IOException e) {
                    System.out.println("Error in capturing audio with connection " + socket.toString());
                    //e.printStackTrace();
                }
            }
        };
        return new Thread(runnable);
    }

    public void sendPressed(String message) {
        client.processMessage(message);
        contentForm.clearChatText();
    }

    public static void addChatText(String chat) {
        contentForm.addChat(chat);
    }
}

class actionCall implements ActionListener {    // Fires when "Call" button is pressed.
    private String name;
    private boolean endTheCall;
    private InetAddress address;

    /**
     * Gets the name, ?Address?, and status of the call. First button press will start call, second will end.
     *
     * @param user The username of the person currently selected
     */
    actionCall(User user, boolean endCall) {
        name = user.toString();         // Grab the name of the user
        address = user.address;
        endTheCall = endCall;
    }

    /**
     * Runs when the ActionListener on the "Call" button is pressed.
     *
     * @param e Not used here
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        Thread audioSendThread; // The Thread used for sending audio.
        JOptionPane.showMessageDialog(main.mainForm.getFrames()[0], "Trying to call: " + name);
        if (!endTheCall) {
            //TODO: Initiate the call - Query and set up the correct socket, using test socket for now
            try {
                audioSendThread = Main.sendAudioThread(new Socket(Main.serverIP, 1199));
                audioSendThread.start();// Start that Thread
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        } else {
            // TODO: End the call
            //audioSendThread.setRunning(false);
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
