import java.util.Map;

/**
 * Created by warreee on 16/03/15.
 */
public abstract class Command {

    private String commandString;
    private String path;
    private boolean http1;
    Map<String, String> info;


    public Command(String commandString, String path, boolean http1, Map<String, String> info) {
        this.commandString = commandString;
        this.path = path;
        this.http1 = http1;
        this.info = info;
    }

    public abstract String getResponse();

    public abstract boolean mustClose();


}
