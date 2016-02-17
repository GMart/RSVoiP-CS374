import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Garrett on 2/11/2016.
 */
public class mainForm {
    private JMenuBar MenuBar;
    private JList contactsList;
    private JScrollPane ContactsScroll;
    private JPanel rootPanel;
    private JButton callButton;
    private JButton sendFileButton;
    private JLabel userLabel;
    private JMenu fileMenu;
    private JMenuItem quitItem;

    public void main(String[] args) {
        JFrame frame = new JFrame("mainForm");
        frame.setContentPane(new mainForm().rootPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();

        frame.setJMenuBar(MenuBar);
        MenuBar.add(fileMenu);
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
        frame.setVisible(true);


    }

}
