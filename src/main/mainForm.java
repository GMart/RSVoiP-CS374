package main;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Garrett on 2/11/2016.
 */
public class mainForm extends JFrame {
    private JList contactsList;
    private JScrollPane ContactsScroll;
    private JPanel rootPanel;
    private JButton callButton;
    private JButton sendFileButton;
    private JLabel userLabel;
    private JPanel actionPanel;
    private JSplitPane splitPanel;


    public mainForm() {
        this.setContentPane(rootPanel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JMenuBar menuBar;
        JMenu fileMenu = new JMenu("File");
        JMenuItem quitItem = new JMenuItem("Quit");
        menuBar = new JMenuBar();

        this.setJMenuBar(menuBar);
        menuBar.add(fileMenu);
        fileMenu.add(quitItem);

        quitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
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
