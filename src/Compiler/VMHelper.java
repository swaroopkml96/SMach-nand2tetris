import java.io.*;

public class VMHelper {

    FileWriter fw;
    BufferedWriter bw;

    public VMHelper(File vmfile) throws IOException {
        fw = new FileWriter(vmfile);
        bw = new BufferedWriter(fw);
    }

    public void writeln(String code) throws IOException {
        bw.write(code);
        bw.newLine();
        bw.flush();
        System.out.println(code);
    }

    public void write(String code) throws IOException {
        bw.write(code);
        bw.flush();
        System.out.print(code);
    }

    public void writenl() throws IOException {
        bw.newLine();
        bw.flush();
        System.out.print("\n");
    }

}
