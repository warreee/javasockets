//Import necessary IO and NETwork libraries
import java.io.*;
import java.net.*;

/*
 * A simple example TCP Server application
 *
 * Computer Networks, KU Leuven.
 *
 * Arne De Brabandere
 * Ward Schodts
 */
class TCPServer
  {

  /*
   * Everything is included in the main method.
   */
  public static void main(String argv[]) throws Exception
    {
    // Create server (incoming) socket on port 6789.
    ServerSocket welcomeSocket = new ServerSocket(6789);

    // Wait for a connection to be made to the server socket. 
    while(true)
      {
      // Create	 a 'real' socket from the Server socket.
      Socket connectionSocket = welcomeSocket.accept();
  
      // Create inputstream (convenient data reader) to this host.
      BufferedReader inFromClient = new BufferedReader(new InputStreamReader	(connectionSocket.getInputStream()));

      // Create outputstream (convenient data writer) to this host.
      DataOutputStream outToClient = new DataOutputStream(connectionSocket.getOutputStream());

      // Read text from the client, make it uppercase and write it back.
      String clientSentence = inFromClient.readLine();
      System.out.println("Received: " + clientSentence);
      String capsSentence = clientSentence.toUpperCase() + '\n';
      outToClient.writeBytes(capsSentence);
      System.out.println("Sent: "+ capsSentence);
      }

    } // End of main method.

  } // End of class TCPServer
