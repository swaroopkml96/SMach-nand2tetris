import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class XMLHelper {

    File objectFile;
    FileWriter fw;
    BufferedWriter bw;

    public XMLHelper(File file) throws IOException {
        objectFile = file;
        fw = new FileWriter(objectFile);
        bw = new BufferedWriter(fw);
    }

    public void write(String s) throws IOException {
        bw.write(s);
        bw.newLine();
        bw.flush();
    }

    public void write(String type, String token) throws IOException {
        bw.write("<"+type+"> " + token + " </"+type+">");
        bw.newLine();
        bw.flush();
    }

}
