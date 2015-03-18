import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/*
* A simple HTTP Client application
*
* Computer Networks, KU Leuven.
*
* Arne De Brabandere
* Ward Schodts
*/
class HTTPClient {

    //////////////////////////////////////////////////MAIN//////////////////////////////////////////////////////////////

    public static void main(String[] args) throws URISyntaxException, IOException, ClassNotFoundException {
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
            URI uri = Helpers.getURI(uriString);

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
     * Check if the arguments are valid.
     */
    public static boolean validArguments(String[] arguments) {
        if (arguments.length != 4)
            return false;
        if (! arguments[0].equals("HEAD") && ! arguments[0].equals("GET") && ! arguments[0].equals("PUT") && ! arguments[0].equals("POST"))
            return false;
        if (! Helpers.isInteger(arguments[2]))
            return false;
        if (! arguments[3].equals("1.0") && ! arguments[3].equals("1.1"))
            return false;
        return true;
    }


    ///////////////////////////////////////////CLIENT-OBJECT////////////////////////////////////////////////////////////


    /**
     * Map with all requested URI's (with the GET command)
     * and the corresponding date (in String format) it was last modified.
     */
    public HashMap<String,String> requestedURIs = new HashMap<>();

    /**
     * Constructor for creating a new HTTP client.
     * @param command HEAD/GET/PUT/POST
     * @param uri URI object
     * @param port Ususally 80
     * @param http1 True if HTTP 1.1 is used, false if HTTP 1.0 is used
     */
    public HTTPClient(String command, URI uri, int port, boolean http1) throws IOException, URISyntaxException, ClassNotFoundException {

        // load requestedURIs from file
        requestedURIs = Helpers.readRequestedURIs();

        // get host and path
        String path = uri.getPath();
        String host = uri.getHost();

        // execute the right method for the given command
        switch (command) {
            case "HEAD":
                head(host, path, port, http1);
                break;
            case "GET":
                get(host, path, port, http1);
                break;
            case "PUT":
                put(host, path, port, http1);
                break;
            case "POST":
                post(host, path, port, http1);
                break;
        }

        // save requestedURIs in file
        Helpers.saveRequestedURIs(requestedURIs);

    }

    ///////////////////////////////////////////////////HEAD////////////////////////////////////////////////////////////

    /**
     * Send a HEAD command and print the response.
     */
    private void head(String host, String path, int port, boolean http1) throws IOException {
        // connect to host
        Socket clientSocket = new Socket(host, port);
        // create outputstream to this host
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        // create an inputstream to this host
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // buffered reader is easier here

        // Send HTTP command to server.
        String request = "";
        if (http1)
            request = "HEAD " + path + " HTTP/1.1" + "\r\n" +
                    "Host: " + host;
        else
            request = "HEAD " + path + " HTTP/1.0";

        outToServer.writeBytes(request + "\r\n\r\n");
        System.out.println("*** Request sent: ***");
        System.out.println(request);

        System.out.println("*** Response: ***");
        // Read text from the server
        String response = "";
        while ((response = inFromServer.readLine()) != null) {
            // print response to screen
            System.out.println(response);
        }

        clientSocket.close();
    }

    ///////////////////////////////////////////////////GET//////////////////////////////////////////////////////////////

    /**
     * Send a GET command,
     * find the embedded object URI's and send GET commands for the embedded objects.
     *
     * @return The response of the server in bytes, as was received by getBytes(...)
     */
    private byte[] get(String host, String path, int port, boolean http1) throws IOException, URISyntaxException {

        // connect to host
        Socket clientSocket = new Socket(host, port);
        // create outputstream to this host
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        // create an inputstream to this host
        DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());

        // get content of requested file in bytes (this will be returned by this function)
        byte[] responseBytes = getResponseBytes(host, path, outToServer, inFromServer, http1);

        // bytes -> list of strings (line per line)
        List<String> response = Helpers.getLines(responseBytes);

        // print the response
        System.out.println("*** Response: ***");
        for (String line : response)
            System.out.print(line);
        // also store the response file locally
        if (! path.isEmpty())
            Helpers.writeToFile(host, path, Helpers.getContent(responseBytes));
        else
            Helpers.writeToFile(host, "/index.html", Helpers.getContent(responseBytes));

        // if HTTP 1.1 was used, then get the embedded objects in the *same* socket connection
        if (http1 && ! Helpers.getEmbeddedObjects(Helpers.getContent(response)).isEmpty()) {
            for (String uri : Helpers.getEmbeddedObjects(Helpers.getContent(response))) {
                String host2 = Helpers.getHost2(host, path, uri);
                String path2 = Helpers.getPath2(path, uri);
                if (host2.equals(host)) {
                    // only get embedded objects on the same host
                    byte[] embeddedObjectContent = Helpers.getContent(getResponseBytes(host2, path2, outToServer, inFromServer, http1)); // in same connection
                    // write to file
                    Helpers.writeToFile(host2, path2, embeddedObjectContent);
                }
            }
        }

        // close the socket
        clientSocket.close();

        // if HTTP 1.0 was used, then get the embedded objects in a *new* socket connection
        if (! http1 && ! Helpers.getEmbeddedObjects(Helpers.getContent(response)).isEmpty()) {
            for (String uri : Helpers.getEmbeddedObjects(Helpers.getContent(response))) {
                String host2 = Helpers.getHost2(host, path, uri);
                String path2 = Helpers.getPath2(path, uri);
                if (host2.equals(host)) {
                    // only get embedded objects on the same host
                    byte[] embeddedObjectContent = Helpers.getContent(get(host2, path2, port, http1));
                    // write to file
                    Helpers.writeToFile(host2, path2, embeddedObjectContent);
                }
            }
        }

        return responseBytes;
    }

    /**
     * Returns the response of a server after sending a GET request.
     * The response is returned as a byte array.
     */
    private byte[] getResponseBytes(String host, String path, DataOutputStream outputStream, DataInputStream inputStream, boolean http1) throws IOException {

        String request = "";

        // send request to the server
        if (requestedURIs.containsKey(host + path)) {
            // if this uri was already requested once, then also send the if modified since header
            if (http1)
                request = "GET " + path + " HTTP/1.1\r\n" +
                        "Host: " + host + "\r\n" +
                        "If-Modified-Since: " + requestedURIs.get(host+path);
            else
                request = "GET " + path + " HTTP/1.0\r\n" +
                        "If-Modified-Since: " + requestedURIs.get(host+path);
        }
        else {
            // this uri was not requested yet, so just send a normal request
            if (http1)
                request = "GET " + path + " HTTP/1.1\r\n" +
                        "Host: " + host;
            else
                request = "GET " + path + " HTTP/1.0";
        }
        outputStream.writeBytes(request + "\r\n\r\n");

        // print the request that was sent
        System.out.println("*** Request sent: ***");
        System.out.println(request);

        // responseByte will contain the last received byte
        Byte responseByte;
        // response will contain the total response as an ArrayList of bytes
        ArrayList<Byte> response = new ArrayList<>();
        // line will contain the current line (by always adding the next byte until it ends by \r\n)
        String line = "";
        // lastModified will contain the Last-Modified date in header information once it is received
        String lastModified = "";
        if (requestedURIs.containsKey(host+path))
            lastModified = requestedURIs.get(host+path);
        // notModified will be set to true if 304 not modified is received
        boolean notModified = false;
        // contentLength will contain the Content-Length information once it is received
        int contentLength = 0;

        // start reading the response in a while loop, some variables needed to stop the wile loop:
        // read as long i <= nbBytes (=number of bytes expected) && ! stop
        int i=0;
        int nbBytes = Integer.MAX_VALUE; // start reading until the head part of the response ends
        boolean stop = false; // stop will be true when the end of the response is reached if HTTP/1.0 is used
        while (i <= nbBytes && ! stop) {

            try {

                // read the next byte
                responseByte = inputStream.readByte();

                // add byte to the response bytes
                response.add(responseByte);

                // convert byte to string
                byte[] responseBytes = new byte[1];
                responseBytes[0] = responseByte;
                String add = new String(responseBytes, "UTF-8");

                // add string to the current line
                line += add;

                if (line.endsWith("\n")) {

                    // if the content length was received, save it
                    if (line.startsWith("Content-Length: ")) {
                        String nb = line.replace("Content-Length: ", "").replace("\n", "").replace("\r", "");
                        contentLength = Integer.parseInt(nb);
                    }

                    // if the date was received, save it
                    if (line.startsWith("Last-Modified: ")) {
                        lastModified = line.replace("Last-Modified: ", "").replace("\n", "").replace("\r", "");
                    }

                    // check if not modified received
                    if (line.contains("304 Not Modified")) {
                        notModified = true;
                    }

                    // when the content of the file starts,
                    // make sure we only read the next [contentLength] bytes
                    if (Helpers.isEmptyLine(line) && nbBytes == Integer.MAX_VALUE) {
                        i = 0;
                        nbBytes = contentLength;
                    }

                    // reset line
                    line = "";

                }

                i++;
            }

            catch (EOFException e) {
                // end of file reached ==> stop reading (this is necessary when HTTP/1.0 is used)
                stop = true;
            }

        }

        // when the file was not modified, return the stored response
        if (notModified) {
            System.out.println("*** Requested file was not modified: using stored response ***");
            String file = path;
            if (file.equals("/"))
                file = "/index";
            Path stored = Paths.get("downloads/cache/"+host+file+".get.log");
            byte[] data = Files.readAllBytes(stored);
            return data;
        }

        // save the response in cache
        String file = path;
        if (file.equals("/"))
            file = "/index";
        Helpers.writeToFile("cache/" + host, file + ".get.log", Helpers.toByteArray(response));

        // make sure we remember that this URI was requested
        requestedURIs.put(host+path, lastModified);

        return Helpers.toByteArray(response);
    }


    ///////////////////////////////////////////////////PUT//////////////////////////////////////////////////////////////

    /**
     * Send a HEAD command and print the response.
     */
    private void put(String host, String path, int port, boolean http1) throws IOException {
        // connect to host
        Socket clientSocket = new Socket(host, port);
        // create outputstream to this host
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        // create an inputstream to this host
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // buffered reader is easier here

        // let user enter the file content
        System.out.println("Enter file content, then enter . to end:");
        String content = ""; // content with "\r\n" at the end of each line
        BufferedReader buffer=new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        while (! (line = buffer.readLine()).equals(".")) {
            content += line + "\r\n";
        }

        // calculate content length
        byte[] bytes = content.getBytes("UTF-8");
        int contentLength = bytes.length; // number of bytes in content

        // Send HTTP command to server.
        String request = "";
        if (http1)
            request = "PUT " + path + " HTTP/1.1" + "\r\n" +
                    "Host: " + host + "\r\n" +
                    "Content-Length: " + contentLength + "\r\n" +
                    "Connection: close" + "\r\n" +
                    "" + "\r\n" +
                    content+"\r\n";
        else
            request = "PUT " + path + " HTTP/1.0" + "\r\n" +
                    "Content-Length: " + contentLength + "\r\n" +
                    "Connection: close" + "\r\n" +
                    "" + "\r\n" +
                    content+"\r\n";

        // print request
        outToServer.writeBytes(request + "\r\n");
        System.out.println("*** Request sent: ***");
        System.out.println(request);

        // print response
        System.out.println("*** Response: ***");
        // Read text from the server
        String response = "";
        while ((response = inFromServer.readLine()) != null) {
            // print response to screen
            System.out.println(response);
        }

        clientSocket.close();
    }

    ///////////////////////////////////////////////////POST/////////////////////////////////////////////////////////////

    /**
     * Send a POST command and print the response.
     */
    private void post(String host, String path, int port, boolean http1) throws IOException {
        // connect to host
        Socket clientSocket = new Socket(host, port);
        // create outputstream to this host
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        // create an inputstream to this host
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // buffered reader is easier here

        // let user enter the content to append to file
        System.out.println("Enter content to append to file, then enter . to end:");
        String content = ""; // content with "\r\n" at the end of each line
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        while (!(line = buffer.readLine()).equals(".")) {
            content += line + "\r\n";
        }

        // calculate content length
        byte[] bytes = content.getBytes("UTF-8");
        int contentLength = bytes.length; // number of bytes in content


        // Send HTTP command to server.
        String request = "";
        if (http1)
            request = "POST " + path + " HTTP/1.1" + "\r\n" +
                    "Host: " + host + "\r\n" +
                    "Content-Length: " + contentLength + "\r\n" +
                    "Connection: close" + "\r\n" +
                    "" + "\r\n" +
                    content+"\r\n";
        else
            request = "POST " + path + " HTTP/1.0" + "\r\n" +
                    "Content-Length: " + contentLength + "\r\n" +
                    "Connection: close" + "\r\n" +
                    "" + "\r\n" +
                    content+"\r\n";

        // print request
        outToServer.writeBytes(request + "\r\n");
        System.out.println("*** Request sent: ***");
        System.out.println(request);

        // print response
        System.out.println("*** Response: ***");
        // Read text from the server
        String response = "";
        while ((response = inFromServer.readLine()) != null) {
            // print response to screen
            System.out.println(response);
        }

        clientSocket.close();
    }
}
