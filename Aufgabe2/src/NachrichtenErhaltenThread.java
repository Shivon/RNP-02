import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

/**
 * Created by Jana on 15.11.2015.
 */
public class NachrichtenErhaltenThread extends  Thread {

    private Werkzeug _ui;
    private Socket _socket;
    private BufferedReader inFromServer;
    private DataOutputStream outToServer;
    private String sentence;

    public NachrichtenErhaltenThread(Werkzeug ui, Socket socket) {
        super();
        this._ui = ui;
        this._socket = socket;
    }

    @Override
    public void run() {

        try {
            /* Socket-Basisstreams durch spezielle Streams filtern */
            outToServer = new DataOutputStream(_socket.getOutputStream());
            inFromServer = new BufferedReader(new InputStreamReader(
                    _socket.getInputStream()));

            while (true) {
                sentence = readFromServer();
                _ui.writeInChatArea(sentence);
            }
            /* Socket-Streams schliessen --> Verbindungsabbau */
            //_socket.close();
        } catch (IOException e) {
            System.err.println("Connection aborted by server!");
        }
        System.out.println("TCP Client stopped!");
    }
    private String readFromServer() throws IOException {
        /* Lies die Antwort (reply) vom Server */
        String reply = inFromServer.readLine();
        System.out.println("TCP Client got from Server: " + reply);
        return reply;
    }
}
