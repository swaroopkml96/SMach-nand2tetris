import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;

public class Tokenizer {

    private String currentToken;
    private boolean foundEOF = false;
    private boolean currentTokenIsString = false;
    private PushbackReader pr;
    private Grammar myGrammar;

    public Tokenizer(File sourceFile) throws IOException {
        FileReader fr = new FileReader(sourceFile);
        pr = new PushbackReader(fr);
        myGrammar = new Grammar();
        this.advance();
    }

    void advance() throws IOException {

        char currentChar;
        char nextChar;
        int currentInt;
        int nextInt;

        // If next character is EOF, Tokenizer does not have more tokens. Returns null.
        // If comment beginning detected, skip through the comment and advance.
        // advance through white space
        // If StringConst beginning detected, collect all characters until end of String. That is our token.
        // If a Symbol(+,-,(,),...) is detected, that is our token.
        // If the above fail, it is some keyword or identifier not inside comment or String. Then, collect all characters until an empty space, a new line or a special symbol other than '_' is detected. That is our token.

        currentInt = pr.read();
        if(currentInt==-1 || foundEOF){
            // If the last token ended in some non EOF, currentInt will be EOF (-1).
            // If the last token ended in EOF, since -1 cannot be pushed back, foundEOF will have been set.
            // So we check if either of these is true.
            currentToken=null;
            return;
        }

        currentChar = (char) currentInt;
        nextInt = pr.read();
        nextChar = (char) nextInt;
        // If the next character is EOF, pr.read() returns -1. But pr.unread()
        // unreads -1, not EOF. Therefore, never unread a potential EOF.
        if(nextInt!=-1) pr.unread(nextInt);



        // If '//' comment, skip through to end of line.
        if (currentChar=='/' && nextChar=='/') {
            while (true) {
                // Move one character forward in file.
                currentInt=pr.read();
                if ( currentInt == 10) {
                    break;
                }
            }
            // Now advance one more step and return to caller w/o executing the rest of the code
            this.advance();
            return;
        }



        // If '/*' comment, skip through until '*/' is encountered.
        if(currentChar=='/' && nextChar=='*')
        {
           while(true){
                currentInt=pr.read();
                currentChar=(char)currentInt;
                nextInt=pr.read();
                nextChar=(char)nextInt;
                pr.unread(nextInt);
                if(currentChar=='*' && nextChar=='/') {
                    break;
                }
            }
            // Now advance one more step and return to caller w/o executing the rest of the code
            pr.read();
            pr.read();
            this.advance();
            return;

        }

        if(currentInt==32 || currentInt==9 || currentInt==10 || currentInt==13)
        {
            // If empty space, or tab space, or new line, or carriage return, calls itself. When it calls itself, next character is read(skipping current empty space)
            this.advance();
            return;
        }

        if(currentChar=='"')
        {
            currentToken = "";

            while(true) {
                currentInt=pr.read();
                currentChar=(char)currentInt;
                if(currentChar=='"') {
                    // End of string constant
                    break;
                }
                else {
                    currentToken = currentToken+String.valueOf(currentChar);
                }
            }
            this.currentTokenIsString=true;
            return;
        }


        if(myGrammar.isCharSymbol(currentChar))
        {
            currentToken = String.valueOf(currentChar);
            return;
        }


        // If the program has advanced this far w/o returning,
        // The current token is some identifier or keyword.

        currentToken="";
        while(true) {
            if(myGrammar.isCharSymbol(currentChar)
                    || currentInt==13
                    || currentInt==32
                    || currentInt==10
                    || currentInt==9
                    || currentInt==-1
                    ) {
                // If a CR, empty space, tab space, new line character, or EOF, end of token.
                break;
            }
            else {
                currentToken = currentToken + String.valueOf(currentChar);
            }
            currentInt=pr.read();
            currentChar=(char)currentInt;
        }
        if(currentInt==-1) foundEOF=true;
        else pr.unread(currentInt);
    }

    String getCurrentToken() throws IOException {
        return currentToken;
    }

    String getCurrentTokenType(){
        if(this.currentTokenIsString){
            this.currentTokenIsString=false;
            return "stringConstant";
        }
        else
            return myGrammar.getTokenType(currentToken);
    }
}
