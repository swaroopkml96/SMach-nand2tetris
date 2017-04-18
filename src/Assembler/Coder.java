
public class Coder {

    public String getDestCodeOf(String s) {
        char A = '0', D = '0', M = '0';
        for (char ch : s.toCharArray()) {
            // Code may be of the form 'AD=M+1', i.e, there may be more than one destination.
            // So it is essential we don't use if-else-if structure.
            if (ch == 'A') A = '1';
            if (ch == 'D') D = '1';
            if (ch == 'M') M = '1';
        }
        String code = String.valueOf(A) + String.valueOf(D) + String.valueOf(M);
        return code;
    }

    public String getJmpCodeOf(String s) {

        String code = "000";
        // Note: s=="JGT" didn't work.
        // Perhaps because though they're the same string, theye're different objects.
        if (s.equals("JGT")) code = "001";
        else if (s.equals("JEQ")) code = "010";
        else if (s.equals("JGE")) code = "011";
        else if (s.equals("JLT")) code = "100";
        else if (s.equals("JNE")) code = "101";
        else if (s.equals("JLE")) code = "110";
        else if (s.equals("JMP")) code = "111";

        return code;
    }

    public String getCompCodeOf(String s) {
        char a = '0';
        String str = null;
        boolean M = s.contains("M");
        boolean D = s.contains("D");
        boolean one = s.contains("1");
        boolean plus = s.contains("+");
        boolean minus = s.contains("-");
        boolean and = s.contains("&");
        boolean or = s.contains("|");
        boolean zero = s.contains("0");
        boolean threeChars = (s.length() == 3);
        boolean twoChars = (s.length() == 2);
        if (M) a = '1';
        if (threeChars) {
            if (!one) {
                if (plus) str = "000010";
                if (minus) {
                    if (s.startsWith("D")) str = "010011";
                    else str = "000111";
                }
                if (and) str = "000000";
                if (or) str = "010101";
            } else {
                if (plus) {
                    if (s.startsWith("D")) str = "011111";
                    else str = "110111";
                }
                if (minus) {
                    if (s.startsWith("D")) str = "001110";
                    else str = "110010";
                }
            }

        } else if (twoChars) {
            if (minus) {
                if (D) str = "001111";
                else if (one) str = "111010";
                else str = "110011";
            } else {
                if (D) str = "001101";
                else str = "110001";
            }
        } else {
            if (D) str = "001100";
            else if (one) str = "111111";
            else if (zero) str = "101010";
            else str = "110000";
        }
        return String.valueOf(a) + str;
    }
}