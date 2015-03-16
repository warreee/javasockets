import java.io.*;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
* A simple HTTP Client application
*
* Computer Networks, KU Leuven.
*
* Arne De Brabandere
* Ward Schodts
*/
class HTTPClient {

    // TODO: log file is not really necessary...
    public static LogFile logFile = new LogFile("log.txt");

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

    /**
     * Check if a line is empty.
     */
    private static boolean isEmptyLine(String line) {
        return line.replace("\n","").replace("\r","").isEmpty();
    }


    ///////////////////////////////////////////CLIENT-OBJECT////////////////////////////////////////////////////////////


    /**
     * Map with all requested URI's (with the GET command) and the corresponding date (in String format) it was last modified.
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
        FileInputStream fileIn = new FileInputStream("requested.log");
        ObjectInputStream in = new ObjectInputStream(fileIn);
        requestedURIs = (HashMap<String,String>) in.readObject();
        in.close();
        fileIn.close();

        String path = uri.getPath();
        String host = uri.getHost();

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
        FileOutputStream fileOut =
                new FileOutputStream("requested.log");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(requestedURIs);
        out.close();
        fileOut.close();
    }

    /**
     * Another constructor, just for testing purposes
     * @param host
     * @param port
     */
    public HTTPClient(String host, int port) throws IOException {
        Socket clientSocket = new Socket(host, port);
        System.out.println("Connection setup successful");
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
     * Send a GET command.
     * @return The response of the server.
     */
    private byte[] get(String host, String path, int port, boolean http1) throws IOException, URISyntaxException {
        // connect to host
        Socket clientSocket = new Socket(host, port);
        // create outputstream to this host
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        // create an inputstream to this host
        DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());

        // get content of requested file
        byte[] responseBytes = get(host,path,outToServer,inFromServer,http1);
        List<String> response = getLines(responseBytes);
        System.out.println("*** Response: ***");
        for (String line : response)
            System.out.print(line);

        // if HTTP 1.1 was used, then get the embedded objects in same socket connection
        if (http1 && ! getEmbeddedObjects(getContent(response)).isEmpty()) {
            for (String uri : getEmbeddedObjects(getContent(response))) {
                String host2 = getHost2(host, path, uri);
                String path2 = getPath2(path, uri);
                if (host2.equals(host)) {
                    // only get embedded objects on the same host
                    // TODO: or also on other hosts? [probably not] (have to make new connection for that)
                    byte[] embeddedObjectContent = getContent(get(host2, path2, outToServer, inFromServer, http1)); // in same connection
                    // write to file
                    writeToFile(host2,path2,embeddedObjectContent);
                }
            }
        }

        clientSocket.close();

        // if HTTP 1.0 was used, then get the embedded objects in new socket connection
        if (! http1 && ! getEmbeddedObjects(getContent(response)).isEmpty()) {
            for (String uri : getEmbeddedObjects(getContent(response))) {
                String host2 = getHost2(host, path, uri);
                String path2 = getPath2(path, uri);
                if (host2.equals(host)) {
                    // only get embedded objects on the same host
                    // TODO: or also on other hosts? [probably not]
                    byte[] embeddedObjectContent = getContent(get(host2, path2, port, http1));
                    // write to file
                    writeToFile(host2,path2,embeddedObjectContent);
                }
            }
        }

        return responseBytes;
    }

    /**
     * Returns the response of a server after sending a GET request.
     */
    private byte[] get(String host, String path, DataOutputStream outputStream, DataInputStream inputStream, boolean http1) throws IOException {

        String request = "";

        if (requestedURIs.containsKey(host + path)) {
            // if this uri was already requested once, then send the if modified since header
            if (http1)
                request = "GET " + path + " HTTP/1.1\r\n" +
                        "Host: " + host + "\r\n" +
                        "If-Modified-Since: " + requestedURIs.get(host+path);
            else
                request = "GET " + path + " HTTP/1.0\r\n" +
                        "If-Modified-Since: " + requestedURIs.get(host+path);
        }
        else {
            if (http1)
                request = "GET " + path + " HTTP/1.1\r\n" +
                        "Host: " + host;
            else
                request = "GET " + path + " HTTP/1.0";
        }

        outputStream.writeBytes(request + "\r\n\r\n");
        System.out.println("*** Request sent: ***");
        System.out.println(request);

        Byte responseByte;
        String line = "";
        ArrayList<Byte> response = new ArrayList<>();

        int i=0;
        int nbBytes = Integer.MAX_VALUE; // start reading until the head part of the response ends
        int contentLength = 0; // the content length will be set as soon as it is received

        boolean stop = false;

        String lastModified = ""; // remember the Last-Modified date in header information
        if (requestedURIs.containsKey(host+path))
            lastModified = requestedURIs.get(host+path);

        boolean notModified = false; // true if 304 not modified was received

        while (i <= nbBytes && ! stop) {

            try {

                // read the next byte
                responseByte = inputStream.readByte();

                // convert byte to string
                byte[] responseBytes = new byte[1];
                responseBytes[0] = responseByte;
                String add = new String(responseBytes, "UTF-8");

                // add byte to the response bytes
                response.add(responseBytes[0]);

                // add string to the current line
                line += add;

                if (line.endsWith("\n")) {

                    // if the content length was received, save it
                    if (line.startsWith("Content-Length: ")) {
                        String nb = line.replace("Content-Length: ", "").replace("\n", "").replace("\r", "");
                        contentLength = Integer.parseInt(nb);
                    }

                    // if the date was received, store it
                    if (line.startsWith("Last-Modified: ")) {
                        lastModified = line.replace("Last-Modified: ", "").replace("\n", "").replace("\r", "");
                    }

                    // check if not modified
                    if (line.contains("Not Modified")) {
                        notModified = true;
                    }

                    // when the content of the file starts,
                    // make sure we only read the next [contentLength] bytes
                    if (isEmptyLine(line) && nbBytes == Integer.MAX_VALUE) {
                        i = 0;
                        nbBytes = contentLength;
                    }

                    //System.out.print(line);

                    line = "";

                }

                i++;
            }

            catch (EOFException e) {
                stop = true; // this is necessary to stop reading when HTTP/1.0 is used
            }

        }

        if (notModified) {
            System.out.println("*** Requested file was not modified: using stored response ***");
            String file = path;
            if (file.equals("/"))
                file = "/index";
            Path stored = Paths.get("downloads/cache/"+host+file+".get.log");
            byte[] data = Files.readAllBytes(stored);
            return data;
        }

        // convert to array
        byte[] result = new byte[response.size()];
        for (int j=0; j < result.length; j++)
            result[j] = response.get(j);

        // save the response locally
        String file = path;
        if (file.equals("/"))
            file = "/index";
        writeToFile("cache/"+host,file+".get.log",result);

        // make sure we remember that this URI was requested
        requestedURIs.put(host+path, lastModified);

        return result;
    }

    private List<String> getLines(byte[] bytes) throws UnsupportedEncodingException {
        ArrayList<String> lines = new ArrayList<>();
        String line = "";
        for (Byte responseByte : bytes) {

            // convert byte to string
            byte[] responseBytes = new byte[1];
            responseBytes[0] = responseByte;
            String add = new String(responseBytes, "UTF-8");

            // add string to the current line
            line += add;

            if (line.endsWith("\n")) {

                // add string to the response string
                lines.add(line);
                //System.out.print(line);

                line = "";

            }
        }
        return lines;
    }

    /**
     * Get the file content of the reponse of a server.
     */
    private List<String> getContent(List<String> response) {
        ArrayList<String> result = new ArrayList<>();
        boolean add = false;
        for (String line : response) {
            if (add) {
                result.add(line);
            }
            else if (isEmptyLine(line)) {
                add = true; // start adding lines after head (= after first empty line)
            }
        }
        return result;
    }

    /**
     * Get the file content of the reponse of a server.
     */
    private byte[] getContent(byte[] response) throws UnsupportedEncodingException {
        String line = "";
        boolean addToContent = false;
        List<Byte> contentList = new ArrayList<>();
        for (Byte responseByte : response) {

            // convert byte to string
            byte[] responseBytes = new byte[1];
            responseBytes[0] = responseByte;
            String add = new String(responseBytes, "UTF-8");

            // add to content
            if (addToContent)
                contentList.add(responseBytes[0]);

            // add string to the current line
            line += add;

            if (line.endsWith("\n")) {

                if (isEmptyLine(line))
                    addToContent = true; // start adding bytes to content after first empty line

                line = "";

            }
        }

        // convert to array
        byte[] result = new byte[contentList.size()];
        for (int j=0; j < result.length; j++)
            result[j] = contentList.get(j);

        return result;
    }

    /**
     * Get the uri's of all the embedded objects in a file.
     */
    private List<String> getEmbeddedObjects(List<String> content) {
        ArrayList<String> uris = new ArrayList<>();
        String pattern = "src=\"(.*?)\"";
        Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        for (String outputLine : content) {
            Matcher m = r.matcher(outputLine);
            while (m.find()) {
                String uri = m.group(1);
                uris.add(uri);
            }
        }
        return uris;
    }

    private void writeToFile(String host, String path, byte[] content) throws IOException {
        String outputDir = "downloads/" + host + path.substring(0, path.lastIndexOf("/") + 1);
        String fileName;
        if (path.contains("/"))
            fileName = path.substring(path.lastIndexOf("/") + 1);
        else
            fileName = path;
        String outputPath = outputDir + fileName;

        // create dirs
        new File(outputDir).mkdirs();

        // create file
        File file = new File(outputPath);
        file.createNewFile();

        // first delete file if it exists
        if (file.exists())
            file.delete();

        FileOutputStream fos = new FileOutputStream(outputPath);
        fos.write(content);
        fos.close();

    }

    private static String getHost2(String host, String path, String uri) throws URISyntaxException {
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            URI uriObject = new URI(uri);
            return uriObject.getHost();
        }
        else if (uri.startsWith("//")) {
            URI uriObject = new URI("http:"+uri);
            return uriObject.getHost();
        }

        String currentDir = path.substring(0,path.lastIndexOf("/"));
        return host;
    }

    private static String getPath2(String path, String uri) throws URISyntaxException {
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            URI uriObject = new URI(uri);
            return uriObject.getPath();
        }
        else if (uri.startsWith("//")) {
            URI uriObject = new URI("http:"+uri);
            return uriObject.getPath();
        }

        String currentDir = path.substring(0,path.lastIndexOf("/"));
        return currentDir + "/" + uri;
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
        System.out.println("Enter file content, then enter empty line to end:");
        String content = ""; // content with "\r\n" at the end of each line
        BufferedReader buffer=new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        while (! (line = buffer.readLine()).equals("")) {
            content += line + "\r\n";
        }

        // calculate content length
        byte[] bytes = content.replace("\r\n","").getBytes("UTF-8");
        int contentLength = bytes.length; // number of bytes in content

        // Send HTTP command to server.
        String request = "";
        if (http1)
            request = "PUT " + path + " HTTP/1.1" + "\r\n" +
                    "Host: " + host + "\r\n" +
                    "Content-Length: " + contentLength + "\r\n" +
                    "Connection: close" + "\r\n" +
                    "" + "\r\n" +
                    content;
        else
            request = "PUT " + path + " HTTP/1.0" + "\r\n" +
                    "Content-Length: " + contentLength + "\r\n" +
                    "Connection: close" + "\r\n" +
                    "" + "\r\n" +
                    content;

        outToServer.writeBytes(request + "\r\n");
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
        System.out.println("Enter content to append to file, then enter empty line to end:");
        String content = ""; // content with "\r\n" at the end of each line
        BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
        String line = "";
        while (!(line = buffer.readLine()).equals("")) {
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
                    content;
        else
            request = "POST " + path + " HTTP/1.0" + "\r\n" +
                    "Content-Length: " + contentLength + "\r\n" +
                    "Connection: close" + "\r\n" +
                    "" + "\r\n" +
                    content;

        outToServer.writeBytes(request + "\r\n");
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
}