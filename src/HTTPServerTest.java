/*
 * A simple example TCP Server application
 *
 * Computer Networks, KU Leuven.
 *
 * Arne De Brabandere
 * Ward Schodts
 */

/**
 *
 */
public class HTTPServerTest {

    public static void main(String argv[]) throws Exception {
        HTTPServer server = new HTTPServer();

        server.run();


        String localhost = "localhost";
        int port = 6789;
        HTTPClient testClient = new HTTPClient(localhost, port);


    }

}
