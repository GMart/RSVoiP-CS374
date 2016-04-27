package main;

/*
 * Copyright (c) 2016.
 * By Garrett Martin (GMart on Github),
 *    Patrick Gephart (ManualSearch),
 *  & Matt Macke (BanishedAngel)
 * Class: main.receiveAudioThread
 * Last modified: 4/27/16 12:28 PM
 */

import javax.sound.sampled.*;
import javax.swing.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

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
            socket.setSoTimeout(0);                     // Wait forever to receive a call
            Socket connection = socket.accept();        // Get the connection from other person
            connection.setSoTimeout(10000);             // Set timeout to 10 secs
            if (makingACall) {   // If WE are making the call, then just receive quietly
                System.out.println("Calling without making a new receive thread");
            } else {             // Else ask the user and make a new sendAudioThread
                if (JOptionPane.showConfirmDialog(Main.contentForm.getFocusOwner(),
                        "Would you like to pick up the call from" + connection.toString() + " ?", "Incoming call!",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == 0) {    // If yes
                    Main.contentForm.setCallButtonText("Hangup");
                    Main.contentForm.inCallNow = true; // Set the calling state to true
                    CallingStarter.audioSendThread = new sendAudioThread(new Socket(Main.serverIP, Main.audioPort));
                } else { // If they don't want to make the call, close the socket to tell the other person they are not welcome
                    connection.close();
                    return;             // Exit the method (Sounddata is closed automatically)
                }
            }

            bufferedInputStream = new BufferedInputStream(connection.getInputStream()); // Get Input from socket
            System.out.println("Received connection!!");
            soundData.start();
            int bufferSize = (int) format.getSampleRate() / 8;// * format.getFrameSize();
            byte buffer[] = new byte[bufferSize];
            try {
                while (soundData.isOpen() && running) {
                    int count = bufferedInputStream.read(buffer, 0, bufferSize);
                    if (count > 0) {
                        soundData.write(buffer, 0, count);
                        System.out.println("Receiving audio, socket buffer size = " + bufferSize);
                    }
                }
                socket.close();     // and Socket when done the call
            } catch (IOException e) {
                System.out.println("Stream problem");
                e.printStackTrace();
            }
        } catch (LineUnavailableException e) {
            System.out.println("Line unavailable");
        } catch (IOException e) {
            System.out.println("IO exception!");
        } finally {
            System.out.println("Finished receiving thread");
        }
    }
}
