import java.util.Map;

/**
 * Created by warreee on 16/03/15.
 */
public class CommandPut extends Command {
    public CommandPut(String commandString, String path, boolean http1, Map<String, String> info) {
        super(commandString, path, http1, info);
    }

    @Override
    public String getResponse() {
        return null;
    }
}
