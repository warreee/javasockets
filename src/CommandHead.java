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
    public String getResponse() {
        String response = "";

        // HTTP 1.1 or 1.0, and must close after this request?
        if (http1) {
            response = "HTTP/1.1 ";
            if (info.containsKey("connection") && info.get("connection").equalsIgnoreCase("close"))
                this.mustClose = true;
        }
        else {
            response = "HTTP/1.0 ";
            this.mustClose = true;
        }

        // REST OF THE RESPONSE

        if (isBadRequest()) { // Bad Request
            // STATUS
            response += "400 Bad Request\r\n";
            // DATE
            response += "Date: " + getCurrentDate() + "\r\n";
        }

        else {

            try { // OK
                File file = new File(this.path);
                // STATUS
                response += "200 OK\r\n";
                // DATE
                response += "Date: " + getCurrentDate() + "\r\n";
                // CONTENT-TYPE
                response += "Content-Type: " + Files.probeContentType(file.toPath())+"\r\n";
                // CONTENT-LENGTH
                response += "Content-Length: " + getNbBytes(readFile(path, StandardCharsets.UTF_8));
            }

            catch (IOException e) { // Not Found
                response += "404 Not Found\r\n";
                // DATE
                response += "Date: " + getCurrentDate() + "\r\n";
            }

        }


        return response;
    }
}
