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
//    public ArrayList clientList;

    /*Usernames*/
    public ArrayList userNames;

    /* Anzeige, ob der Server-Dienst weiterhin benoetigt wird */
    public boolean serviceRequested = true;
    private List<TCPWorkerThread> clientThreads;

    /* Konstruktor mit Parametern: Server-Port, Maximale Anzahl paralleler Worker-Threads*/
    public TCPServer(int serverPort, int maxThreads) {
        this.serverPort = serverPort;
        this.workerThreadsSem = new Semaphore(maxThreads);
        clientThreads = new ArrayList<>();
    }


    public void startServer() {
        ServerSocket welcomeSocket; // TCP-Server-Socketklasse
        Socket connectionSocket; // TCP-Standard-Socketklasse

        int nextThreadNumber = 0;



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



            /* Neuen Arbeits-Thread erzeugen und die Nummer, den Socket sowie das Serverobjekt uebergeben */
                nextThreadNumber = intializeClientThread(connectionSocket, nextThreadNumber);


            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }

    private synchronized int intializeClientThread(Socket connectionSocket, int nextThreadNumber) {
        TCPWorkerThread clientThread = new TCPWorkerThread(++nextThreadNumber, connectionSocket, this, userNames);
        clientThreads.add(clientThread);
        clientThread.start();
        return nextThreadNumber;
    }

    protected synchronized void writeToAllClients(String reply) throws IOException {
      /* Sende den String als Antwortzeile (mit CRLF) zu allen Clients */

        for(TCPWorkerThread client : clientThreads){
            client.writeToClient(reply);
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

    public void removeClient(TCPWorkerThread tcpWorkerThread) {
        clientThreads.remove(tcpWorkerThread);
        userNames.remove(tcpWorkerThread.getChatName());

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
    private BufferedReader inFromClient;
    private DataOutputStream outToClient;
    private String chatName;
    private ArrayList _userNames;
    boolean workerServiceRequested = true; // Arbeitsthread beenden?



    boolean loggedIn;

    public TCPWorkerThread(int num, Socket sock, TCPServer server, ArrayList userNames) {
      /* Konstruktor */
        this.name = num;
        this.socket = sock;
        this.server = server;
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
                    if(sentence.contains("/login")){
                        JOptionPane.showMessageDialog(null, "Bitte erst ausloggen", "Bereits eingelogged",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    else if (sentence.startsWith("/quit")){
//                        writeToClient("/username" );
                   //     loggedIn = false;
                        server.removeClient(this);
                        server.writeToAllClients("/members" + _userNames);
                        server.writeToAllClients(chatName + " left chatroom.");
                        workerServiceRequested = false;
                        socket.close();

                    } else {
                        server.writeToAllClients(chatName + ": " + sentence);
                    }
                }
                else{
                    if(sentence.contains("/login")){
                        chatName = sentence.replace("/login", "");
                        if (_userNames.contains(chatName)) {
                            JOptionPane.showMessageDialog(null, "Username vergeben", "Username bereits veregeben",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                        else if(chatName.equals("") || chatName.equals(" ")){
                            JOptionPane.showMessageDialog(null, "Bitte Benutzernamen angeben", "kein Benutzername",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                        else {
                            loggedIn = true;
                            server.writeToAllClients(chatName + "/enterChatroom");
//                            writeToClient(chatName + "/enterChatroom");
                            _userNames.add(chatName);
                            server.writeToAllClients("/members" + _userNames);
//                            writeToClient("/members" + _userNames);
                        }
                    }
                    else if(!sentence.contains("/login")){
                        JOptionPane.showMessageDialog(null, "Bitte erst einloggen", "noch nicht eingelogged",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }

         /* Socket-Streams schliessen --> Verbindungsabbau */
            socket.close();
        } catch (IOException e) {
            System.err.println("Connection aborted by client!");
        } finally {
            System.out.println("TCP Worker Thread " + name + " stopped!");
         /* Platz fuer neuen Thread freigeben */
            server.workerThreadsSem.release();
            server.removeClient(this);
        }
    }

    private String readFromClient() throws IOException {
      /* Lies die naechste Anfrage-Zeile (request) vom Client */
        String request = inFromClient.readLine();
        System.out.println("TCP Worker Thread " + name + " detected job: " + request);

        return request;
    }

    protected synchronized void writeToClient(String reply) throws IOException {
      /* Sende den String als Antwortzeile (mit CRLF) zum Client */
        outToClient.write((reply + '\r' + '\n').getBytes(Charset.forName("UTF-8")));
        System.out.println("TCP Worker Thread " + name +
                " has written the message: " + reply);
    }

    protected String getChatName() {
        return this.chatName;
    }
}
