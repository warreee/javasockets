import java.util.Map;

/**
 * Created by warreee on 16/03/15.
 */
public class CommandGet extends Command {
    public CommandGet(String path, boolean http1, Map<String, String> info) {
        super(path, http1, info, null);
    }

    @Override
    public String getResponse() {
        return null;
    }

    @Override
    public boolean mustClose() {
        return true;
    }
}
