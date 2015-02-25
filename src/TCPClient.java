// Import necessary IO and NETwork libraries
import com.sun.xml.internal.bind.v2.TODO;

import java.io.*;
import java.net.*;

/*
 * A simple example TCP Client application
 *
 * Computer Networks, KU Leuven.
 *
 * Arne De Brabandere
 * Ward Schodts
 */
class TCPClient
  {

  public static void main(String[] args) throws Exception
    {

        parseCommand(args);

    } // End of main method

  private static void parseCommand(String[] args) throws Exception {

    switch (args[0]) {
       case "HEAD" : head(args[1], args[2], args[3]);
           break;
       /*case "GET" : get(args[1], args[2], args[3]);
           break;
       case "PUT" : put(args[1], args[2], args[3]);
           break;
       case "POST" : post(args[1], args[2], args[3]);
           break;*/
    }
  }

      /**
       * TODO nakijken of er http:// voostaat of niet
       * @param uri
       * @param stringPort
       * @param httpversion
       * @throws Exception
       */
  private static void head(String uri, String stringPort, String httpversion) throws Exception {
      int port = Integer.parseInt(stringPort);
      URI uriObject = new URI(uri);
      String path = uriObject.getPath();
      String host = uriObject.getHost();
      Socket clientSocket = new Socket(uri, port);

      // Create outputstream (convenient data writer) to this host.
      DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());

      // Create an inputstream (convenient data reader) to this host
      BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

      outToServer.writeBytes("HEAD /index.html HTTP/1.0 \r\n\r\n");

      // Read text from the server and write it to the screen.
      String response = "";
      while ((response = inFromServer.readLine()) != null) {

          System.out.println(response);

      }


      // Close the socket.
      clientSocket.close();
  }


  } // End of class TCPClient
