/**
 * Created by warreee on 16/03/15.
 */
public class CommandParser {

    public Command parseCommand(String command){

        String mainCommand = command.substring(command.indexOf(" "));

        switch (mainCommand){
            case "HEAD": break;
            case "GET": break;
            case "POST": break;
        }
        return null;
    }

}
