
public class SymbolManager {

    SymbolTable ST = new SymbolTable();

    public boolean symbolExists(String s)
    {
        // Returns true if symbol already exists in the symbol table.
        boolean found = false;
        for(int i=0;i<ST.noOfSymbols;i++)
            if(s.equals(ST.symbolAt(i))) found=true;
        return found;
    }
    public int getValueOf(String s)
    {
        // Returns the value associated with the symbol.
        int n = -1;
        for(int i=0;i<ST.noOfSymbols;i++)
            if(s.equals(ST.symbolAt(i))) n=ST.valueAt(i);

        return n;
    }
    public void add(String s, int n)
    {
        // Adds the pair s and n to the symbol table.
        ST.addSymbol(s);
        ST.addValue(n);
        ST.noOfSymbols++;
    }

}
