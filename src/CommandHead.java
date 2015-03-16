import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * Created by warreee on 16/03/15.
 */
public class CommandHead extends Command {

    public CommandHead(String path, boolean http1, Map<String, String> info, String data) {
        super(path, http1, info, data);
    }

    @Override
    public String getResponse() {
        String response = "";

        // HTTP 1.1 or 1.0
        if (http1)
            response = "HTTP/1.1 ";
        else
            response = "HTTP/1.0 ";

        // STATUS CODE
        if (isBadRequest()) {
            response += "400 Bad Request\r\n";
        }
        else {
            try {
                File file = new File(this.path);
                response += "200 OK\r\n";
                response += "Content-Type: " + Files.probeContentType(file.toPath())+"\r\n";
            }
            catch (IOException e) {
                response += "404 Not Found\r\n";
            }
        }

        // DATE
        response += "Date: " + getCurrentDate() + "\r\n";

        return response;
    }

    @Override
    public boolean mustClose() {
        return true;
    }
}
