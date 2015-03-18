import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Map;

/**
 * Created by warreee on 16/03/15.
 */
public class CommandPut extends Command {
    public CommandPut(String path, boolean http1, Map<String, String> info, String data) {
        super(path, http1, info, data);
    }

    @Override
    public byte[] getResponse() {
        Response response = new Response();

        // HTTP 1.1 or 1.0, and must close after this request
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

        else if (! info.containsKey("content-length")) { // Content-Length not specified, so write nothing to file
                                                         // but still return a response like a GET request
            try {
                File file = new File("www" + this.path);
                String contentType = Files.probeContentType(file.toPath());
                long nbBytes = readFile(file.getPath()).length;
                byte[] fileContent = readFile(file.getPath());

                // file exists, so print details

                // STATUS
                response.addString("200 OK\r\n");
                // DATE
                response.addString("Date: " + currentDate + "\r\n");
                // CONTENT-TYPE
                response.addString("Content-Type: " + contentType + "\r\n");
                // CONTENT-LENGTH
                response.addString("Content-Length: " + nbBytes + "\r\n");
                // CONTENT
                response.addString("\r\n");
                response.addBytes(fileContent);
            }
            catch (IOException e) {
                // file doesn't exist but can't write because content length was not given ==> 404 Not Found

                response.addString("404 Not Found\r\n");
                // DATE
                response.addString("Date: " + currentDate + "\r\n");
            }
        }

        else {

            try { // OK

                // Write data to file
                // Note: only the first <Content-Length> bytes will be written!
                this.writeToFile("www" + this.path, Arrays.copyOfRange(data.getBytes(), 0, Integer.parseInt(info.get("content-length"))));

                // Get info of edited/created file
                File file = new File("www"+this.path);
                String contentType = Files.probeContentType(file.toPath());
                long nbBytes = readFile(file.getPath()).length;
                byte[] fileContent = readFile(file.getPath());

                // show info and data of edited/created file

                // STATUS
                response.addString("200 OK\r\n");
                // DATE
                response.addString("Date: " + currentDate + "\r\n");
                // CONTENT-TYPE
                response.addString("Content-Type: " + contentType +"\r\n");
                // CONTENT-LENGTH
                response.addString("Content-Length: " + nbBytes +"\r\n");
                // CONTENT
                response.addString("\r\n");
                response.addBytes(fileContent);
            }

            catch (IOException e) { // Problem with creating of writing to file
                response.addString("500 Server Error\r\n");
                // DATE
                response.addString("Date: " + currentDate + "\r\n");
            }

        }


        return response.getByteArray();
    }

}
