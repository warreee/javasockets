import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

/*
* A simple HTTP Client application
*
* Computer Networks, KU Leuven.
*
* Arne De Brabandere
* Ward Schodts
*/
class HTTPClient {

    /**
     * Log file: log.txt
     */
    public static LogFile logFile = new LogFile("log.txt");

    public static void main(String[] args) throws Exception {

        // if the arguments are invalid, then print the description of how to specify the program arguments
        if (! validArguments(args)) {
            printHelp();
        } else {
            // add command string to log file
            logFile.addLine("\n" + "Command:" + "\n\n" + args[0] + " " + args[1] + " " + args[2] + " " + args[3]);

            // get arguments
            String command = args[0];
            String uriString = args[1];
            String portString = args[2];
            String version = args[3];

            // get URI object from uriString
            URI uri = getURI(uriString);

            // get port int
            int port = Integer.parseInt(portString);

            executeCommand(command, uri, port, version);

            // add separator to log file
            logFile.addLine("--------------------------------------------------------------------------------------");
        }
    }

    /**
    * Print the description of how to specify the program arguments.
    */
    public static void printHelp() {
        // TODO
        System.out.println("The argument that you entered were wrong:");
        System.out.println("HEAD url(starting with or without http) port(usuallly 80) httpversion(1.0 or 1.1)");
        System.out.println("GET url port httpversion");
        System.out.println("PUT url port httpversion");
        System.out.println("POST url port httpversion");
    }

    /**
    * Get URI object from given URI string
    * @param uriString String value of the given URI
    */
    private static URI getURI(String uriString) throws Exception {
        if (! uriString.startsWith("http://") && ! uriString.startsWith("https://")) {
            uriString = "http://" + uriString;
        }
        return new URI(uriString);
    }

    /**
    * Execute the command.
    * @param command command string
    * @param uri URI object
    * @param port port number
    * @param version http version (1.0 or 1.1)
    */
    private static void executeCommand(String command, URI uri, int port, String version) throws Exception {

        String path = uri.getPath(); // path to file
        String host = uri.getHost();

        // Connect to the host.
        Socket clientSocket = null;
        try {
            clientSocket = new Socket(host, port);
        } catch (IOException e) {
            System.out.println("Unable to connect to " + host + ":" + port);
        }

        // Create outputstream (convenient data writer) to this host.
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

        // Create an inputstream (convenient data reader) to this host
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        // Parse command.
        try {
            switch (command) {
                case "HEAD":
                    head(inFromServer, outToServer, path, host, version);
                    break;
                /*case "GET":
                    get(inFromServer, outToServer, path, host, version);
                    break;
                case "PUT":
                    put(inFromServer, outToServer, path, host, version);
                    break;
                case "POST":
                    post(inFromServer, outToServer, path, host, version);
                    break;*/
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        // Close the socket.
        clientSocket.close();
    }

    private static void head(BufferedReader inFromServer, DataOutputStream outToServer, String path, String host, String version) throws Exception {
        // Send HTTP command to server.
        if(version.equals(1.0)) {
            outToServer.writeBytes("HEAD " + path + " HTTP/" + version + "\r\n\r\n");
        } else {
            outToServer.writeBytes("HEAD " + path + " HTTP/" + version + "\r\n" +
                                    "HOST: " + host + "\r\n\r\n");
        }
        logFile.addLine("\n" + "Response:" + "\n");

        // Read text from the server
        String response = "";
        while ((response = inFromServer.readLine()) != null) {
            // print response to screen
            System.out.println(response);
            // write response to log file
            logFile.addLine(response);
        }
    }

    /**
     * Check if the arguments are valid.
     */
    public static boolean validArguments(String[] arguments) {
        if (arguments.length != 4)
            return false;
        if (! arguments[0].equals("HEAD") && ! arguments[0].equals("GET") && ! arguments[0].equals("PUT") && ! arguments[0].equals("POST"))
            return false;
        if (! isInteger(arguments[2]))
            return false;
        if (! arguments[3].equals("1.0") && ! arguments[3].equals("1.1"))
            return false;
        return true;
    }

    /**
     * Check if a string is an integer.
     */
    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

}
