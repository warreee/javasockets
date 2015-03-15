/**
 * Created by warreee on 2/26/15.
 */


public class HTTPClientTest {

    public static void main(String[] args) {
        /**
         * Interessante site om te testen:
         *
         *      http://httpbin.org/
         *
         */
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
        String[] args12 = {"HEAD", "http://tldp.org/", "80", "1.1"};
        String[] args13 = {"GET", "http://tldp.org/", "80", "1.1"};
        String[] args14 = {"GET", "http://www.linux-ip.net/", "80", "1.0"};
        String[] args15 = {"GET", "http://www.linux-ip.net/", "80", "1.1"};
        String[] args16 = {"PUT", "http://httpbin.org/put", "80", "1.0"};
        String[] args17 = {"PUT", "http://httpbin.org/put", "80", "1.1"};

        try {
           HTTPClient.main(args17);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
