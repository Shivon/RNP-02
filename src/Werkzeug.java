import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Jana on 14.11.2015.
 */
public class Werkzeug {

    private GUI _gui;
    private String _sentence;
    private  Socket _socket;
//    private boolean messageSend = false;
    private DataOutputStream outToServer;

    public Werkzeug(Socket socket) throws IOException {
        _gui = new GUI();
        _socket = socket;
        outToServer = new DataOutputStream(_socket.getOutputStream());
        registriereSend();
    }

    public void registriereSend() {
        _gui.getSend().addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (!_gui.getWritingField().getText().isEmpty()) {
                    /* Text aus Textfeld lesen, an Server senden*/

                    _sentence = _gui.getWritingField().getText();
                    System.out.println("in der GUI" + _sentence);

                    try {
                        writeToServer(_sentence);
                    } catch (IOException e1) {
                        writeInChatArea("FEHLER: konnte nicht abgeschickt werden");
                    }

                    /*Textfeld leeren*/
                    _gui.getWritingField().setText("");

//                    messageSend = true;
                } else {
                    JOptionPane.showMessageDialog(null, "keine Nachricht eingegeben", "Keine Nachricht eingegeben",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
//
//    public Boolean registeredMessageSend(){
//        System.out.println(messageSend);
//        return messageSend;
//    }
//
//    public String getUserInput(){
//
//                    /*Textfeld leeren*/
//        _gui.getWritingField().setText("");
//        System.out.println(_sentence);
//        return _sentence;
//    }

    public void writeInChatArea(String message){
        System.out.println(message);
        _gui.getChatArea().setText(_gui.getChatArea().getText()+ '\r' + '\n'+ message);

    }

//    public void writeInTextArea(String message){
//        _gui.getWritingField().setText(message);
//        _gui.getWritingField().setCaretPosition(_gui.getWritingField().getText().length());
//        _gui.getWritingField().getCaret().setVisible(true);
//    }

    private void writeToServer(String request) throws IOException {
        /* Sende eine Zeile (mit CRLF) zum Server */
        outToServer.writeBytes(request + '\r' + '\n');
        System.out.println("TCP Client has sent the message: " + request);
    }
}
