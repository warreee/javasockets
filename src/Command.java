import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by warreee on 16/03/15.
 */
public abstract class Command {

    protected String path;
    protected boolean http1;
    protected Map<String, String> info;
    protected String data;

    protected boolean mustClose = false;

    public Command(String path, boolean http1, Map<String, String> info, String data) {
        this.data = data;
        this.path = path;
        this.http1 = http1;
        this.info = info;
    }

    public abstract byte[] getResponse();

    public boolean mustClose() {
        return this.mustClose;
    }

    protected boolean isBadRequest() {
        if (! http1)
            return false; // TODO
        else
            return ! info.containsKey("host"); // TODO: ook controleren of info.get("Host") == [hostnaam] ??
    }

    protected String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        format.setTimeZone(TimeZone.getTimeZone("GMT"));
        return format.format(calendar.getTime());
    }

    protected byte[] readFile(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    /**
     * Write bytes to file (overwrite file)
     */
    protected void writeToFile(String path, byte[] content) throws IOException {

        // create dirs
        File dir = (new File(path)).getParentFile();
        dir.mkdirs();

        // create file
        File file = new File(path);
        file.createNewFile();

        // first delete file if it exists
        if (file.exists())
            file.delete();

        FileOutputStream fos = new FileOutputStream(path);
        fos.write(content);
        fos.close();

    }

}
