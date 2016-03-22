package main;
/*
 Copyright (c) 2016.
 By Garrett Martin (GMart on Github),
    Patrick Gephart (ManualSearch),
  & Matt Macke (BanishedAngel)
 Class: main.ClientUI
 Last modified: 3/22/16 10:42 AM
 */

/*
 *           Name: Matthew Macke
 * Project Number: 2
 *         Course: CS350
 *           Date: 3/25/2015
 */

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;


@SuppressWarnings("serial")
public class ClientUI extends JFrame implements Runnable, ActionListener {
    private JPanel contentPane;
    // The socket connecting us to the server
    private Socket socket;
    private mainForm theForm;

    // The streams to communicate
    private DataOutputStream dout;
    private DataInputStream din;

    private JButton btnSubmitText;
    private JTextArea chat = theForm.chatArea;
    private JTextField chatInput;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        String host = "localhost";
        String strPort = "";
        int port = 8080;

        // Get the port number
        //strPort = JOptionPane.showInputDialog("Enter the port number:");
        boolean boolPort = false;

        while (!boolPort) {
            try {
                port = Integer.parseInt(strPort);
            } catch (Exception x) {

            }
            if (port < 1024 || port > 65535) {
                strPort = JOptionPane
                        .showInputDialog("Enter the port number:");
            } else {
                boolPort = true;
            }

        }


        // Create the ClientUI
        //ClientUI frame = new ClientUI(host, port, new mainForm());
        //frame.setVisible(true);
    }

    /**
     * Make the frame and connect to server
     *
     * @param host - the host
     * @param port - the port the server is on
     */
    public ClientUI(String host, int port, mainForm form) {
        theForm = form;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 600, 500);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        btnSubmitText = new JButton("Submit Text");
        btnSubmitText.setBounds(450, 404, 144, 68);
        contentPane.add(btnSubmitText);
        btnSubmitText.addActionListener(this);

        //chat = new JTextArea();
        //form.chatArea.setEditable(false);
        //form.chatArea.setBounds(6, 6, 588, 386);
        //contentPane.add(chat);

        //chatInput = new JTextArea();
        chatInput.setBounds(6, 404, 432, 68);
        //contentPane.add(chatInput);

        // Connect to the server
        try {
            // Initiate the connection
            socket = new Socket(host, port);

            // Gram the streams
            din = new DataInputStream(socket.getInputStream());
            dout = new DataOutputStream(socket.getOutputStream());

            // Start a background thread for receiving messages
            new Thread(this).start();
        } catch (IOException ie) {
            System.out.println(ie);
        }
    }

    // This method is called when the submit button is pressed
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == btnSubmitText)
            processMessage(chatInput.getText());
    }

    // Gets called when the user types something
    protected void processMessage(String message) {
        try {
            // Send it to the server
            dout.writeUTF(message);
            // Clear out text input field
            //theForm.chatText.setText("");
        } catch (IOException ie) {
            System.out.println(ie);
        }
    }

    // Display messages from others
    public void run() {

        try {
            // Receive messages as long as it exists
            while (true) {
                // Get the message
                String message = din.readUTF();
                // Print it to the text window


            }
        } catch (IOException ie) {
            //System.out.println(ie);
        }
    }
}
