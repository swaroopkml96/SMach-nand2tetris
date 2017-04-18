import java.io.*;

public class VM {

    public static int cmdNo = 0;
    public static int asmNo = 0;
    public static String fileName = null;

    public static void main(String[] args) throws IOException {

        // VM takes each line in the file (goes through each file in a directory)
        // and passes it as a string to Parser. Parser checks which type of command
        // it is and returns an array of strings containing assembly code.
        // Note: the first string in this array contains the no. of asm lines

        File objectFile = new File(args[1]);
        File sourceFolder = new File(args[0]);  // Holds the directory containing vm files or path of a single vm file
        File sourceFiles[] = new File[100];     // Will hold the array of vm files

        FileWriter fw = new FileWriter(objectFile);
        BufferedWriter bw = new BufferedWriter(fw);
        FileReader fr = null;
        BufferedReader br = null;

        VM.injectBootstrapCode(bw);
        asmNo+=52;

        Parser myParser = new Parser();

        if (sourceFolder.isFile() && sourceFolder.getName().endsWith(".vm")) {
            fileName = sourceFolder.getName();
            sourceFiles[0] = sourceFolder;  // i.e, the argument is not a directory but a single vm file

        }
        else {

            // Else, sourceFolder is a directory
            // Then, use a FileFilter which accepts only vm files to store only the vm Files in sourceFiles.
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File file) {

                    if (file.isFile() && file.getName().endsWith(".vm")) return true;
                    else return false;
                }
            };

            sourceFiles = sourceFolder.listFiles(filter);

        }


        // For each file in sourceFiles,
        for (File vmFile : sourceFiles) {

            if(vmFile==null) break;
            fileName = vmFile.getName();
            System.out.println("\nFile " + vmFile.getAbsolutePath() + " : " + fileName + "\n");

            fr = new FileReader(vmFile);
            br = new BufferedReader(fr);

            String currentLine;

            while (true) {
                currentLine = br.readLine();
                // break if EOF
                if (currentLine == null) break;
                currentLine = currentLine.trim();
                // if empty, continue
                if (currentLine.isEmpty()) continue;
                // continue if comment
                if (currentLine.startsWith("//")) continue;
                // Take the part of the line before '//'
                currentLine = currentLine.split("//")[0];
                // Remove terminal spaces
                currentLine = currentLine.trim();
                String[] asm = myParser.parse(currentLine);

                System.out.println("\n" + cmdNo + ": " + currentLine + "\n");
                cmdNo++;
                for (int i = 1; i < Integer.parseInt(asm[0]); i++) {
                    bw.write(asm[i]);
                    bw.newLine();
                    bw.flush();

                    boolean Linstruction = asm[i].startsWith("(");
                    System.out.println("\t" + (Linstruction? " " : asmNo + ": ") + asm[i]);
                    // Do not count L-instructions. This does not affect other parts of the code since it is only used
                    // for displaying in the console. Further, not counting L-ins is important because L-ins disappear
                    // after assembly
                    if(!Linstruction) asmNo++;
                }
            }
        }

    }

    private static void injectBootstrapCode(BufferedWriter bw) {

        try {

            // BootstrapCode will be injected into ROM at 0x0000. It will set SP and call the Sys.init() function.
            // Sys.init's contract is that it will call the main function with required arguments and enter into an inf. loop.


            String[] bootstrapCode = new String[60];

            // Sys.init() will never return, so there is no need to push return addr, LCL, ARG, THIS, THAT.
            // Also, Sys.init() does not use local variables, nor does it take arguments (therefore no need to set LCL and ARG segments.
            // But we do all these so as to preserve uniformity.

            // @256
            // D=A
            // @0
            // M=D      // SP set to 256

            // @returnaddr.bs
            // D=A      // D holds return addr
            // @SP
            // A=M      // A points to stack head
            // M=D      // return addr pushed
            // @SP
            // M=M+1    // SP incremented

            // @LCL
            // D=M      // D points to LCL
            // @SP
            // A=M      // A points to stack head
            // M=D      // LCL pointer pushed
            // @SP
            // M=M+1    // SP incremented

            // @ARG
            // D=M      // D points to ARG
            // @SP
            // A=M      // A points to stack head
            // M=D      // ARG pointer pushed
            // @SP
            // M=M+1    // SP incremented

            // @THIS
            // D=M      // D points to THIS
            // @SP
            // A=M      // A points to stack head
            // M=D      // THIS pointer pushed
            // @SP
            // M=M+1    // SP incremented

            // @THAT
            // D=M      // D points to THAT
            // @SP
            // A=M      // A points to stack head
            // M=D      // THAT pointer pushed
            // @SP
            // M=M+1    // SP incremented

            // @SP
            // D=M      // SP stored in D
            // @LCL
            // M=D      // LCL set to SP

            // @SP
            // D=M      // SP stored in D
            // @5
            // D=D-A    // SP-5 stored in D. n=0, since Sys.init has no arguments
            // @ARG
            // M=D      // ARG set to SP-5

            // @function.Sys.init
            // 0;JMP
            // (returnaddr.bs)  // This is the point to which Sys.init() should have returned to.


            bootstrapCode[0] = "@256";
            bootstrapCode[1] = "D=A";
            bootstrapCode[2] = "@0";
            bootstrapCode[3] = "M=D";
            bootstrapCode[4] = "@returnaddr.bs";
            bootstrapCode[5] = "D=A";
            bootstrapCode[6] = "@SP";
            bootstrapCode[7] = "A=M";
            bootstrapCode[8] = "M=D";
            bootstrapCode[9] = "@SP";
            bootstrapCode[10] = "M=M+1";
            bootstrapCode[11] = "@LCL";
            bootstrapCode[12] = "D=M";
            bootstrapCode[13] = "@SP";
            bootstrapCode[14] = "A=M";
            bootstrapCode[15] = "M=D";
            bootstrapCode[16] = "@SP";
            bootstrapCode[17] = "M=M+1";
            bootstrapCode[18] = "@ARG";
            bootstrapCode[19] = "D=M";
            bootstrapCode[20] = "@SP";
            bootstrapCode[21] = "A=M";
            bootstrapCode[22] = "M=D";
            bootstrapCode[23] = "@SP";
            bootstrapCode[24] = "M=M+1";
            bootstrapCode[25] = "@THIS";
            bootstrapCode[26] = "D=M";
            bootstrapCode[27] = "@SP";
            bootstrapCode[28] = "A=M";
            bootstrapCode[29] = "M=D";
            bootstrapCode[30] = "@SP";
            bootstrapCode[31] = "M=M+1";
            bootstrapCode[32] = "@THAT";
            bootstrapCode[33] = "D=M";
            bootstrapCode[34] = "@SP";
            bootstrapCode[35] = "A=M";
            bootstrapCode[36] = "M=D";
            bootstrapCode[37] = "@SP";
            bootstrapCode[38] = "M=M+1";
            bootstrapCode[39] = "@SP";
            bootstrapCode[40] = "D=M";
            bootstrapCode[41] = "@LCL";
            bootstrapCode[42] = "M=D";
            bootstrapCode[43] = "@SP";
            bootstrapCode[44] = "D=M";
            bootstrapCode[45] = "@5";
            bootstrapCode[46] = "D=D-A";
            bootstrapCode[47] = "@ARG";
            bootstrapCode[48] = "M=D";
            bootstrapCode[49] = "@function.Sys.init";
            bootstrapCode[50] = "0;JMP";
            bootstrapCode[51] = "(returnaddr.bs)";

            for(String code : bootstrapCode) {
                if(code==null) break;

                bw.write(code);
                bw.newLine();
                bw.flush();

            }

            System.out.println("\n\n[Injected BOOTSTRAP code]\n\n");
        }
        catch (IOException e) {
            System.out.println("File not found\n\n");
        }


    }


}