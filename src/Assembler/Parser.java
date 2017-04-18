
public class Parser {

    public int pass;
    private int nextRamAddress = 16;
    private int nextRomAddress = 0;
    private String mc=null;
    Coder myCoder = new Coder();
    SymbolManager mySymbolManager = new SymbolManager();


    public String parse(String s)
    {
        if(pass==2)
        {
            if(s.startsWith("@"))
                this.parseA(s);
            else if(s.startsWith("("))
                // Do nothing with L-instructions in pass 2.
                // Set mc to null. Otherwise it will be returned containing the previous parse code.
                mc=null;
            else
                this.parseC(s);
        }
        else{
            // If pass = 1.
            if(s.startsWith("@"))
                // An A-instruction. Do nothing but increment ROM.
                this.nextRomAddress++;
            else if(s.startsWith("("))
                // An L-instruction. Parse it (since pass=1)
                this.parseL(s);
            else
                // A C-instruction. Do nothing but increment ROM
                this.nextRomAddress++;
        }

        return mc;
    }

    private void parseA(String s)
    {

        // Parses A-instructions
        int n; // Holds the address in base 10.
        // Strip off the '@'
        String constOrSym = s.substring(1);
        if(this.isSym(constOrSym)) // If symbol (not constant)
        {
            if(mySymbolManager.symbolExists(constOrSym))
                n = mySymbolManager.getValueOf(constOrSym);
            else {
                mySymbolManager.add(constOrSym,this.nextRamAddress);
                n = this.nextRamAddress;
                this.nextRamAddress++;
            }
            this.mc=this.to16bit(Integer.toBinaryString(n));
        }
        else
            this.mc=this.to16bit(Integer.toBinaryString(Integer.parseInt(constOrSym)));

    }

    private void parseL(String s)
    {
        // Parses L-instructions
        // Strip off the '()'
        String label = s.replace('(',' ');
        label = label.replace(')',' ');
        label = label.trim();
        // Add symbol to table
        mySymbolManager.add(label,this.nextRomAddress);
        System.out.println("Added ("+label+","+this.nextRomAddress+") to the symbol table");

    }

    private void parseC(String s)
    {
        // Parses C-instructions
        String dest=null;
        String jmp=null;
        String comp=null;

        String[] sf1 = s.split("=");

        String[] sf2 = s.split(";");

        if(sf1.length<2)
        {
            // The instruction didn't contain destination
            // Then, we take sf2, which should contain comp and jump fields
            comp=myCoder.getCompCodeOf(sf2[0]);
            jmp=myCoder.getJmpCodeOf(sf2[1]);
            this.mc="111"+comp+"000"+jmp;
        }
        else if(sf2.length<2)
        {
            // The instruction didn't contain jump field
            // Then we take sf1, which should contain dest and comp fields
            dest=myCoder.getDestCodeOf(sf1[0]);
            comp=myCoder.getCompCodeOf(sf1[1]);
            this.mc="111"+comp+dest+"000";
        }
        else{
            // Contains both dest and jump fields.
            // sf1[0] contains dest, sf2[1] contains jmp field.
            String sf3[] = sf1[1].split(";");
            // sf3[0] contains comp field
            dest=myCoder.getDestCodeOf(sf1[0]);
            jmp=myCoder.getJmpCodeOf(sf2[1]);
            comp=myCoder.getCompCodeOf(sf3[0]);
            this.mc="111"+comp+dest+jmp;
        }
    }

    private boolean isSym(String s)
    {
        // takes a String and determines if it is a symbol or a number
        if(s.charAt(0)<48 || s.charAt(0)>57) return true;
        else return false;
    }

    private String to16bit(String s)
    {
        // Takes a String containing a binary number and prefixes enough zeroes to make it 16bit
        char[] zeroes = {'0','0','0','0','0','0','0','0','0','0','0','0','0','0','0','0'};
        char[] inputString = s.toCharArray();
        for(int i=0;i<s.length();i++)
        {
            zeroes[16-s.length()+i]=inputString[i];
        }
        return String.copyValueOf(zeroes);
    }

}


