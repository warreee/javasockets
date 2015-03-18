import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Helpers {


    /**
     * Get URI object from given URI string
     * @param uriString String value of the given URI
     */
    static URI getURI(String uriString) throws URISyntaxException {
        if (! uriString.startsWith("http://") && ! uriString.startsWith("https://")) {
            uriString = "http://" + uriString;
        }
        return new URI(uriString);
    }

    /**
     * Check if a string is an integer.
     */
    static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * Check if a line is empty.
     */
    static boolean isEmptyLine(String line) {
        return line.replace("\n","").replace("\r","").isEmpty();
    }

    /**
     * Returns the host of a uri that was read from a page
     */
    static String getSubHost(String host, String path, String uri) throws URISyntaxException {
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            URI uriObject = new URI(uri);
            return uriObject.getHost();
        }
        else if (uri.startsWith("//")) {
            URI uriObject = new URI("http:"+uri);
            return uriObject.getHost();
        }

        String currentDir = path.substring(0,path.lastIndexOf("/"));
        return host;
    }

    /**
     * Returns the path of a uri that was read from a page
     */
    static String getSubPath(String path, String uri) throws URISyntaxException {
        if (uri.startsWith("http://") || uri.startsWith("https://")) {
            URI uriObject = new URI(uri);
            return uriObject.getPath();
        }
        else if (uri.startsWith("//")) {
            URI uriObject = new URI("http:"+uri);
            return uriObject.getPath();
        }

        String currentDir = path.substring(0,path.lastIndexOf("/"));
        return currentDir + "/" + uri;
    }

    /**
     * Returns a string version of a byte array.
     */
    public static List<String> getLines(byte[] bytes) throws UnsupportedEncodingException {
        ArrayList<String> lines = new ArrayList<>();
        String line = "";
        for (Byte responseByte : bytes) {

            // convert byte to string
            byte[] responseBytes = new byte[1];
            responseBytes[0] = responseByte;
            String add = new String(responseBytes, "UTF-8");

            // add string to the current line
            line += add;

            if (line.endsWith("\n")) {

                // add string to the response string
                lines.add(line);
                //System.out.print(line);

                line = "";

            }
        }
        return lines;
    }

    /**
     * Get the file content of a server response in a string.
     */
    public static List<String> getContent(List<String> response) {
        ArrayList<String> result = new ArrayList<>();
        boolean add = false;
        for (String line : response) {
            if (add) {
                result.add(line);
            }
            else if (isEmptyLine(line)) {
                add = true; // start adding lines after head (= after first empty line)
            }
        }
        return result;
    }

    /**
     * Get the file content of a server response in bytes.
     */
    public static byte[] getContent(byte[] response) throws UnsupportedEncodingException {
        String line = "";
        boolean addToContent = false;
        List<Byte> contentList = new ArrayList<>();
        for (Byte responseByte : response) {

            // convert byte to string
            byte[] responseBytes = new byte[1];
            responseBytes[0] = responseByte;
            String add = new String(responseBytes, "UTF-8");

            // add to content
            if (addToContent)
                contentList.add(responseBytes[0]);

            // add string to the current line
            line += add;

            if (line.endsWith("\n")) {

                if (isEmptyLine(line))
                    addToContent = true; // start adding bytes to content after first empty line

                line = "";

            }
        }

        // convert to array
        byte[] result = new byte[contentList.size()];
        for (int j=0; j < result.length; j++)
            result[j] = contentList.get(j);

        return result;
    }

    /**
     * Get the uri's of all the embedded objects in a file.
     */
    public static List<String> getEmbeddedObjects(List<String> content) {
        ArrayList<String> uris = new ArrayList<>();
        String pattern = "src=\"(.*?)\"";
        Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        for (String outputLine : content) {
            Matcher m = r.matcher(outputLine);
            while (m.find()) {
                String uri = m.group(1);
                uris.add(uri);
            }
        }
        return uris;
    }

    /**
     * Write bytes to file
     */
    public static void writeToFile(String host, String path, byte[] content) throws IOException {
        String outputDir = "downloads/" + host + path.substring(0, path.lastIndexOf("/") + 1);
        String fileName;
        if (path.contains("/"))
            fileName = path.substring(path.lastIndexOf("/") + 1);
        else
            fileName = path;
        String outputPath = outputDir + fileName;

        // create dirs
        new File(outputDir).mkdirs();

        // create file
        File file = new File(outputPath);
        file.createNewFile();

        // first delete file if it exists
        if (file.exists())
            file.delete();

        FileOutputStream fos = new FileOutputStream(outputPath);
        fos.write(content);
        fos.close();

    }

    /**
     * Transforms an arraylist of bytes to a byte array.
     */
    public static byte[] toByteArray(ArrayList<Byte> list) {
        byte[] result = new byte[list.size()];
        for (int j=0; j < result.length; j++)
            result[j] = list.get(j);
        return result;
    }

    public static HashMap<String, String> readRequestedURIs() throws ClassNotFoundException {
        HashMap<String, String> requestedURIs = new HashMap<String,String>();
        try {
            FileInputStream fileIn = new FileInputStream("requested.log");
            ObjectInputStream in = new ObjectInputStream(fileIn);
            requestedURIs = (HashMap<String, String>) in.readObject();
            in.close();
            fileIn.close();
        }
        catch (IOException e) {
            // do nothing
        }
        return requestedURIs;
    }

    public static void saveRequestedURIs(Map<String,String> requestedURIs) throws IOException {
        FileOutputStream fileOut =
                new FileOutputStream("requested.log");
        ObjectOutputStream out = new ObjectOutputStream(fileOut);
        out.writeObject(requestedURIs);
        out.close();
        fileOut.close();
    }
}
