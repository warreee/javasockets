import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

/**
 * Created by warreee on 16/03/15.
 */
public abstract class Command {

    protected String path;
    protected boolean http1;
    protected Map<String, String> info;
    protected String data;


    public Command(String path, boolean http1, Map<String, String> info, String data) {
        this.data = data;
        this.path = path;
        this.http1 = http1;
        this.info = info;
    }

    public abstract String getResponse();

    public abstract boolean mustClose();

    protected boolean isBadRequest() {
        if (http1)
            return true;
        else
            return info.containsKey("host"); // TODO: ook controleren of info.get("Host") == [hostnaam] ??
    }

    protected String getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");
        return format.format(calendar.getTime());
    }

}
