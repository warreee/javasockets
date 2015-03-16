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

    public abstract String getResponse();

    public boolean mustClose() {
        return this.mustClose;
    }

    protected boolean isBadRequest() {
        if (! http1)
            return false;
        else
            return ! info.containsKey("host"); // TODO: ook controleren of info.get("Host") == [hostnaam] ??
    }

    protected String getCurrentDate() {
        TimeZone timeZone = TimeZone.getTimeZone("Europe/Brussels");
        Locale locale = new Locale("en", "US");
        Calendar calendar = Calendar.getInstance(timeZone, locale);
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        return format.format(calendar.getTime());
    }

    protected long getNbBytes(String string) {
        byte[] byteArray = string.getBytes();
        return byteArray.length;
    }

    protected String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

}
