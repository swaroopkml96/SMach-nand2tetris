import java.io.IOException;

public class TokenBuffer {

    // Tokenizer (The Source) .........Buffer.......... The Abyss
    //                              ^
    //                           pointer
    // advance(): Advance towards the light. If you have reached the source, pray the Tokenizer for a new Token.
    // retreat(): Retreat into the abyss


    private int bufferSize = 10;
    private int pointer = 0;
    private Tokenizer tz;
    private String[] tokens = new String[bufferSize];
    private String[] types = new String[bufferSize];

    public TokenBuffer(Tokenizer t) throws IOException {
        tz=t;
        this.advance();
    }

    void advance() throws IOException {

        if(pointer>0){
            pointer--;
        }
        else{
            for(int i = bufferSize - 1; i>0; i-- ){
                tokens[i] = tokens[i - 1];
                types[i] = types[i-1];
            }
            tokens[0] = tz.getCurrentToken();
            types[0] = tz.getCurrentTokenType();
            tz.advance();
        }
    }

    void retreat(){
        if(pointer< bufferSize - 1) pointer++;
    }

    String getCurrentToken(){
        return tokens[pointer];
    }

    String getCurrentTokenType(){
        return types[pointer];
    }

}
