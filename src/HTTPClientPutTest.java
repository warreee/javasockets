/**
 * Created by warreee on 3/11/15.
 */
public class HTTPClientPutTest {

    public static void main(String[] args) {
        String[] args1 = {"PUT", "http://httpbin.org/put", "80", "1.0"};
        String[] args2 = {"PUT", "localhost/test", "6789", "1.0"};

        try {

            HTTPClient.main(args2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
