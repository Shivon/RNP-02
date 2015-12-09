import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Jana on 14.11.2015.
 */
public class Werkzeug {

    private GUI _gui;
    private String _sentence;
    private Socket _socket;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;
    private boolean loggedIn;


    public synchronized boolean isLoggedIn() {
        return loggedIn;
    }

    public Werkzeug(Socket socket) throws IOException {
        _gui = new GUI();
        _socket = socket;
        outToServer = new DataOutputStream(_socket.getOutputStream());
        inFromServer = new BufferedReader(new InputStreamReader(_socket.getInputStream(),  "UTF-8"));
        registriereSend();
        registriereLogin();
        registriereLogout();
        loggedIn = false;
    }
    public synchronized void setLoggedIn(boolean loggedIn){
        this.loggedIn = loggedIn;
    }

    public void registriereLogin(){
        _gui.getLogin().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(loggedIn == false){
                    _sentence = _gui.getWritingField().getText();
                    _sentence = _sentence.replaceFirst(Pattern.quote("Bitte Usernamen angeben: "), "");
                    try {
                        writeToServer("/login" + _sentence);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    System.out.println("in der GUI" + _sentence);
                }
                else {
                    throwLogoutFirstException();
                }
            }
        });
    }

    public void registriereLogout(){
        _gui.getLogout().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(loggedIn){
                        try {
                            writeToServer("/quit");
                               /*Textfeld leeren*/
                            _gui.getWritingField().setText("");
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        loggedIn = false;
                    }
                else{
                    throwLoginFirstException();
                }
            }
        });
    }


    public void registriereSend() {
        _gui.getSend().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                if (!_gui.getWritingField().getText().isEmpty()) {
                    /* Text aus Textfeld lesen, an Server senden*/
                    _sentence = _gui.getWritingField().getText();
                    if(loggedIn) {
                        try {
                            writeToServer("/message" + _sentence);
                               /*Textfeld leeren*/
                            _gui.getWritingField().setText("");
                        } catch (IOException e1) {
                            writeInChatArea("FEHLER: konnte nicht abgeschickt werden");
                        }
                    }
                    else{
                        throwLoginFirstException();
                    }
                        System.out.println("in der GUI" + _sentence);

                } else {
                    JOptionPane.showMessageDialog(null, "keine Nachricht eingegeben", "Keine Nachricht eingegeben",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    public void writeInChatArea(String message){
        System.out.println(message);
                _gui.getChatArea().setText(_gui.getChatArea().getText() + '\r' + '\n' + message);
    }

    public void writeToServer(String request) throws IOException {
        /* Sende eine Zeile (mit CRLF) zum Server */
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

    public void throwNoUserNameException(){
        JOptionPane.showMessageDialog(null, "Bitte Benutzernamen angeben", "kein Benutzername",
                JOptionPane.ERROR_MESSAGE);
    }

    public void throwUserNameNotAvailableException(){
        JOptionPane.showMessageDialog(null, "Username vergeben", "Username bereits veregeben",
                JOptionPane.ERROR_MESSAGE);
    }

    public void throwLogoutFirstException(){
        JOptionPane.showMessageDialog(null, "Bitte erst ausloggen", "Bereits eingelogged",
                JOptionPane.ERROR_MESSAGE);
    }

    public void throwLoginFirstException(){
        JOptionPane.showMessageDialog(null, "Bitte erst einloggen", "noch nicht eingelogged",
                JOptionPane.ERROR_MESSAGE);
    }
}
