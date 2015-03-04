/**
 * Created by warreee on 3/4/15.
 */
public class HTTPClientPostTest {

    public static void main(String[] args) {
        String[] args2 = {"HEAD", "http://www.google.be/index.html", "80", "1.0"}; // waarom krijgen we hier toch 302 voor terug?
        String[] args3 = {"HEAD", "http://www.google.com/index.html", "80", "1.0"};
        String[] args4 = {"HEAD", "http://www.fonafix.be/index.php", "80", "1.0"};
        // 1.1
        String[] args5 = {"HEAD", "http://www.fonafix.be/index.php", "80", "1.1"}; // waarom krijgen we hier 301?
        String[] args6 = {"HEAD", "http://www.google.be/index.html", "80", "1.1"};
        String[] args7 = {"HEAD", "http://www.google.com/index.html", "80", "1.1"}; // werkt nog niet
        String[] args8 = {"HEAD", "http://people.cs.kuleuven.be/~bart.demoen/AB/index.html", "80", "1.1"};
        String[] args9 = {"GET", "http://fonafix.be/", "80", "1.0"};
        try {

            HTTPClient.main(args9);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
