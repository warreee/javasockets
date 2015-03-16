import java.util.Map;

/**
 * Created by warreee on 16/03/15.
 */
public class CommandHead extends Command {
    public CommandHead(String commandString, String path, boolean http1, Map<String, String> info) {
        super(commandString, path, http1, info);
    }

    @Override
    public String getResponse() {
        return null;
    }
}
