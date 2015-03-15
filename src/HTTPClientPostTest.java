/**
 * Created by warreee on 3/4/15.
 */
public class HTTPClientPostTest {

    public static void main(String[] args) {
        String[] args1 = {"POST", "http://httpbin.org/post", "80", "1.0"};

        try {

            HTTPClient.main(args1);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
