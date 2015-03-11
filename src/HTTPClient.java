import java.io.*;
import java.net.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
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

    public static void main(String[] args) throws IOException {

        String host = "tldp.org";
        String path = "/images/flags/t_es.gif";
        String version = "1.1";
        int port = 80;

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
        DataInputStream inFromServer = new DataInputStream(clientSocket.getInputStream());

        outToServer.writeBytes("GET " + path + " HTTP/" + version + "\r\n" +
                "HOST: " + host + "\r\n\r\n");

        Byte responseByte;
        String line = "";
        String response = "";
        int i=0;
        int nbBytes = Integer.MAX_VALUE;
        int contentLength = 0;
        while (i <= nbBytes) {

            responseByte = inFromServer.readByte();
            byte[] responseBytes = new byte[1];
            responseBytes[0] = responseByte;
            String add = new String(responseBytes, "UTF-8");
            line += add;
            response += add;

            if (line.endsWith("\r\n")) {

                if (line.startsWith("Content-Length: ")) {
                    String nb = line.replace("Content-Length: ","").replace("\r\n","");
                    contentLength = Integer.parseInt(nb);
                }

                if (line.equals("\r\n")) {
                    i=0;
                    nbBytes = contentLength;
                }

                line = "";

            }

            i++;
        }

        System.out.println(response);

        clientSocket.close();

    }

    /**
     * Log file: log.txt
     */
    public static LogFile logFile = new LogFile("log.txt");

    public static void main2(String[] args) throws Exception {

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

        ArrayList<String> uris = new ArrayList<>();

        // Parse command.
        try {
            switch (command) {
                case "HEAD":
                    head(inFromServer, outToServer, path, host, version);
                    break;
                case "GET":
                    // als get the returned array of embedded object URI's to request
                    uris = get(inFromServer, outToServer, path, host, version, clientSocket);
                    break;
                case "PUT":
                    put(inFromServer, outToServer, path, host, version);
                    break;
                case "POST":
                    post(inFromServer, outToServer, path, host, version);
                    break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        inFromServer.close();
        outToServer.close();
        // Close the socket.
        clientSocket.close();

        // If command is GET and http version is 1.0, then get the embedded objects AFTER the socket was closed
        // (if http version 1.1 was used, the embedded objects are requested inside the get() method)
        if (command.equals("GET") && version.equals("1.0")) {
            for (String uriString : uris) {

                String host2 = getHost2(host, path, uriString);
                String path2 = getPath2(path, uriString);
                String outDir = host2 + path2.substring(0, path2.lastIndexOf("/") + 1);

                // Connect to the host.
                try {
                    clientSocket = new Socket(host2, 80); // TODO: port van nieuwe connectie?
                } catch (IOException e) {
                    System.out.println("Unable to connect to " + host2 + ":" + 80);
                }

                // Create outputstream (convenient data writer) to this host.
                outToServer = new DataOutputStream(clientSocket.getOutputStream());

                // Create an inputstream (convenient data reader) to this host
                inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Get the file and save it
                getSave(inFromServer, outToServer, path2, host2, version, outDir, clientSocket);

                // Close the socket.
                clientSocket.close();

            }
        }
    }

    ///////////////////////////////////////////////////HEAD////////////////////////////////////////////////////////////

    private static void head(BufferedReader inFromServer, DataOutputStream outToServer, String path, String host, String version) throws Exception {
        // Send HTTP command to server.
        if(version.equals("1.0")) {
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

    ///////////////////////////////////////////////////GET////////////////////////////////////////////////////////////

    /**
     * GET request
     * @param inFromServer
     * @param outToServer
     * @param path
     * @param host
     * @param version
     * @return
     * @throws Exception
     */
    private static ArrayList<String> get(BufferedReader inFromServer, DataOutputStream outToServer, String path, String host, String version, Socket socket) throws Exception {
        // Send HTTP command to server.
        if(version.equals("1.0")) {
            outToServer.writeBytes("GET " + path + " HTTP/" + version + "\r\n\r\n");
        } else {
            outToServer.writeBytes("GET " + path + " HTTP/" + version + "\r\n" +
                    "HOST: " + host + "\r\n\r\n");
        }

        logFile.addLine("\n" + "Response:" + "\n");

        // Read text from the server
        String response = "";
        String[] output = new String[0];
        while ((response = inFromServer.readLine()) != null) {
            // print response to screen
            System.out.println(response);
            // write response to log file
            logFile.addLine(response);
            // add line to output in outputString
            output = Arrays.copyOf(output, output.length+1);
            output[output.length-1] = response;
        }

        // find embedded objects and add to uris
        ArrayList<String> uris = new ArrayList<>();
        String pattern = "src=\"(.*?)\"";
        Pattern r = Pattern.compile(pattern);
        for (String outputLine : output) {
            Matcher m = r.matcher(outputLine);
            while (m.find()) {
                String uri = m.group(1);
                uris.add(uri);
            }
        }

        // if http 1.1 was used, then get the embedded objects and save them locally
        if (version.equals("1.1")) {
            for (String uri : uris) {
                String host2 = getHost2(host, path, uri); // TODO: als dit een andere host is ==> nieuwe connectie maken??
                String path2 = getPath2(path, uri);
                String outDir = host2 + path2.substring(0, path2.lastIndexOf("/") + 1);
                getSave(inFromServer, outToServer, path2, host2, version, outDir, socket);
            }
        }

        return uris;
    }

    private static String getHost2(String host, String path, String uri) throws Exception {
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

    private static String getPath2(String path, String uri) throws Exception {
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

    public static void getSave(BufferedReader inFromServer, DataOutputStream outToServer, String path, String host, String version, String outDir, Socket socket) throws Exception {
        if (socket.isClosed())
            System.out.println("socket closed...");


        // Send HTTP command to server.
        if(version.equals("1.0")) {
            outToServer.writeBytes("GET " + path + " HTTP/" + version + "\r\n\r\n");
        } else {
            outToServer.writeBytes("GET " + path + " HTTP/" + version + "\r\n" +
                    "HOST: " + host + "\r\n\r\n");
        }

        // Read text from the server
        String response = "";
        String outputString = "";
        while ((response = inFromServer.readLine()) != null) {
            // add line to output in outputString
            outputString += response;
        }
        // TODO: eerste deel van outputString weglaten (todat de inhoud van het eigenlijke bestand begint)

        String outputDir = "out/" + outDir;
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

        PrintWriter printWriter = new PrintWriter (file);
        printWriter.print(outputString);
        printWriter.close();



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

    ///////////////////////////////////////////////////POST////////////////////////////////////////////////////////////




    private static void post(BufferedReader inFromServer, DataOutputStream outToServer, String path, String host, String version) throws Exception {



                // Send HTTP command to server.
                if (version.equals("1.0")) {
                    logFile.addLine("POST " + path + " HTTP/" + version + "\r\n");
                    System.out.println("POST " + path + " HTTP/" + version + "\r\n");
                    outToServer.writeBytes("POST " + path + " HTTP/" + version + "\r\n");
                    logFile.addLine("POST " + path + "HTTP/" + version + "\r\n");
                    outToServer.writeBytes("Host: " + host + "\r\n");
                    logFile.addLine("Host: " + host + "\r\n");
                    outToServer.writeBytes("Content-Type: application/x-www-form-urlencoded" + "\r\n");
                    logFile.addLine("Content-Type: multipart/form-data" + "\r\n");
                    outToServer.writeBytes("Content-Length: 22" + "\r\n");
                    logFile.addLine("Content-Length: 100" + "\r\n");
                    outToServer.writeBytes("\r\n");
                    logFile.addLine("\r\n");
                    outToServer.writeBytes("name6=testqsd" + "\r\n");
                    outToServer.writeBytes("name1=testqsd" + "\r\n\r\n");
                    logFile.addLine("name=test&bla=bla");
                } else { // not yet implemented
                    outToServer.writeBytes("HEAD " + path + " HTTP/" + version + "\r\n" +
                            "HOST: " + host + "\r\n\r\n");
                }
                logFile.addLine("\n" + "Response:" + "\n");

                // Read text from the server
                String response;
                while ((response = inFromServer.readLine()) != null) {
                    // print response to screen
                    System.out.println(response);
                    // write response to log file
                    logFile.addLine(response);
                }
            }
    ///////////////////////////////////////////////////PUT//////////////////////////////////////////////////////////////

    private static void put(BufferedReader inFromServer, DataOutputStream outToServer, String path, String host, String version) throws Exception {

        outToServer.writeBytes("PUT " + "/put/test" + " HTTP/" + version + "\r\n");
        outToServer.writeBytes("Host: " + host + "\r\n");
        outToServer.writeBytes("Content-Type: image/png" + "\r\n");
        outToServer.writeBytes("Content-Length: 22" + "\r\n");
        outToServer.writeBytes("\r\n");
        //outToServer.write();


        String response;
        while ((response = inFromServer.readLine()) != null) {
            // print response to screen
            System.out.println(response);
            // write response to log file
            logFile.addLine(response);
        }


    }
    }
