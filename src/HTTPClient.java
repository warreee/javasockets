import java.io.*;
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

  public static void main(String[] args) throws Exception {
    if (args.length != 4)
      printHelp();

    // get arguments
    String command = args[0];
    String uriString = args[1];
    String portString = args[2];
    String version = args[3];

    // get URI object from uriString
    URI uri = getURI(uriString);

    // get port int
    int port = Integer.parseInt(portString);

    parseCommand(command,uri,port,version);
  }

  /**
   * Print the description of how to specify the program arguments.
   */
  public static void printHelp() {

      System.out.println("Description of arguments..."); // TODO
  }

  /**
   * Get URI object from given URI string
   * @param uriString String value of the given URI
   */
  private static URI getURI(String uriString) throws Exception {
    // TODO nakijken of er http:// voostaat of niet
    return new URI(uriString);
  }

  /**
   * Parse the command.
   * @param command command string
   * @param uri URI object
   * @param port port number
   * @param version http version (1.0 or 1.1)
   */
  private static void parseCommand(String command, URI uri, int port, String version) {
    try {
      switch (command) {
        case "HEAD":
          head(uri, port, version);
          break;
         /*case "GET" : get(uri, port, version);
                        break;
         case "PUT" : put(uri, port, version);
                      break;
         case "POST" : post(uri, port, version);
                       break;*/
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Command: HEAD (uri) (port) (version)
   * @param uri URI object
   * @param port port number
   * @param version http version (1.0 or 1.1)
   * @throws Exception
   */
  private static void head(URI uri, int port, String version) throws Exception {
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

    // Send HTTP command to server.
    outToServer.writeBytes("HEAD " + path + " HTTP/" + version + "\r\n\r\n");

    // Read text from the server and write it to the screen.
    String response = "";
    while ((response = inFromServer.readLine()) != null) {

        System.out.println(response);

    }

    // Close the socket.
    clientSocket.close();
  }

}
