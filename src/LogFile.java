import java.io.*;

/**
 * Created by arnedebrabandere on 2/03/15.
 */
public class LogFile {

    private String path;

    public LogFile(String path) {
        this.path = path;
    }

    public void addLine(String line) {
        try {
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(path, true)));
            out.println(line);
            out.close();
        } catch(Exception e) {
            System.out.println("Could not write to log file ("+path+")");
        }
    }

}
