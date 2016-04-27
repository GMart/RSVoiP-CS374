package main;

/*
 * Copyright (c) 2016.
 * By Garrett Martin (GMart on Github),
 *    Patrick Gephart (ManualSearch),
 *  & Matt Macke (BanishedAngel)
 * Class: main.sendAudioThread
 * Last modified: 4/27/16 12:28 PM
 */

import javax.sound.sampled.*;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketOptions;

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
            int bufferSize = (int) audioFormat.getSampleRate() / 8;// * audioFormat.getFrameSize();
            byte buffer[] = new byte[bufferSize];
            BufferedOutputStream bufferedStream = new BufferedOutputStream(socket.getOutputStream(), bufferSize);
            System.out.println("Buffersize for sending:" + bufferSize);
            while (sendLine.isOpen() && running) {
                int count = sendLine.read(buffer, 0, buffer.length);
                if (count > 0 || (sendLine.getLevel() > 0)) { // Arbitrary volume level (not working) to reduce data sent
                    bufferedStream.write(buffer, 0, count);
                    System.out.println("Sending audio, socket buffer size = " + bufferSize);
                    //System.out.println("Sending: " + Arrays.toString(buffer));

                }
            }
            bufferedStream.close();
            sendLine.close();
            socket.close();
            System.out.println("Stopped sending!");
            Main.contentForm.addChat("Hung up call");
        } catch (IOException | LineUnavailableException e) {
            System.out.println("Error in capturing audio with connection " + socket.toString());
            //e.printStackTrace();
        } finally {
            setRunning(false);
        }
    }
}
