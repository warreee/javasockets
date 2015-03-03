/**
 * Created by warreee on 2/26/15.
 */

import org.junit.*;

public class HTTPClientTest {

    public static void main(String[] args) {
        String[] args2 = {"HEAD", "http://www.google.be/index.html", "80", "1.0"};
        try {
            HTTPClient.main(args2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
