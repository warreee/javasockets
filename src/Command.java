/**
 * Created by warreee on 16/03/15.
 */
public abstract class Command {

    private String commandString;

    public Command(String commandString) {
        this.commandString = commandString;

    }

    public abstract String getResponse();


}
