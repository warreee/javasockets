/**
 * Created by warreee on 2/26/15.
 */


public class HTTPClientTest {

    public static void main(String[] args) {
        String[] args2 = {"HEAD", "http://www.google.be/index.html", "80", "1.0"}; // waarom krijgen we hier toch 302 voor terug?
        String[] args3 = {"HEAD", "http://www.google.com/index.html", "80", "1.0"};
        String[] args4 = {"HEAD", "http://www.fonafix.be/index.php", "80", "1.0"};
        // 1.1
        String[] args5 = {"HEAD", "http://www.fonafix.be/index.php", "80", "1.1"}; // waarom krijgen we hier 301?
        String[] args6 = {"HEAD", "http://www.google.be/index.html", "80", "1.1"};
        String[] args7 = {"HEAD", "http://www.google.com/index.html", "80", "1.1"}; // werkt nog niet
        String[] args8 = {"HEAD", "http://people.cs.kuleuven.be/~bart.demoen/AB/index.html", "80", "1.1"};
        String[] args9 = {"GET", "http://fonafix.be/", "80", "1.1"};
        String[] args10 = {"GET", "localhost/javasockets/index.php", "80", "1.1"};
        String[] args11 = {"GET", "http://tldp.org/", "80", "1.0"};
        String[] args12 = {"GET", "http://tldp.org/", "80", "1.1"};

        try {
            /*HTTPClient.main(args2);
            HTTPClient.main(args3);*/
           /* HTTPClient.main(args4);
            HTTPClient.main(args5);*/
           /* HTTPClient.main(args6);
            HTTPClient.main(args7);*/
           /* HTTPClient.main(args9);*/
           HTTPClient.main(args12);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
