/**
 * Created by warreee on 16/03/15.
 */
public class CommandParser {

    public Command parseCommand(String command){

        String mainCommand = command.substring(command.indexOf(" "));

        switch (mainCommand){
            case "HEAD":
                command = command.substring(4, command.length() - 1); //Removes mainCommand
                break;
            case "GET":
                command = command.substring(3, command.length() - 1); //Removes mainCommand
                break;
            case "POST":
                command = command.substring(4, command.length() - 1); //Removes mainCommand
                break;
            case "PUT":
                command = command.substring(3, command.length() - 1); //Removes mainCommand
                break;
        }
        return null;
    }

}
