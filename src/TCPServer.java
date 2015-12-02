/*
 * TCPServer.java
 *
 * Version 3.1
 * Autor: M. Huebner HAW Hamburg (nach Kurose/Ross)
 * Zweck: TCP-Server Beispielcode:
 *        Bei Dienstanfrage einen Arbeitsthread erzeugen, der eine Anfrage bearbeitet:
 *        einen String empfangen, in Grossbuchstaben konvertieren und zuruecksenden
 *        Maximale Anzahl Worker-Threads begrenzt durch Semaphore
 *  
 */
import javax.swing.*;
import java.io.*;
import java.net.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;


public class TCPServer {
   /* TCP-Server, der Verbindungsanfragen entgegennimmt */

    /* Semaphore begrenzt die Anzahl parallel laufender Worker-Threads  */
    public Semaphore workerThreadsSem;

    /* Portnummer */
    public final int serverPort;

    /* Clients*/
    public ArrayList clientList;

    /*Usernames*/
    public ArrayList userNames;

    /* Anzeige, ob der Server-Dienst weiterhin benoetigt wird */
    public boolean serviceRequested = true;

    /* Konstruktor mit Parametern: Server-Port, Maximale Anzahl paralleler Worker-Threads*/
    public TCPServer(int serverPort, int maxThreads) {
        this.serverPort = serverPort;
        this.workerThreadsSem = new Semaphore(maxThreads);
    }


    public void startServer() {
        ServerSocket welcomeSocket; // TCP-Server-Socketklasse
        Socket connectionSocket; // TCP-Standard-Socketklasse

        int nextThreadNumber = 0;


        clientList = new ArrayList();

        userNames = new ArrayList();


        try {
         /* Server-Socket erzeugen */
            welcomeSocket = new ServerSocket(serverPort);

            while (serviceRequested) {
                workerThreadsSem.acquire();  // Blockieren, wenn max. Anzahl Worker-Threads erreicht

                System.out.println("TCP Server is waiting for connection - listening TCP port " + serverPort);
            /*
             * Blockiert auf Verbindungsanfrage warten --> nach Verbindungsaufbau
             * Standard-Socket erzeugen und an connectionSocket zuweisen
             */


                connectionSocket = welcomeSocket.accept();

                clientList.add(connectionSocket);


            /* Neuen Arbeits-Thread erzeugen und die Nummer, den Socket sowie das Serverobjekt uebergeben */
                (new TCPWorkerThread(++nextThreadNumber, connectionSocket, this, clientList, userNames)).start();


            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    public static void main(String[] args) {
      /* Erzeuge Server und starte ihn */
        if (args.length != 1) {
            System.err.println("Argument fehlt!");
        }
        int port = Integer.parseInt(args[0]);
        TCPServer myServer = new TCPServer(port, 10);
        myServer.startServer();
    }
}

// ----------------------------------------------------------------------------

class TCPWorkerThread extends Thread {
    /*
     * Arbeitsthread, der eine existierende Socket-Verbindung zur Bearbeitung
     * erhaelt
     */
    private int name;
    private Socket socket;
    private TCPServer server;
    private ArrayList<Socket> clientList;
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private String chatName;
    private ArrayList _userNames;
    boolean workerServiceRequested = true; // Arbeitsthread beenden?
    boolean loggedIn;



    public TCPWorkerThread(int num, Socket sock, TCPServer server, ArrayList clientList, ArrayList userNames) {
      /* Konstruktor */
        this.name = num;
        this.socket = sock;
        this.server = server;
        this.clientList = clientList;
        this._userNames = userNames;
        loggedIn = false;
    }

    public void run() {
        String sentence;

        System.out.println("TCP Worker Thread " + name +
                " is running until quit is received!");

        try {
         /* Socket-Basisstreams durch spezielle Streams filtern */
            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            outToClient = new DataOutputStream(socket.getOutputStream());


            /* in dem Textfeld anzeigen lassen*/
            writeToClient("/username");

            while (workerServiceRequested) {


                /*Eingabe des Clients an alle Clients senden*/
                sentence = readFromClient();
                System.out.println("beim Server " + sentence);

                if(loggedIn){
                    if(sentence.startsWith("/login")){
                        JOptionPane.showMessageDialog(null, "Bitte erst ausloggen", "Bereits eingelogged",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    else if (sentence.startsWith("/quit")){
                        loggedIn = false;
                        workerServiceRequested = false;
                        _userNames.remove(chatName);
                        writeToAllClients("/members" + _userNames);
                        writeToAllClients(chatName + " left chatroom.");
                        clientList.remove(socket);
                        writeToClient("/username" );
                    }
                    else {
                        writeToAllClients(chatName + ": " + sentence);
                    }
                }
                else{
                    if(sentence.startsWith("/login")){
                        chatName = sentence.replace("/login", "");
                        if (_userNames.contains(chatName)) {
                            JOptionPane.showMessageDialog(null, "Username vergeben", "Username bereits veregeben",
                                    JOptionPane.ERROR_MESSAGE);
                        } else {
                            loggedIn = true;
                            writeToAllClients("");
                            writeToAllClients(chatName + "  entered the chatroom.");
                            writeToAllClients("");
                            _userNames.add(chatName);
                            writeToAllClients("/members" + _userNames);
                        }
                    }
                    else if(!sentence.startsWith("/login")){
                        JOptionPane.showMessageDialog(null, "Bitte erst einloggen", "noch nicht eingelogged",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
//            /* Test, ob Arbeitsthread beendet werden soll */
//                if (sentence.startsWith("/quit") && loggedIn) {
//                    loggedIn = false;
//                    workerServiceRequested = false;
//                    _userNames.remove(chatName);
//                    writeToAllClients("/members" + _userNames);
//                    writeToAllClients(chatName + " left chatroom.");
//                    clientList.remove(socket);
//                    writeToClient("/login" );
//                }
//                /* Login, Eingabe des Usernames*/
//                else if (sentence.startsWith("/login")&& loggedIn == false) {
//                    chatName = sentence.replace("/login", "");
//                    if (_userNames.contains(chatName)) {
//                        JOptionPane.showMessageDialog(null, "Username vergeben", "Username bereits veregeben",
//                                JOptionPane.ERROR_MESSAGE);
//                    } else {
//                        loggedIn = true;
//                        writeToAllClients("");
//                        writeToAllClients(chatName + "  entered the chatroom.");
//                        writeToAllClients("");
//                        _userNames.add(chatName);
//                        writeToAllClients("/members" + _userNames);
//                    }
//                }
//                else if(sentence.startsWith("/login")&& loggedIn){
//                    JOptionPane.showMessageDialog(null, "Bitte erst ausloggen", "Bereits eingelogged",
//                            JOptionPane.ERROR_MESSAGE);
//                }
//
//                /* Messanges from Client*/
//                else if((!sentence.startsWith("/login")) && loggedIn){
//                    writeToAllClients(chatName + ": " + sentence);
//                }
//                else{
//                    JOptionPane.showMessageDialog(null, "Bitte erst einloggen", "noch nicht eingelogged",
//                            JOptionPane.ERROR_MESSAGE);
//                }
            }

         /* Socket-Streams schliessen --> Verbindungsabbau */
            socket.close();
        } catch (IOException e) {
            System.err.println("Connection aborted by client!");
        } finally {
            System.out.println("TCP Worker Thread " + name + " stopped!");
         /* Platz fuer neuen Thread freigeben */
            server.workerThreadsSem.release();
            clientList.remove(socket);
        }
    }

    private String readFromClient() throws IOException {
      /* Lies die naechste Anfrage-Zeile (request) vom Client */
        String request = inFromClient.readLine();
        System.out.println("TCP Worker Thread " + name + " detected job: " + request);

        return request;
    }

    private void writeToClient(String reply) throws IOException {
      /* Sende den String als Antwortzeile (mit CRLF) zum Client */
        outToClient.write((reply + '\r' + '\n').getBytes(Charset.forName("UTF-8")));
        System.out.println("TCP Worker Thread " + name +
                " has written the message: " + reply);
    }

    private void writeToAllClients(String reply) throws IOException {
      /* Sende den String als Antwortzeile (mit CRLF) zu allen Clients */

       for(Socket client : clientList){
           outToClient = new DataOutputStream(client.getOutputStream());
           outToClient.write((reply + '\r' + '\n').getBytes(Charset.forName("UTF-8")));
           System.out.println(reply);
       }
    }

}
