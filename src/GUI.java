import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by Jana  on 13.11.2015.
 */
public class GUI {

    private JFrame _frame;
    private JPanel _chatPanel;
    private JPanel _writingPanel;
    private JTextArea _writingField;
    private JTextArea _chatArea;
    private JTextArea _memberField;
    private JScrollPane _chatPane;
    private JButton _send;
    private JButton _login;
    private JButton _logout;
    private JPanel _buttonPanel;

    public GUI(){
        _frame = new JFrame();
        _chatPanel = new JPanel();
        _writingPanel = new JPanel();
        _writingField = new JTextArea();
        _chatArea = new JTextArea();
        _memberField = new JTextArea();
        _chatPane = new JScrollPane(_chatArea);
        _send = new JButton("send");
        _login = new JButton("login");
        _logout = new JButton("logout");
        _buttonPanel = new JPanel();

        _chatPanel.setBorder(new EmptyBorder(2, 3, 2, 3));
        _chatPanel.setPreferredSize(new Dimension(400,300));
        _writingPanel.setBorder(new EmptyBorder(2,3,2,3));
        _writingPanel.setPreferredSize(new Dimension(550, 200));
        _buttonPanel.setBorder(new EmptyBorder(2,3,2,3));

        _chatPane.setPreferredSize(new Dimension(400,300));
        _chatArea.setBackground(Color.DARK_GRAY);
        _chatArea.setForeground(Color.YELLOW);
        _chatArea.setWrapStyleWord(true);
        _chatArea.setLineWrap(true);
        _chatArea.setPreferredSize(new Dimension(400,10000));
        _chatArea.setEnabled(false);
        _chatPanel.add(_chatPane, BorderLayout.CENTER);

        _memberField.setPreferredSize(new Dimension(100,300));
        _memberField.setBackground(Color.BLUE);
        _memberField.setForeground(Color.YELLOW);
        _memberField.setWrapStyleWord(true);
        _memberField.setLineWrap(true);
        _memberField.setEnabled(false);
        _chatPanel.add(_memberField, BorderLayout.CENTER);

        _writingField.setPreferredSize(new Dimension(500,200));
        _writingField.setBackground(Color.CYAN);
        _writingField.setWrapStyleWord(true);
        _writingField.setLineWrap(true);
        _writingPanel.add(_writingField, BorderLayout.CENTER);

        _buttonPanel.add(_login, BorderLayout.WEST);
        _buttonPanel.add(_send, BorderLayout.CENTER);
        _buttonPanel.add(_logout, BorderLayout.EAST);

        _frame.setSize(550,600);

        _frame.add(_chatPanel, BorderLayout.NORTH);
        _frame.add(_writingPanel, BorderLayout.CENTER);
        _frame.add(_buttonPanel, BorderLayout.PAGE_END);

        _frame.setVisible(true);
        _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        System.out.println("hallo");
    }

    public JButton getSend() {
        return _send;
    }

    public JButton getLogin(){ return _login;}

    public JButton getLogout() { return _logout;}

    public JTextArea getChatArea() {
        return _chatArea;
    }

    public JTextArea getWritingField() {

        return _writingField;
    }

    public JTextArea getMemberField(){
        return _memberField;
    }

    public JFrame get_frame(){ return _frame;}
}
