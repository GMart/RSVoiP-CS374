package main;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Garrett on 2/11/2016.
 */
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

    public mainForm() {
        String names[] = {"Test User", "Garrett", "Matt"};
        String addrs[] = {"127.0.0.1", "12.23.34.45", "192.168.1.2"};

        this.setContentPane(rootPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JMenuBar menuBar;
        JMenu fileMenu = new JMenu("File");
        JMenuItem addUserItem = new JMenuItem("Add new user");
        JMenuItem delUserItem = new JMenuItem("Remove user");
        JMenuItem quitItem = new JMenuItem("Quit");
        menuBar = new JMenuBar();

        this.setJMenuBar(menuBar);
        menuBar.add(fileMenu);
        fileMenu.add(addUserItem);
        fileMenu.add(delUserItem);
        fileMenu.add(quitItem);

        contactsModel = new DefaultListModel();
        for (String name : names)
            contactsModel.addElement(name);     // Adds names from names to the List in the GUI
        //contactsList.setListData(names);
        contactsList.setModel(contactsModel);
        contactsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        class contactsListener implements ListSelectionListener {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting() == false) {
                    if (contactsList.getSelectedIndex() == -1) {
                    } else {
                        currentUser = contactsList.getSelectedIndex();
                        userLabel.setText(names[currentUser].trim());
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
                String newUser =
                        JOptionPane.showInputDialog(getRootPane(), "Add user", "Add new user", JOptionPane.OK_CANCEL_OPTION);
                //names[names.length] = newUser;
                contactsModel.addElement(newUser);
                //contactsList.setModel(contactsModel);
            }
        });
        delUserItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (contactsList.getSelectedIndex() != -1) {
                    contactsModel.remove(contactsList.getSelectedIndex());

                    currentUser--;
                    contactsList.setSelectedIndex(currentUser);
                }
            }
        });
        callButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), "Implementing soon", "Coming soon", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE);
                //callButton.setActionCommand(names[currentUser]);
                actionCall call = new actionCall(names[currentUser], addrs[currentUser]);
                call.actionPerformed(e);
            }
        });
        sendButton.addActionListener((new chatListener()));
        chatText.addActionListener((new chatListener()));

        this.pack();
        this.setLocationByPlatform(true);
        this.setVisible(true);
        // while (true) {

        //chatListener.newChat = "";
        //}
    }

    class chatListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Main.client.processMessage(chatText.getText());
        }
    }
    public void clearChatText() {
        chatText.setText("");
    }

    public void addChat(String chat) {
        chatArea.append("\n" + chat);
    }
}

