import com.sun.javafx.collections.ArrayListenerHelper;

import javax.swing.*;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by Jana on 14.11.2015.
 */
public class Main {

    public Main(){

    }

    public static void main(String[] args) {

         /* Test: Erzeuge Client und starte ihn. */
        String host = args[0];
        int port = Integer.parseInt(args[1]);


        /*GUI starten*/
        Werkzeug ui = null;

        try {
            Socket socket = new Socket(host, port);
            ui = new Werkzeug(socket);


            NachrichtenErhaltenThread nachrichtenErhaltenThread = new NachrichtenErhaltenThread(ui, socket);
            nachrichtenErhaltenThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
