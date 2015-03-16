import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
                // Alles in lijn per lijn opsplitsen:
                String[] commandLines = LineString(command);
                // De eerste lijn bevat de core info
                String[] param = commandLines[0].split(" ");
                String path = param[1];
                boolean http1 = http1(param[2]);
                Map<String, String> info = null;
                if (commandLines.length > 1) {
                    info = getInfo(Arrays.copyOfRange(commandLines, 1, commandLines.length - 1));
                }
                String data = null;
                return new CommandHead(path, http1,info);
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
        String mainCommand = command.substring(0, command.indexOf(" "));

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
        List<String> strList = Arrays.asList(data);
        return StringUtils.join(strList, "\r\n");
    }

    private Map<String, String> getInfo(String[] info) {

        HashMap<String, String> infoMap = new HashMap<>();

        for (String item: info){
            String key = item.substring(0, item.indexOf(": ")).toLowerCase();
            String value = item.substring(item.indexOf(": ") + 2);
            infoMap.put(key, value);
        }

        return infoMap;
    }

}
