import java.util.HashMap;
import java.util.Map;

/**
 * Created by warreee on 16/03/15.
 */
public class CommandParser {



    public CommandParser(){

    }



    public Command parseCommand(String command){


        MainCommand mainCommand;

        switch (getMainCommand(command)) {
            case HEAD:
                String[] commandLines = LineString(command);
                String[] param = commandLines[0].split(" ");
                String path = param[1];
                boolean http1 = http1(commandLines[2]);
                Map<String, String> info = new HashMap<String, String>();
                String data = null;
                CommandHead head = new CommandHead(path, http1,info, data);
                break;
            case POST:

                break;
            case GET:
                System.out.println();
            case PUT:
                break;
        }

        return null;
    }

    /**
     * Returns false if its a GET or HEAD request, is used to see if we need to continue read the input stream.
     * @param command
     * @return
     */
    public boolean continueReading(String command){
        if (getMainCommand(command).equals(MainCommand.GET) || getMainCommand(command).equals(MainCommand.HEAD)){
            return false;
        } else {
            return true;
        }
    }

    private enum MainCommand {
        HEAD,
        POST,
        GET,
        PUT
    }

    /**
     * Geeft het hoofdcommando terug
     * @param command in stringvorm
     * @return
     */
    private MainCommand getMainCommand(String command){
        String mainCommand = command.substring(command.indexOf(" "));

        switch (mainCommand){
            case "HEAD":
                command = command.substring(4, command.length() - 1); //Removes mainCommand
                System.out.println(command);
                return MainCommand.HEAD;
            case "GET":
                command = command.substring(3, command.length() - 1); //Removes mainCommand
                return MainCommand.GET;
            case "POST":
                command = command.substring(4, command.length() - 1); //Removes mainCommand
                return MainCommand.POST;
            case "PUT":
                command = command.substring(3, command.length() - 1); //Removes mainCommand
                return MainCommand.PUT;
        }
        return null;
    }

    /**
     *
     * @param command
     * @return
     */
    private String[] LineString(String command) {
        String[] lines = command.split("\\r?\\n");
        return lines;
    }

    private boolean http1(String http1){
        if (http1.equalsIgnoreCase("HTTP/1.1")){
           return true;
        } else {
            return false;
        }
    }

    private String getData(String[] data) {
        return null;
    }

}
