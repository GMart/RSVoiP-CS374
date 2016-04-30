package main;

/*
 * Copyright (c) 2016.
 * By Garrett Martin (GMart on Github),
 *    Patrick Gephart (ManualSearch),
 *  & Matt Macke (BanishedAngel)
 * Class: main.receiveAudioThread
 * Last modified: 4/30/16 1:00 PM
 */

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * This class starts a thread with a ServerSocket and waits to connect,
 * then just plays the audio from the socket connection out of the main sound line.
 */
class receiveAudioThread extends Thread {
    private boolean running = true;
    private boolean makingACall = false;    // When call is placed locally, this is true
    private ServerSocket socket;

    receiveAudioThread(ServerSocket socket) {
        this.socket = socket;
        setRunning(true);
    }

    synchronized void setRunning(boolean running) { // Lets us control this Thread from another
        this.running = running;
    }

    boolean isRunning() {
        return running;
    }

    void setMakingACall(boolean makingACall) {
        this.makingACall = makingACall;
    }

    @Override
    public void run() {
        System.out.println("Starting audio!");
        AudioFormat format = new AudioFormat(48000, 16, 1, true, false);
        BufferedInputStream bufferedInputStream;

        try (SourceDataLine soundData = AudioSystem.getSourceDataLine(format)) {
            soundData.open(format);
            System.out.println("Opened Sound");
            socket.setSoTimeout(2000);       // Wait [TIMEOUT] to receive a call
            Socket connection = null;        // Get the connection from other person

            while (connection == null) {
                if (isRunning()) {  // If we should still be running...
                    try {           // Wait for [TIMEOUT] seconds
                        connection = socket.accept();
                    } catch (SocketTimeoutException e) {
                        System.out.println(e.getMessage());
                    }
                } else {            // If we should stop:
                    socket.close(); // Close the socket
                    throw new LineUnavailableException("Not running");         // Exit the thread
                }
            }
            System.out.println("Received connection!!");

            connection.setSoTimeout(10000);             // Set timeout to 10 secs
            if (makingACall) {   // If WE are making the call, then just receive quietly
                System.out.println("Getting audio without making a new receive thread");
            } else {             // Else ask the user and make a new sendAudioThread
                if (JOptionPane.showConfirmDialog(Main.contentForm.getFocusOwner(),
                        "Would you like to pick up the call from" + connection.toString() + " ?", "Incoming call!",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {    // If yes
                    Main.contentForm.setCallButtonText("Hangup");
                    Main.contentForm.inCallNow = true; // Set the calling state to true
                    CallingStarter.audioSendThread = new sendAudioThread(connection);
                } else { // If they don't want to make the call, close the socket to tell the other person they are not welcome
                    connection.close();
                    return;             // Exit the method (Sounddata is closed automatically)
                }
            }

            bufferedInputStream = new BufferedInputStream(connection.getInputStream()); // Get Input from socket

            soundData.start();
            int bufferSize = (int) format.getSampleRate() / 8;// * format.getFrameSize();
            byte buffer[] = new byte[bufferSize];

            while (soundData.isOpen() && running) {
                int count = bufferedInputStream.read(buffer, 0, bufferSize);
                if (count > 0) {
                    soundData.write(buffer, 0, count);
                    System.out.println("Receiving audio, socket buffer size = " + bufferSize);
                }
            }
            socket.close();     // Close Socket when done the call
        } catch (IOException e) {
            System.out.println("Stream problem");
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            System.out.println("Closed");
        } finally {
            System.out.println("Finished receiving thread");
            setRunning(false);
        }
    }
}
