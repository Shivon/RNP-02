import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jana on 14.11.2015.
 */
public class Werkzeug {

    private GUI _gui;
    private String _sentence;
    private Socket _socket;
    private DataOutputStream outToServer;
    private boolean loggedIn;

    public Werkzeug(Socket socket) throws IOException {
        _gui = new GUI();
        _socket = socket;
        outToServer = new DataOutputStream(_socket.getOutputStream());
        registriereSend();
        loggedIn = false;
    }

    public void registriereSend() {
        _gui.getSend().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (!_gui.getWritingField().getText().isEmpty()) {
                    /* Text aus Textfeld lesen, an Server senden*/
                    _sentence = _gui.getWritingField().getText();

                    if(_sentence.contains("/login") && loggedIn == false){
                        loggedIn = true;
                        _sentence = _sentence.replace("Bitte Usernamen angeben: ", "");
                        try {
                            writeToServer(_sentence);
                               /*Textfeld leeren*/
                            _gui.getWritingField().setText("");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        System.out.println("in der GUI" + _sentence);
                    }
//                    else if(_sentence.contains("/quit") && loggedIn){
//                        try {
//                            writeToServer(_sentence);
//                               /*Textfeld leeren*/
//                            _gui.getWritingField().setText("");
//                        } catch (IOException e1) {
//                            e1.printStackTrace();
//                        }
//                        loggedIn = false;
//                    }
                    else{
                        try {
                            writeToServer(_sentence);
                               /*Textfeld leeren*/
                            _gui.getWritingField().setText("");
                        } catch (IOException e1) {
                            writeInChatArea("FEHLER: konnte nicht abgeschickt werden");
                        }
                        System.out.println("in der GUI" + _sentence);
                    }

                } else {
                    JOptionPane.showMessageDialog(null, "keine Nachricht eingegeben", "Keine Nachricht eingegeben",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public void writeInChatArea(String message){
        System.out.println(message);
        if(loggedIn) {
                _gui.getChatArea().setText(_gui.getChatArea().getText() + '\r' + '\n' + message);
        }
    }

    private void writeToServer(String request) throws IOException {
        /* Sende eine Zeile (mit CRLF) zum Server */
//        outToServer.writeBytes(request + '\r' + '\n');
        outToServer.write((request + '\r' + '\n').getBytes(Charset.forName("UTF-8")));
        System.out.println("TCP Client has sent the message: " + request);
    }

    public void writeInMemberField(String member) throws IOException{
        _gui.getMemberField().setText(member);
    }

    public void writeInWritingField(String message){
        _gui.getWritingField().setText(message);
        _gui.getWritingField().setCaretPosition(message.length());
    }

    public void shutDown() {
        _gui.get_frame().setVisible(false);
        _gui.get_frame().dispose();
    }
}
