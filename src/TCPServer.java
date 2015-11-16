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
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;


public class TCPServer {
   /* TCP-Server, der Verbindungsanfragen entgegennimmt */

    /* Semaphore begrenzt die Anzahl parallel laufender Worker-Threads  */
    public Semaphore workerThreadsSem;

    /* Portnummer */
    public final int serverPort;

    /* Clients*/
    public ArrayList clientList;

    public TCPWorkerThread workerThread;

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
                (new TCPWorkerThread(++nextThreadNumber, connectionSocket, this, clientList)).start();


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
    private  String chatName;
    boolean workerServiceRequested = true; // Arbeitsthread beenden?
    boolean registration = true;

    public TCPWorkerThread(int num, Socket sock, TCPServer server, ArrayList clientList) {
      /* Konstruktor */
        this.name = num;
        this.socket = sock;
        this.server = server;
        this.clientList = clientList;
    }

    public void run() {
        String sentence;

        System.out.println("TCP Worker Thread " + name +
                " is running until quit is received!");

        try {
         /* Socket-Basisstreams durch spezielle Streams filtern */
            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToClient = new DataOutputStream(socket.getOutputStream());

            /* in dem Textfeld anzeigen lassen*/
            writeToClient("your chatname: " );

            while (registration) {
            /* aus dem Textfeld lesen*/
                chatName = readFromClient();

                System.out.println("chatname beim Server " + chatName);

            /*Info, dass neuer Client im Chatroom an alle Clients (ChatArea)*/
                writeToAllClients(" entered the chatroom.");
                    registration = false;
            }

            while (workerServiceRequested) {
                /*Eingabe des Clients an alle Clients senden*/
                sentence = readFromClient();
                System.out.println("beim Server " + sentence);

            /* Test, ob Arbeitsthread beendet werden soll */
                if (sentence.startsWith("/quit")) {
                    workerServiceRequested = false;

                    writeToAllClients(" left chatroom.");
                }
                else{
                    writeToAllClients(": " + sentence);
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
        outToClient.writeBytes(reply + '\r' + '\n');
        System.out.println("TCP Worker Thread " + name +
                " has written the message: " + reply);
    }

    private void writeToAllClients(String reply) throws IOException {
      /* Sende den String als Antwortzeile (mit CRLF) zu allen Clients */

       for(Socket client : clientList){
           outToClient = new DataOutputStream(client.getOutputStream());
           reply = chatName + reply;
           outToClient.writeBytes(reply + '\r' + '\n');
           System.out.println(reply);
       }
    }
}
