import java.io.*;

public class Compiler {

    public static void main(String[] args) throws IOException {

        File sourceFolder = new File(args[0]);
        File[] sourceFiles = new File[100];
        File[] destFiles = new File[100];
        File[] vmFiles = new File[100];

        if(sourceFolder.isFile() && sourceFolder.getName().endsWith("jack")){
            // The argument is actually a single file.
            sourceFiles[0] = sourceFolder;
            destFiles[0] = new File(sourceFiles[0].getAbsolutePath().replaceAll(".jack",".xml"));
            vmFiles[0] = new File(sourceFiles[0].getAbsolutePath().replaceAll(".jack",".vm"));
        }

        else{
            // The argument is a folder possibly containing many files
            FileFilter filter = new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if(file.getName().endsWith(".jack")) return true;
                    else return false;
                }
            };

            sourceFiles = sourceFolder.listFiles(filter);

            for(int i=0;i<sourceFiles.length;i++){
                if(sourceFiles[i]==null) break;
                else{
                    destFiles[i] = new File(sourceFiles[i].getAbsolutePath().replaceAll(".jack",".xml"));
                    vmFiles[i] = new File(sourceFiles[i].getAbsolutePath().replaceAll(".jack",".vm"));
                }
            }
        }

        // For each file in sourceFiles
        for(int i=0;i<sourceFiles.length;i++){
            if(sourceFiles[i]==null) break;

            Tokenizer tz = new Tokenizer(sourceFiles[i]);
            XMLHelper xml = new XMLHelper(destFiles[i]);
            VMHelper vm = new VMHelper(vmFiles[i]);

            // Tokenizer, given a source file, provides a stream of tokens and their types
            // The XMLHelper, given an xml file, provides services to print text into it.
            // A Parser, given a tokenizer an XMLHelper and a VMHelper, will create a parse tree, which it saves in the form of an xml file, and generates and writes the VM code.
            // The parser makes use of a TokenBuffer, which is an abstraction over the Tokenizer, and maintains a buffer of the token and type streams so that a token can be unread.
            // The Grammar class is a class that provides a useful set of methods that help with matters of grammar.

            Parser myParser = new Parser(tz,xml,vm);
            myParser.parse();
        }
    }
}
