/**
 * Created by warreee on 3/4/15.
 */
public class HTTPClientPostTest {

    public static void main(String[] args) {
        String[] args1 = {"POST", "http://httpbin.org/post", "80", "1.0"};
        String[] args2 = {"POST", "http://localhost/javasockets/welcome.php", "80", "1.1"};
        //String[] args2 = {"POST", "http://www.tweakers.net/zoeken", "80", "1.0"};
        String[] args4 = {"HEAD", "http://www.fonafix.be/index.php", "80", "1.0"};
        // 1.1
        String[] args5 = {"HEAD", "http://www.fonafix.be/index.php", "80", "1.1"}; // waarom krijgen we hier 301?
        String[] args6 = {"HEAD", "http://www.google.be/index.html", "80", "1.1"};
        String[] args7 = {"HEAD", "http://www.google.com/index.html", "80", "1.1"}; // werkt nog niet
        String[] args8 = {"HEAD", "http://people.cs.kuleuven.be/~bart.demoen/AB/index.html", "80", "1.1"};
        String[] args9 = {"GET", "http://fonafix.be/", "80", "1.0"};
        try {

            HTTPClient.main(args1);
            //HTTPClient.main(args2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
