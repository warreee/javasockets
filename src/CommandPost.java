import java.util.Map;

/**
 * Created by warreee on 16/03/15.
 */
public class CommandPost extends Command {
    public CommandPost(String path, boolean http1, Map<String, String> info, String data) {
        super(path, http1, info, data);
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