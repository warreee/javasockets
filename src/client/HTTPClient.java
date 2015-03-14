package client;

import org.w3c.dom.html.HTMLParagraphElement;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

/**
 * An HTTP client that can send HEAD, GET, PUT and POST commands with HTTP/1.0 and HTTP/1.1
 */
public class HTTPClient {

    /**
     * Constructor for creating a new HTTP client.
     * @param command HEAD/GET/PUT/POST
     * @param uri URI object
     * @param port Ususally 80
     * @param http1 True if HTTP 1.1 is used, false if HTTP 1.0 is used
     */
    public HTTPClient(String command, URI uri, int port, boolean http1) throws IOException {
        String path = uri.getPath();
        String host = uri.getHost();

        switch (command) {
            case "GET":
                get(host, path, port, http1);
                break;
            // TODO
        }
    }

    /**
     * Send a GET command.
     */
    private void get(String host, String path, int port, boolean http1) throws IOException {
        // connect to host
        Socket clientSocket = new Socket(host, port);
        // create outputstream to this host
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        // create an inputstream to this host
        DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());

        // get content of requested file
        List<String> reponse = get(host,path,outToServer,inFromServer,http1);
    }

    /**
     * Returns the response of a server after sending a GET request.
     */
    private List<String> get(String host, String path, DataOutputStream outputStream, DataInputStream inputStream, boolean http1) throws IOException {
        if (http1)
            outputStream.writeBytes("GET " + path + " HTTP/1.1\r\n" +
                "HOST: " + host + "\r\n\r\n");
        else
            outputStream.writeBytes("GET " + path + " HTTP/1.0\r\n\r\n");

        Byte responseByte;
        String line = "";
        ArrayList<String> response = new ArrayList<>();

        int i=0;
        int nbBytes = Integer.MAX_VALUE; // start reading until the head part of the response ends
        int contentLength = 0; // the content length will be set as soon as it is received

        while (i <= nbBytes) {

            // read the next byte
            responseByte = inputStream.readByte();

            // convert byte to string
            byte[] responseBytes = new byte[1];
            responseBytes[0] = responseByte;
            String add = new String(responseBytes, "UTF-8");

            // add string to the current line
            line += add;

            // add string to the response string
            response.add(add);

            if (line.endsWith("\r\n")) {

                // if the content length was received, save it
                if (line.startsWith("Content-Length: ")) {
                    String nb = line.replace("Content-Length: ","").replace("\r\n","");
                    contentLength = Integer.parseInt(nb);
                }

                // when the content of the file starts,
                // make sure we only read the next [contentLength] bytes
                if (line.equals("\r\n") && nbBytes == Integer.MAX_VALUE) {
                    i=0;
                    nbBytes = contentLength;
                }

                line = "";

            }

            i++;
        }

        return response;
    }

    /**
     * Get the file content of the reponse of a server.
     */
    private String getContent(String[] response) {

    }

    private List<URI> getEmbeddedObjects(String content) {

    }

    //////////////////////////////////////////////////STATIC////////////////////////////////////////////////////////////

    public static void main(String[] args) throws URISyntaxException, IOException {
        // if the arguments are invalid, then print the description of how to specify the program arguments
        if (! validArguments(args)) {
            printHelp();
        }

        else {
            // get arguments
            String command = args[0];
            String uriString = args[1];
            String portString = args[2];
            String version = args[3];

            // get URI object from uriString
            URI uri = getURI(uriString);

            // get port int
            int port = Integer.parseInt(portString);

            boolean http1;
            if (version.equals("1.1"))
                http1 = true;
            else
                http1 = false;

            // create new HTTP client
            HTTPClient client = new HTTPClient(command,uri,port,http1);
        }
    }

    /**
     * Print the description of how to specify the program arguments.
     */
    public static void printHelp() {
        System.out.println("The arguments that you entered were wrong.");
        System.out.println("Use one of these commands:");
        System.out.println("HEAD url (starting with or without http) port (usuallly 80) httpversion (1.0 or 1.1)");
        System.out.println("GET url port httpversion");
        System.out.println("PUT url port httpversion");
        System.out.println("POST url port httpversion");
    }

    /**
     * Get URI object from given URI string
     * @param uriString String value of the given URI
     */
    private static URI getURI(String uriString) throws URISyntaxException {
        if (! uriString.startsWith("http://") && ! uriString.startsWith("https://")) {
            uriString = "http://" + uriString;
        }
        return new URI(uriString);
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
