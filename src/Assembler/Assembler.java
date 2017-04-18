
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class Assembler {

    public static void main(String[] args) {

        // For the first pass, set pass = 1
        // Read each line in file
        // Call Parser on the string
        // Parser returns the machine code string after consulting SymbolManager and (for C-instructions), Coder.
        // Write this string into the object file.
        // Repeat again for pass 2, setting pass = 2

        String sourceFile = args[0];
        String objectFile = args[1];


        try{

            FileReader fr = null;
            BufferedReader br = null;
            FileWriter fw = null;
            BufferedWriter bw = null;
            Parser myParser = new Parser();

            String currentLine = null;
            for(int i=1; i<3; i++)
            {
                fr = new FileReader(sourceFile);
                br = new BufferedReader(fr);
                fw = new FileWriter(objectFile);
                bw = new BufferedWriter(fw);
                // Set pass.
                // If pass=1, only L-instructions are processed.
                // Else only A- and C- instructions are processed.
                myParser.pass=i;
                System.out.println("\n\n\t Pass "+i+" \t\n");
                while(true)
                {
                    currentLine = br.readLine();
                    // break if EOF
                    if(currentLine==null) break;
                    currentLine = currentLine.trim();
                    // If empty, continue
                    if(currentLine.isEmpty()) continue;
                    // continue if comment
                    if(currentLine.startsWith("//")) continue;
                    // Take the part of the line before '//'
                    currentLine = currentLine.split("//")[0];
                    currentLine = currentLine.trim();
                    String machineCode = myParser.parse(currentLine);
                    if(machineCode==null) continue;
                    System.out.println(machineCode+"\t<---\t"+currentLine);
                    bw.write(machineCode);
                    bw.newLine();
                    bw.flush();
                }
            }
            br.close();
            bw.close();
        }
        catch(IOException e){}

    }

}


