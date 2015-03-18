public class HTTPClientGetTest {

    public static void main(String[] args) {

        String[] args1 = {"GET", "www.example.com/index.html", "80", "1.0"};
        String[] args2 = {"GET", "www.tcpipguide.com/index.htm", "80", "1.0"};
        String[] args3 = {"GET", "www.jmarshall.com/index.html", "80", "1.1"};



        try {
            HTTPClient.main(args1);
            HTTPClient.main(args2);
            HTTPClient.main(args3);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
