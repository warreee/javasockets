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

    /**
     * @return command object of given command string
     */
    public Command parseCommand(String command){

        String[] commandLines;
        String[] param;
        String path;
        boolean http1;
        int emptyLineNumber;
        Map<String, String> info;
        String data;

        switch (getMainCommand(command)) {
            case HEAD:
                // Alles in lijn per lijn opsplitsen:
                commandLines = LineString(command);
                // De eerste lijn bevat de core info
                param = commandLines[0].split(" ");
                path = param[1];
                http1 = http1(param[2]);
                info = getInfo(commandLines, 1, commandLines.length);
                return new CommandHead(path, http1,info);
            case POST:
                // Alles in lijn per lijn opsplitsen:
                commandLines = LineString(command);
                // De eerste lijn bevat de core info
                param = commandLines[0].split(" ");
                path = param[1];
                http1 = http1(param[2]);
                // Zoek de lege lijn na de info headers
                emptyLineNumber = getEmptyLineNumber(commandLines);
                info = getInfo(commandLines, 1, emptyLineNumber/* - 1*/);
                // Data teruggeven als 1 grote string
                data = getData(commandLines, emptyLineNumber + 1, commandLines.length/* - 1*/);
                return new CommandPost(path, http1, info, data);
            case GET:
                // Alles in lijn per lijn opsplitsen:
                commandLines = LineString(command);
                // De eerste lijn bevat de core info
                param = commandLines[0].split(" ");
                path = param[1];
                http1 = http1(param[2]);
                info = getInfo(commandLines, 1, commandLines.length/* -1*/);
                return new CommandGet(path, http1, info);
            case PUT:
                // Alles in lijn per lijn opsplitsen:
                commandLines = LineString(command);
                // De eerste lijn bevat de core info
                param = commandLines[0].split(" ");
                path = param[1];
                http1 = http1(param[2]);
                // Zoek de lege lijn na de info headers
                emptyLineNumber = getEmptyLineNumber(commandLines);
                info = getInfo(commandLines, 1, emptyLineNumber/* - 1*/);
                // Data teruggeven als 1 grote string
                data = getData(commandLines, emptyLineNumber + 1, commandLines.length/* - 1*/);
                return new CommandPut(path, http1, info, data);
        }

        return null;
    }

    /**
     * Geeft de het lijnnummer terug van de lege lijn na de info headers.
     */
    private int getEmptyLineNumber(String[] emptyLines) {

        int emptyLine = -1;

        for (int i = 0; i < emptyLines.length; i++){
            if(emptyLines[i].equals("")){
                emptyLine = i;
                break;
            }
        }
        return emptyLine;
    }

    /**
     * Returns true if the server has to continue reading the command:
     *  - for HEAD and GET: as long as no empty line is received
     *  - for PUT and POST: as long as the data is not received
     */
    public boolean continueReading(String command){
        if (getMainCommand(command).equals(MainCommand.GET) || getMainCommand(command).equals(MainCommand.HEAD)){
            return getEmptyLineNumber(LineString(command)) == -1;
        } else {
            return ! dataReceived(command);
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
                //command = command.substring(4, command.length() - 1); //Removes mainCommand
                //System.out.println(command);
                return MainCommand.HEAD;
            case "GET":
                //command = command.substring(3, command.length() - 1); //Removes mainCommand
                return MainCommand.GET;
            case "POST":
                //command = command.substring(4, command.length() - 1); //Removes mainCommand
                return MainCommand.POST;
            case "PUT":
                //command = command.substring(3, command.length() - 1); //Removes mainCommand
                return MainCommand.PUT;
        }
        return null;
    }

    /**
     * Command string to array of lines
     */
    private String[] LineString(String command) {
        String[] lines = command.split("\\r?\\n");
        return lines;
    }

    /**
     * true if HTTP/1.1 is used
     */
    private boolean http1(String http1){
        if (http1.equalsIgnoreCase("HTTP/1.1")){
           return true;
        } else {
            return false;
        }
    }

    private String getData(String[] data, int startIndex, int endIndex) {
        data = Arrays.copyOfRange(data, startIndex, endIndex);
        List<String> strList = Arrays.asList(data);
        return StringUtils.join(strList, "\r\n");
    }

    /**
     * Get the info map ("Key: value")
     */
    private Map<String, String> getInfo(String[] info, int startIndex, int endIndex) {

        info = Arrays.copyOfRange(info, startIndex, endIndex);

        HashMap<String, String> infoMap = new HashMap<>();

        if (info.length > 0) {

            for (String item : info) {
                String key = item.substring(0, item.indexOf(": ")).toLowerCase();
                String value = item.substring(item.indexOf(": ") + 2);
                infoMap.put(key, value);
            }
        }

        return infoMap;
    }

    /**
     * Check if the data is received (for a PUT or POST command)
     */
    private boolean dataReceived(String command) {
        // Alles in lijn per lijn opsplitsen:
        String[] commandLines = LineString(command);
        // Zoek de lege lijn na de info headers
        int emptyLineNumber = getEmptyLineNumber(commandLines);
        if (emptyLineNumber == -1) { // info headers not received yet...
            return false;
        }
        else {
            Map<String, String> info = getInfo(commandLines, 1, emptyLineNumber);
            // Content-Length opvragen
            int contentLength = Integer.parseInt(info.get("content-length"));
            // Data teruggeven als 1 grote string
            String data = getData(commandLines, emptyLineNumber + 1, commandLines.length);
            // bereken aantal bytes in data
            int nbBytes = data.getBytes().length;
            return nbBytes >= contentLength;
        }
    }

}
