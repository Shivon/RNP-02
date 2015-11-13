import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Created by Jana  on 13.11.2015.
 */
public class GUI {



    public static void GUI(){

    }

    public static void main(String[] args) {
        chatWindow();
    }

    private static void chatWindow(){
        JFrame frame = new JFrame();
        JPanel chatPanel = new JPanel();
        JPanel writingPanel = new JPanel();
        JTextArea writingField = new JTextArea();
        JTextArea chatArea = new JTextArea();
        JScrollPane chatPane = new JScrollPane(chatArea);
        JButton send = new JButton("send");
        JPanel buttonPanel = new JPanel();


        chatPanel.setBorder(new EmptyBorder(2, 3, 2, 3));
        chatPanel.setPreferredSize(new Dimension(400,300));
        writingPanel.setBorder(new EmptyBorder(2,3,2,3));
        writingPanel.setPreferredSize(new Dimension(400, 200));
        buttonPanel.setBorder(new EmptyBorder(2,3,2,3));

        chatPane.setPreferredSize(new Dimension(400,300));
        chatArea.setBackground(Color.DARK_GRAY);
        chatArea.setForeground(Color.YELLOW);
        chatArea.setWrapStyleWord(true);
        chatArea.setLineWrap(true);
        chatArea.setPreferredSize(new Dimension(400,10000));
        chatPanel.add(chatPane, BorderLayout.CENTER);

        writingField.setPreferredSize(new Dimension(400,200));
        writingField.setBackground(Color.CYAN);
        writingField.setWrapStyleWord(true);
        writingField.setLineWrap(true);
        writingPanel.add(writingField, BorderLayout.CENTER);

        buttonPanel.add(send);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450,600);


        frame.add(chatPanel, BorderLayout.NORTH);
        frame.add(writingPanel, BorderLayout.CENTER);
        frame.add(send, BorderLayout.PAGE_END);

        frame.setVisible(true);
    }
}