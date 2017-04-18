
public class SymbolTable {

    // A symbol table is just an array of Strings containing the symbols
    // and an array of integers containing their values.
    // They are linked by their common index.

    public String[] symbols;
    public int[] values = new int[10000];
    public int noOfSymbols = 23;



    public SymbolTable()
    {
        symbols = new String[10000];
        // Some pre-defined symbols
        symbols[0] = "SP"; values[0] 	= 0;
        symbols[1] = "LCL"; values[1] = 1;
        symbols[2] = "ARG"; values[2] = 2;
        symbols[3] = "THIS"; values[3] = 3;
        symbols[4] = "THAT"; values[4] = 4;
        symbols[5] = "SCREEN"; values[5] = 16384;
        symbols[6] = "KBD"; values[6] = 24576;
        symbols[7] = "R0"; values[7] = 0;
        symbols[8] = "R1"; values[8] = 1;
        symbols[9] = "R2"; values[9] = 2;
        symbols[10] = "R3"; values[10] = 3;
        symbols[11] = "R4"; values[11] = 4;
        symbols[12] = "R5"; values[12] = 5;
        symbols[13] = "R6"; values[13] = 6;
        symbols[14] = "R7"; values[14] = 7;
        symbols[15] = "R8"; values[15] = 8;
        symbols[16] = "R9"; values[16] = 9;
        symbols[17] = "R10"; values[17] = 10;
        symbols[18] = "R11"; values[18] = 11;
        symbols[19] = "R12"; values[19] = 12;
        symbols[20] = "R13"; values[20] = 13;
        symbols[21] = "R14"; values[21] = 14;
        symbols[22] = "R15"; values[22] = 15;
    }

    public String symbolAt(int i)
    {
        return symbols[i];
    }

    public int valueAt(int i)
    {
        return values[i];
    }

    public void addSymbol(String s)
    {
        symbols[noOfSymbols]=s;
    }

    public void addValue(int n)
    {
        values[noOfSymbols]=n;
    }

}
