import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Created by warreee on 16/03/15.
 */
public class CommandHead extends Command {

    public CommandHead(String path, boolean http1, Map<String, String> info) {
        super(path, http1, info, null);
    }

    @Override
    public byte[] getResponse() {
        Response response = new Response();

        // HTTP 1.1 or 1.0, and must close after this request?
        if (http1) {
            response.addString("HTTP/1.1 ");
            if (info.containsKey("connection") && info.get("connection").equalsIgnoreCase("close"))
                this.mustClose = true;
        }
        else {
            response.addString("HTTP/1.0 ");
            this.mustClose = true;
        }

        // REST OF THE RESPONSE

        String currentDate = getCurrentDate();

        if (isBadRequest()) { // Bad Request
            // STATUS
            response.addString("400 Bad Request\r\n");
            // DATE
            response.addString("Date: " + currentDate + "\r\n");
        }

        else {

            try { // OK
                File file = new File("www"+this.path);
                String contentType = Files.probeContentType(file.toPath());
                long nbBytes = readFile(file.getPath()).length;

                // STATUS
                response.addString("200 OK\r\n");
                // DATE
                response.addString("Date: " + currentDate + "\r\n");
                // CONTENT-TYPE
                response.addString("Content-Type: " + contentType +"\r\n");
                // CONTENT-LENGTH
                response.addString("Content-Length: " + nbBytes +"\r\n");
            }

            catch (IOException e) { // Not Found
                response.addString("404 Not Found\r\n");
                // DATE
                response.addString("Date: " + currentDate + "\r\n");
            }

        }


        return response.getByteArray();
    }
}
