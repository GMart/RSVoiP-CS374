package main;

/*
 Copyright (c) 2016.
 By Garrett Martin (GMart on Github),
    Patrick Gephart (ManualSearch),
  & Matt Macke (BanishedAngel)
 Class: main.User
 Last modified: 3/22/16 10:42 AM
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
    ArrayList<User> users = new ArrayList<>(6);

    public mainForm() {
        try {
            users.add(new User("Garrett Martin", InetAddress.getByName("127.0.0.1"), 001));
            users.add(new User("Matt", InetAddress.getByName("149.0.0.1"), 002));
            users.add(new User("Patrick", InetAddress.getByName("217.0.0.1"), 003));
        } catch (UnknownHostException e) {
        }
        String names[] = {"Test User", "Garrett", "Matt"};
        String addrs[] = {"127.0.0.1", "12.23.34.45", "192.168.1.2"};
        Font standardFont = new Font("Segoe", Font.PLAIN, 15);
        Font grayFont = new Font("Segoe", Font.ITALIC, 12);

        this.setTitle("RSVoiP messaging program - v0.1");
        this.setContentPane(rootPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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

        contactsModel = new DefaultListModel();
        contactsList.setListData(users.toArray());
        //contactsList.setModel(contactsModel);
        contactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        contactsList.setSelectedIndex(0);

        class contactsListener implements ListSelectionListener {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {     // If the user isn't modifying the selection
                    if (contactsList.getSelectedIndex() == -1) {
                    } else {                                // and they have selected a user
                        currentUser = contactsList.getSelectedIndex();  // Get that user index
                        userLabel.setText(users.get(currentUser).toString());
                        // Sets the userLabel to the currently selected name on the left in GUI.
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
                    String newAddrString = "555.555.555.555";
                    String newUserID = "";
                    while (newUserID.isEmpty()) {
                        newUserID =
                                JOptionPane.showInputDialog(getRootPane(),
                                        "What is " + newUser + "'s ID?",
                                        "Enter User ID",
                                        JOptionPane.QUESTION_MESSAGE);
                    }
                    users.ensureCapacity(users.size() + 2);
                    users.add(new User(newUser, InetAddress.getByName(newAddrString), Integer.parseInt(newUserID)));
                    contactsList.setListData(users.toArray());
                } catch (NumberFormatException nfe) {
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                }


            }
        });
        delUserItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (contactsList.getSelectedIndex() != -1) {
                    //contactsModel.remove(contactsList.getSelectedIndex());
                    users.remove(contactsList.getSelectedValue());
                    currentUser = (currentUser != 0) ? currentUser-- : currentUser++;
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
                //JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), "Implementing soon", "Coming soon", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE);
                actionCall call = new actionCall(users.get(currentUser));
                call.actionPerformed(e);
            }
        });
        sendButton.addActionListener((new chatListener())); // Send message
        chatText.addActionListener((new chatListener()));   // Send message when enter pressed

        this.pack();
        this.setLocationByPlatform(true);
        this.setVisible(true);
        // while (true) {


        //}
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
                e1.printStackTrace();
                System.out.println(e1);
                chatArea.setText("");          // If setting fails, set to nothing
            }

        }
    }

    public void clearChatText() {
        chatText.setText("");
    }

    public void addChat(String chat) {
        chatArea.append(chat + "\n");
    }
}

