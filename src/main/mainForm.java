package main;

/*
 * Copyright (c) 2016.
 * By Garrett Martin (GMart on Github),
 *    Patrick Gephart (ManualSearch),
 *  & Matt Macke (BanishedAngel)
 * Class: main.User
 * Last modified: 4/5/16 12:20 AM
 */

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import java.awt.*;
import java.awt.event.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static main.Main.Username;

/**
 * Created by Garrett on 2/11/2016.
 */
class User {
    String name;
    InetAddress address;
    int userID;

    User(String name, InetAddress address, int userID) {
        this.name = name;
        this.address = address;
        this.userID = userID;
    }

    @Override
    public String toString() {
        return name.trim();
    }
}

public class mainForm extends JFrame {
    private JList contactsList;
    private DefaultListModel contactsModel;
    private JScrollPane contactsScroll;
    private JPanel rootPanel;
    private JButton callButton;
    JButton sendButton;
    private JLabel userLabel;
    private JPanel actionPanel;
    private JSplitPane splitPanel;
    public JTextField chatText;     // Where you type in chat
    private JSplitPane rightPane;
    private JScrollPane chatTextHist;
    JTextArea chatArea;             // Where the chat appears
    int currentUser = 0;
    boolean typingMessage = false;
    boolean inCallNow = false;
    ArrayList<User> users = new ArrayList<>(6);

    public mainForm() {
        try {
            users.add(new User("Garrett Martin", InetAddress.getByName("127.0.0.1"), 101));
            users.add(new User("Matt", InetAddress.getByName("149.0.0.1"), 102));
            users.add(new User("Patrick", InetAddress.getByName("217.0.0.1"), 103));
        } catch (UnknownHostException e) {
        }
        //// SETTING UP GUI ELEMENTS  ////

        Font standardFont = new Font("Segoe", Font.PLAIN, 15);
        Font grayFont = new Font("Segoe", Font.ITALIC, 12);

        this.setTitle("RSVoiP messaging program - v0.2");
        this.setContentPane(rootPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //this.setTitle("RSVoiP messaging program - " + Username);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem addUserItem = new JMenuItem("Add new user");
        JMenuItem delUserItem = new JMenuItem("Remove user");
        JMenuItem quitItem = new JMenuItem("Quit");
        JMenu chatMenu = new JMenu("Chat", true);
        JMenuItem clearChatItem = new JMenuItem("Clear chat history to one line");

        fileMenu.add(addUserItem);
        fileMenu.add(delUserItem);
        fileMenu.add(quitItem);
        chatMenu.add(clearChatItem);
        menuBar.add(fileMenu);
        menuBar.add(chatMenu);
        this.setJMenuBar(menuBar);

        JPopupMenu clearPopup = new JPopupMenu("Clear");
        JMenuItem clearItem = new JMenuItem("Clear chat history");
        clearItem.addActionListener(new clearChatHistory());
        clearPopup.add(clearItem);
        MouseListener popupListener = new PopupListener(clearPopup);
        chatArea.addMouseListener(popupListener);
        //// DONE SETTING UP GUI    ////
        //// SETTING UP LISTENERS   ////
        contactsModel = new DefaultListModel();
        contactsList.setListData(users.toArray());
        //contactsList.setModel(contactsModel);
        contactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactsList.setSelectedIndex(0);
        userLabel.setText(users.get(currentUser).toString());

        class contactsListener implements ListSelectionListener {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {     // If the user isn't modifying the selection
                    if (contactsList.getSelectedIndex() == -1) {
                    } else {                                // and they have selected a user
                        currentUser = contactsList.getSelectedIndex();  // Get that user index
                        userLabel.setText(users.get(currentUser).toString());
                        // Sets the userLabel to the currently selected name on the left in GUI.
                        //TODO: Make changing the user work correctly - tear down old connection and make new one
                        //Main.changeConnection(users.get(currentUser).address.toString().substring(1), users.get(currentUser).userID);
                        setTitle("RSVoiP messaging program - " + users.get(currentUser).toString());
                    }
                }
            }
        }
        contactsList.addListSelectionListener(new contactsListener());

        quitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        addUserItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String newUser =
                            JOptionPane.showInputDialog(getRootPane(),
                                    "Add user",
                                    "Add new user",
                                    JOptionPane.QUESTION_MESSAGE);
                    String newAddrString = "127.0.0.01";
                    String newUserID = "";
                    while (newUserID.isEmpty()) {
                        newUserID =
                                JOptionPane.showInputDialog(getRootPane(),
                                        "What is " + newUser + "'s ID?",
                                        "Enter User ID",
                                        JOptionPane.QUESTION_MESSAGE);
                    }
                    users.ensureCapacity(users.size() + 1);
                    users.add(new User(newUser, InetAddress.getByName(newAddrString), Integer.parseInt(newUserID)));
                    contactsList.setListData(users.toArray());
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                } catch (UnknownHostException e1) {
                    JOptionPane.showMessageDialog(getContentPane(), "Error: Invalid IP address. Cancelling add user",
                            "Error!", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
        delUserItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (contactsList.getSelectedIndex() != -1) {
                    //contactsModel.remove(contactsList.getSelectedIndex());
                    users.remove(contactsList.getSelectedValue());
                    if (currentUser != 0)
                        currentUser--;
                    else currentUser++;
                    //Change current user
                    contactsList.setSelectedIndex(currentUser);
                    contactsList.setListData(users.toArray()); //Finally send the user data to the list
                }
            }
        });
        clearChatItem.addActionListener(new clearChatHistory());
        callButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actionCall call = new actionCall(users.get(currentUser), inCallNow);
                call.actionPerformed(e);
            }
        });
        sendButton.addActionListener((new chatListener())); // Send message
        chatText.addActionListener((new chatListener()));   // Send message when enter pressed
        //// DONE SETTING UP LISTENERS  ////
        //// FINALIZING GUI             ////
        this.pack();
        this.setLocationByPlatform(true);
        this.setVisible(true);
        chatText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                super.focusGained(e);
                if (!typingMessage) {
                    chatText.setFont(standardFont);
                    chatText.setText("");
                    typingMessage = true;

                } else {
                    chatText.setFont(standardFont);
                }

            }

            @Override
            public void focusLost(FocusEvent e) {
                super.focusLost(e);
                if (!typingMessage) {
                    chatText.setFont(grayFont);
                } else if (chatText.getText().isEmpty()) {
                    typingMessage = false;
                    chatText.setFont(grayFont);
                    chatText.setText("Enter a message to send");
                }
            }
        });
    }

    class chatListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (typingMessage)
                Main.client.processMessage(chatText.getText());
        }
    }

    class PopupListener extends MouseAdapter {
        JPopupMenu popup;

        PopupListener(JPopupMenu popupMenu) {
            popup = popupMenu;
        }

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }

    class clearChatHistory implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {        // Fires when popup menu or menuItem is clicked
            int chatLength = chatArea.getText().length();   // Length of chat history
            try {
                if (chatArea.getSelectedText() == null) {
                    chatArea.setText(chatArea.getText(chatArea.getText().lastIndexOf('\n', chatLength - 2) + 1,
                            chatLength - chatArea.getText().lastIndexOf('\n',
                                    chatLength - 2)).trim());
                    chatArea.append("\n");
                } else
                    chatArea.setText(chatArea.getText(chatArea.getSelectionStart(), chatArea.getSelectionEnd()));
            } catch (BadLocationException e1) {
                System.out.println(e1);
                chatArea.setText("");          // If setting fails, set to nothing
            }

        }
    }

    /**
     * Allows the Chat Client to clear the text box when text is successfully sent
     */
    public void clearChatText() {
        chatText.setText("");
    }

    static void setUserName(String user) {
        Username = user;
    }

    public void addChat(String chat) {
        chatArea.append(chat + "\n");
    }
}

