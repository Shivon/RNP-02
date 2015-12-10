import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * The server that can be run both as a console application or a GUI
 */
public class Server {
    // a unique ID for each connection
    private static int uniqueId;
    // an ArrayList to keep the list of the Client
    private ArrayList<ClientThread> clientThreadList;
    // ServerGUI object which is only set if I am in the server GUI
    private ServerGUI serverGUI;
    // to display time
    private SimpleDateFormat displayTime;
    // the port number to listen for connection
    private int port;
    // the boolean that will be turned of to stop the server
    private boolean keepGoing;


    /*
     *  server constructor that receive the port to listen to for connection as parameter
     *  in console
     */
    public Server(int port) {
        this(port, null);
    }

    public Server(int port, ServerGUI serverGUI) {
        // server GUI or not
        this.serverGUI = serverGUI;
        // the port
        this.port = port;
        // to display hh:mm:ss
        displayTime = new SimpleDateFormat("HH:mm:ss");
        // ArrayList for the Client list
        clientThreadList = new ArrayList<ClientThread>();
    }


    public void start() {
        keepGoing = true;
	      // create socket server and wait for connection requests
        try
        {
            // the socket used by the server
            ServerSocket serverSocket = new ServerSocket(port);

            // infinite loop to wait for connections
            while(keepGoing)
            {
                // format message saying we are waiting
                display("Server waiting for Clients on port " + port + ".");
                // accept connection
                Socket socket = serverSocket.accept();
                // if I was asked to stop
                if(!keepGoing)
                    break;
                // make a thread of it
                ClientThread clientThread = new ClientThread(socket);
                // save it in the ArrayList
                clientThreadList.add(clientThread);
                clientThread.start();
            }
            // I was asked to stop
            try {
                serverSocket.close();
                for(int i = 0; i < clientThreadList.size(); ++i) {
                    ClientThread clientThread = clientThreadList.get(i);
                    try {
                        clientThread.sInput.close();
                        clientThread.sOutput.close();
                        clientThread.socket.close();
                    }
                    catch(IOException ioE) {
                        // not much I can do
                    }
                }
            }
            catch(Exception e) {
                display("Exception closing the server and clients: " + e);
            }
        }
        // something went bad
        catch (IOException e) {
            String msg = displayTime.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
            display(msg);
        }
    }

    /*
     * For the GUI to stop the server
     */
    protected void stop() {
        keepGoing = false;
        // connect to myself as Client to exit statement
        // Socket socket = serverSocket.accept();
        try {
            new Socket("localhost", port);
        }
        catch(Exception e) {
            // nothing I can really do
        }
    }

    /*
     * Display an event (not a message) to the console or the GUI
     */
    private void display(String msg) {
        String time = displayTime.format(new Date()) + " " + msg;
        if(serverGUI == null)
            System.out.println(time);
        else
            serverGUI.appendEvent(time + "\n");
    }

    /*
     *  to broadcast a message to all Clients
     */
    private synchronized void broadcast(String message) {
        // add HH:mm:ss and \n to the message
        String time = displayTime.format(new Date());
        String messageLf = time + " " + message + "\n";
        // display message on console or GUI
        if(serverGUI == null)
            System.out.print(messageLf);
        else
            serverGUI.appendRoom(messageLf);     // append in the room window

        // we loop in reverse order in case we would have to remove a Client
        // because it has disconnected
        for(int i = clientThreadList.size(); --i >= 0;) {
            ClientThread ct = clientThreadList.get(i);
            // try to write to the Client if it fails remove it from the list
            if(!ct.writeMsg(messageLf)) {
                clientThreadList.remove(i);
                display("Disconnected Client " + ct.username + " removed from list.");
            }
        }
    }

//    // to get a list if all logged in users
//    public synchronized ArrayList<String> getUserList(){
//        ArrayList<String> users = new ArrayList<>();
//        for(int i = clientThreadList.size(); --i >= 0;) {
//            ClientThread ct = clientThreadList.get(i);
//            // try to write to the Client if it fails remove it from the list
//            if(!ct.writeMsg("ping")) {
//                clientThreadList.remove(i);
//                display("Disconnected Client " + ct.username + " removed from list.");
//            } else {
//                users.add(ct.username);
//            }
//        }
//        return users;
//    }

    // for a client who logoff using the LOGOUT message
    synchronized void remove(int id) {
        // scan the array list until we found the Id
        for(int i = 0; i < clientThreadList.size(); ++i) {
            ClientThread ct = clientThreadList.get(i);
            // found it
            if(ct.id == id) {
                clientThreadList.remove(i);
                return;
            }
        }
    }

    /*
     *  To run as a console application just open a console window and:
     * > java Server
     * > java Server portNumber
     * If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        // start server on port 1500 unless a PortNumber is specified
        int portNumber = 1500;
        switch(args.length) {
            case 1:
                try {
                    portNumber = Integer.parseInt(args[0]);
                }
                catch(Exception e) {
                    System.out.println("Invalid port number.");
                    System.out.println("Usage is: > java Server [portNumber]");
                    return;
                }
            case 0:
                break;
            default:
                System.out.println("Usage is: > java Server [portNumber]");
                return;

        }
        // create a server object and start it
        Server server = new Server(portNumber);
        server.start();
    }

    /** One instance of this thread will run for each client */
    class ClientThread extends Thread {
        // the socket where to listen/talk
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        // my unique id (easier for deconnection)
        int id;
        // the Username of the Client
        String username;
        // the only type of message a will receive
        ChatMessage cm;
        // the date I connect
        String date;

        // Constructors
        ClientThread(Socket socket) {
            // a unique id
            id = ++uniqueId;
            this.socket = socket;
			/* Creating both Data Stream */
            System.out.println("Thread trying to create Object Input/Output Streams");
            try
            {
                // create output first
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput  = new ObjectInputStream(socket.getInputStream());
                // read the username
                username = (String) sInput.readObject();
                display(username + " just connected.");
            }
            catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            }
            // have to catch ClassNotFoundException
            // but I read a String, I am sure it will work
            catch (ClassNotFoundException e) {
            }
            date = new Date().toString() + "\n";
        }

        // what will run forever
        public void run() {
            // to loop until LOGOUT
            boolean keepGoing = true;
            while(keepGoing) {
                // TODO: insert userlist here
                // ArrayList<String> userList = getUserList();

                // read a String (which is an object)
                try {
                    cm = (ChatMessage) sInput.readObject();
                }
                catch (IOException e) {
                    display(username + " Exception reading Streams: " + e);
                    break;
                }
                catch(ClassNotFoundException e2) {
                    break;
                }
                // the message part of the ChatMessage
                String message = cm.getMessage();

                // Switch on the type of message receive
                switch(cm.getType()) {

                    case ChatMessage.MESSAGE:
                        broadcast(username + ": " + message);
                        break;
                    case ChatMessage.LOGOUT:
                        display(username + " disconnected with a LOGOUT message.");
                        keepGoing = false;
                        break;
                    case ChatMessage.WHOISIN:
                        ArrayList<String> users = new ArrayList<>();

                        for(int i = 0; i < clientThreadList.size(); ++i) {
                            ClientThread ct = clientThreadList.get(i);
                            users.add(ct.username.toString());
                        }

                        sendUserList(users);

                        break;
//                        writeMsg("List of the users connected at " + displayTime.format(new Date()) + "\n");
//                        // scan clientThreadList the users connected
//                        for(int i = 0; i < clientThreadList.size(); ++i) {
//                            ClientThread ct = clientThreadList.get(i);
//                            writeMsg((i+1) + ") " + ct.username + " since " + ct.date);
//                        }
//                        break;
                }
            }
            // remove myself from the arrayList containing the list of the
            // connected Clients
            remove(id);
            close();
        }

        // try to close everything
        private void close() {
            // try to close the connection
            try {
                if(sOutput != null) sOutput.close();
            }
            catch(Exception e) {}
            try {
                if(sInput != null) sInput.close();
            }
            catch(Exception e) {};
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        /*
         * Write a String to the Client output stream
         */
        private boolean writeMsg(String msg) {
            // if Client is still connected send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }
            // write the message to the stream
            try {
                sOutput.writeObject(msg);
            }
            // if an error occurs, do not abort just inform the user
            catch(IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }

        /*
         * Write an ArrayString<> to the Client output stream
         */
        private boolean sendUserList(ArrayList users) {
            // if Client is still connected send the message to it
            if(!socket.isConnected()) {
                close();
                return false;
            }

            // write the message to the stream
            try {
                sOutput.writeObject(users);
            }
            // if an error occurs, do not abort just inform the user
            catch(IOException e) {
                display("Error sending message to " + username);
                display(e.toString());
            }
            return true;
        }
    }
}