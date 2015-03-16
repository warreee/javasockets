import java.io.*;
import java.net.*;

class HTTPServer extends Thread {

    private Socket connectionSocket;
    private CommandParser parser = new CommandParser();

    public HTTPServer(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    public static void main(String args[]) throws Exception {
        // create server (incoming) socket on port 5555
        ServerSocket welcomeSocket = new ServerSocket(5555);

        // wait for a connection to be made to the server socket
        while(true) {

            // create a new HTTPServer thread
            HTTPServer thread = new HTTPServer(welcomeSocket.accept());
            thread.run();

        }
    }

    @Override
    public void run() {

        try {
            // Create inputstream (convenient data reader) to this host.
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

            // Create outputstream (convenient data writer) to this host.
            DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            String line = "";
            String commandString = "";
            boolean stop = false;
            while (! stop && (line = inFromClient.readLine()) != null) {

                if (commandString.endsWith("\r\n\r\n")) { // TODO: nu moet ge 3 keer op enter duwen, zou maar 2 keer moeten zijn (na het ingeven van commando)
                    System.out.println("*** Received command: ***");
                    System.out.print(commandString);
                    // parse command
                    Command command = parser.parseCommand(commandString);
                    // return response to client
                    returnResponse(command.getResponse(), outToClient);
                    // stop?
                    if (command.mustClose())
                        stop = true;
                    commandString = "";
                }

                else {
                    commandString += line + "\r\n";
                }
            }
        }

        catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Return a response to the client.
     */
    public void returnResponse(String response, DataOutputStream outToClient) {
        try {
            outToClient.writeBytes(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
