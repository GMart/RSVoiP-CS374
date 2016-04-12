package main;

/*
 * Copyright (c) 2016.
 * By Garrett Martin (GMart on Github),
 *    Patrick Gephart (ManualSearch),
 *  & Matt Macke (BanishedAngel)
 * Class: main.Main
 * Last modified: 4/12/16 3:48 PM
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
    static String Username; // User's own Name
    static String serverIP = "149.164.221.6";
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

        //sendIPToServer();


        System.out.println("IP sent to server");

        //Socket socket = new Socket("null", port);
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
            socket = new Socket(serverIP, 1199);
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

    /**
     * Play audio - TODO: Modify to receive a socket parameter and play that directly
     *
     * @param audioClip Audio to play
     */
    static void startPlayingAudio(ServerSocket socket) {
        AudioFormat format;
        TargetDataLine targetDataLine;
        format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        BufferedInputStream bufferedInputStream = null;
        SourceDataLine soundData = null;
        try {
            //targetDataLine = AudioSystem.getTargetDataLine(format);
            soundData = AudioSystem.getSourceDataLine(format);
            soundData.open(format);
            //targetDataLine.open(format);
            bufferedInputStream = new BufferedInputStream(socket.accept().getInputStream());

        } catch (LineUnavailableException e) {
            System.out.println("Line unavailable");

        } catch (IOException e) {
            System.out.println("IO exception!");

        }
        byte buffer[] = new byte[512];
        try {
            while (bufferedInputStream.available() > 0) {
                int count = bufferedInputStream.read(buffer, 0, 512);
                if (count > 0) {
                    soundData.write(buffer, 0, 512);
                }
            }
            bufferedInputStream.close();        // Close the stream when done
        } catch (IOException e) {
            System.out.println("Stream problem");
            e.printStackTrace();
        } finally {
            soundData.close();                  // Always close the microphone
        }

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

    public synchronized void setRunning(boolean running) { // Lets us control this Thread from another
        this.running = running;
    }

    @Override
    public void run() {
        AudioFormat audioFormat = new AudioFormat(16000, 16, 1, true, false); //Audio stream format, may tweak
        setRunning(true);
        try {
            socket.setTcpNoDelay(true);
            socket.setTrafficClass(SocketOptions.IP_TOS);
            socket.setPerformancePreferences(1, 5, 2);


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
                    final AudioInputStream ais = new AudioInputStream(input, audioFormat, buffer.length / audioFormat.getFrameSize());

                }
            }
            bufferedStream.close();

        } catch (LineUnavailableException | IOException e) {
            System.out.println("Error in capturing audio with connection " + socket.toString());
            //e.printStackTrace();
        }
    }
}

class receiveAudioThread extends Thread {
    private boolean running = true;
    Socket socket;

    public void run() {
        try {
            Main.startPlayingAudio(new ServerSocket(1201));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class actionCall implements ActionListener {    // Fires when "Call" button is pressed.
    private String name;
    private boolean endTheCall;

    /**
     * Gets the name, ?Address?, and status of the call. First button press will start call, second will end.
     *
     * @param user The username of the person currently selected
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
        sendAudioThread audioSendThread = null; // The Thread used for sending audio.
        Socket audioSendSocket;

        JOptionPane.showMessageDialog(main.mainForm.getFrames()[0], "Trying to call: " + name);
        if (!endTheCall) {
            //TODO: Initiate the call - Query and set up the correct socket, using test socket for now
            try {
                (new receiveAudioThread()).start();
                audioSendSocket = new Socket("localhost", 1201);
                audioSendThread = new sendAudioThread(audioSendSocket);

                audioSendThread.start();// Start that Thread
            } catch (IOException e1) {
                System.out.println("Calling or socket problem");
                e1.printStackTrace();
            }
        } else {
            // TODO: End the call
            if (audioSendThread == null || audioSendThread.isAlive()) {
                audioSendThread.setRunning(false);
            }
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
