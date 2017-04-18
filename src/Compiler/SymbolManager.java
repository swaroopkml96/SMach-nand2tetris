public class SymbolManager {

    private int writeHead = 0;
    private int readHead = 0;

    private String name;
    private String type;
    private String segment;
    private int index;

    private String[] names = new String[1000];
    private String[] types = new String[1000];
    private String[] segments = new String[1000];
    private int[] indices = new int[1000];

    void setName(String name){
        this.name=name;
    }

    void setType(String type){
        this.type=type;
    }

    void setSegment(String segment){
        this.segment=segment;
    }

    void setIndex(int index){
        this.index=index;
    }

    void add(){
        names[writeHead] = name;
        types[writeHead] = type;
        segments[writeHead] = segment;
        indices[writeHead] = index;
        writeHead++;
    }

    String getSegmentIndex(String varName) {
        boolean found = this.search(varName);
        if(found) return segments[readHead]+" "+indices[readHead];
        else return "notFound";
    }


    String getType(String varName) {
        boolean found = this.search(varName);
        if(found) return types[readHead];
        else return "notFound";
    }

    private boolean search(String varName){

        // It is important that we search the symbol table in reverse.
        // Because, consider
        //                          method1(x,y,z){var int a,b,c;}
        //                          method2(z,y,x){var int c,b,a;}

        // If the table is searched from 0 on, 'x <--> argument 0' will appear before 'z <--> argument 0'
        // so that, even in method2, x will always appear mapped to argument 0.

        readHead = writeHead -1;
        for(readHead = writeHead -1; readHead >=0; readHead--){
            if(names[readHead].equals(varName)){
                return true;
            }
        }
        return false;
    }

    boolean isVar(String varName){
        return (this.search(varName));
    }

    void printTable(){
        System.out.println();
        for(int i = 0; i< writeHead; i++){
            System.out.println(names[i]+" ("+types[i]+")"+" <---> "+segments[i]+" "+indices[i]);
        }
    }
}
