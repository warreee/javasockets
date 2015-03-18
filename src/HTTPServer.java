import java.io.*;
import java.net.*;

class HTTPServer extends Thread {

    private Socket connectionSocket;
    private CommandParser parser = new CommandParser();

    public HTTPServer(Socket connectionSocket) {
        this.connectionSocket = connectionSocket;
    }

    public static void main(String args[]) throws Exception {
        // create server (incoming) socket on port 6789
        ServerSocket welcomeSocket = new ServerSocket(6789);

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

            String commandString = "";
            boolean stop = false;
            while (! stop) {

                String line = inFromClient.readLine();

                if (line == null) {
                    stop = true;
                }

                else {

                    if (! (commandString.replace("\r", "").replace("\n", "").isEmpty() && line.isEmpty()) ) {
                        // add the read line to commandString
                        commandString += line + "\r\n";
                    }


                    if (!commandString.replace("\r", "").replace("\n", "").isEmpty()
                            && !parser.continueReading(commandString)) {

                        // parse command and return response

                        System.out.println("*** Received command: ***");
                        System.out.print(commandString);
                        // parse command
                        Command command = parser.parseCommand(commandString);
                        // return response to client
                        byte[] responseBytes = command.getResponse();
                        returnResponse(responseBytes, outToClient);

                        System.out.println("*** Returned this to the client: ***");
                        System.out.println(new String(responseBytes));

                        // stop?
                        if (command.mustClose())
                            stop = true;
                        commandString = "";

                    }

                }

            }

            connectionSocket.close();
        }

        catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Return a response to the client.
     */
    public void returnResponse(byte[] response, DataOutputStream outToClient) {
        try {
            outToClient.write(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
