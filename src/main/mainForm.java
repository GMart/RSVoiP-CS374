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
    private JButton sendButton;
    private JLabel userLabel;
    private JPanel actionPanel;
    private JSplitPane splitPanel;
    private JTextArea chatText;
    private JSplitPane rightPane;
    private JScrollPane chatTextHist;
    private JTextArea chatArea;

    public mainForm() {
        String names[] = {"Test User", "Garrett", "Matt"};
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
                        userLabel.setText(contactsList.getSelectedValue().toString());
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
                contactsModel.addElement(newUser);
            }
        });
        delUserItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (contactsList.getSelectedIndex() != -1) {
                    contactsModel.remove(contactsList.getSelectedIndex());
                    contactsList.setSelectedIndex(-1);
                }
            }
        });
        callButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showConfirmDialog(JOptionPane.getRootFrame(), "Implementing soon", "Coming soon", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE);
            }
        });

        this.pack();
        this.setLocationByPlatform(true);
        this.setVisible(true);

    }

}
