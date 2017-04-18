
public class Parser {

    //Parser takes trimmed lines, and returns appropriate asm code and no. of asm lines

    private String[] asm;
    private String currentFunctionName = "main";

    String[] parse(String s)
    {
        asm = new String[100];
        // Extract the individual words in a line
        String[] parts = s.split(" ");
        // Trim the individual words in a line
        // since gap between words may contain multiple spaces
        for(int i=0;i<parts.length;i++) parts[i]=parts[i].trim();
        // M - Memory access commands
        // A - Arithmetic  and logical commands
        // P - Program flow commands
        // F - Function calling commands
        if(isMCommand(parts[0])) parseM(parts);
        else if(isACommand(parts[0])) parseA(parts);
        else if(isPCommand(parts[0])) parseP(parts);
        else parseF(parts); // Subroutine command

        return asm;

    }

    private boolean isMCommand(String s)
    {
        // Memory access command
        boolean M = (s.equals("push") || s.equals("pop"));
        return M;
    }

    private boolean isACommand(String s)
    {
        // Arithmetic/Logical command
        boolean A = (s.equals("add") || s.equals("sub") || s.equals("neg") || s.equals("eq") || s.equals("gt") || s.equals("lt") || s.equals("and") || s.equals("or") || s.equals("not"));
        return A;
    }

    private boolean isPCommand(String s)
    {
        // Program flow command
        if(s.equals("label") || s.equals("goto") || s.equals("if-goto")) return true;
        else return false;
    }

    private void parseM(String[] s)
    {
        if(s[0].equals("push")) parsePUSH(s);
        else if(s[0].equals("pop")) parsePOP(s);
    }

    private void parseA(String[] s)
    {
        if(s[0].equals("add"))
        {
            // @SP
            // A=M-1	// Now A points to y
            // D=M		// now D holds y
            // @SP
            // A=M-1	// Now A points to y
            // A=A-1	// Now A points to x
            // M=M+D	// Now x holds x+y
            // @SP
            // M=M-1	// Decrement stack pointer

            asm[0] = "10";
            asm[1] = "@SP";
            asm[2] = "A=M-1";
            asm[3] = "D=M";
            asm[4] = "@SP";
            asm[5] = "A=M-1";
            asm[6] = "A=A-1";
            asm[7] = "M=M+D";
            asm[8] = "@SP";
            asm[9] = "M=M-1";
        }

        else if(s[0].equals("sub"))
        {
            // @SP
            // A=M-1	// Now A points to y
            // D=M		// now D holds y
            // @SP
            // A=M-1	// Now A points to y
            // A=A-1	// Now A points to x
            // M=M-D	// Now x is replaced by x-y
            // @SP
            // M=M-1	// Decrement stack pointer

            asm[0] = "10";
            asm[1] = "@SP";
            asm[2] = "A=M-1";
            asm[3] = "D=M";
            asm[4] = "@SP";
            asm[5] = "A=M-1";
            asm[6] = "A=A-1";
            asm[7] = "M=M-D";
            asm[8] = "@SP";
            asm[9] = "M=M-1";
        }

        else if(s[0].equals("neg"))
        {
            // @SP
            // A=M		// A points to front of stack
            // A=A-1	// A points to x
            // M=-M		// x holds -x

            asm[0] = "5";
            asm[1] = "@SP";
            asm[2] = "A=M";
            asm[3] = "A=A-1";
            asm[4] = "M=-M";
        }

        else if(s[0].equals("eq"))
        {
            // @SP
            // A=M-1	// Now A points to y
            // D=M		// now D holds y
            // @SP
            // A=M-1	// Now A points to y
            // A=A-1	// Now A points to x, ie, x is in M
            // D=M-D	// D holds x-y

            // @ifeq.<cmdNo> // cmdNo refers to the number of the vm command (a unique identifier since a single vm function may have multiple eq commands
            // D;JEQ
            // @SP
            // A=M-1	// A points to y
            // A=A-1	// A points to x
            // M=0		// M holds 0 or false
            // @eqend.<cmdNo>
            // 0;JMP

            // (ifeq.<cmdNo>)
            // @SP
            // A=M-1	// A points to y
            // A=A-1	// A points to x
            // M=-1		// M holds -1 or true

            // (eqend.<cmdNo>)
            // @SP
            // M=M-1	// Decrement stack pointer

            asm[0] = "24";
            asm[1] = "@SP";
            asm[2] = "A=M-1";
            asm[3] = "D=M";
            asm[4] = "@SP";
            asm[5] = "A=M-1";
            asm[6] = "A=A-1";
            asm[7] = "D=M-D";

            asm[8] = "@ifeq."+Integer.toString(VM.cmdNo);
            asm[9] = "D;JEQ";
            asm[10] = "@SP";
            asm[11] = "A=M-1";
            asm[12] = "A=A-1";
            asm[13] = "M=0";
            asm[14] = "@eqend."+Integer.toString(VM.cmdNo);
            asm[15] = "0;JMP";

            asm[16] = "(ifeq."+Integer.toString(VM.cmdNo)+")";
            asm[17] = "@SP";
            asm[18] = "A=M-1";
            asm[19] = "A=A-1";
            asm[20] = "M=-1";

            asm[21] = "(eqend."+Integer.toString(VM.cmdNo)+")";
            asm[22] = "@SP";
            asm[23] = "M=M-1";

            // NOTE: Even though we're using symbols like ifeq..., all symbols loaded using
            // A-instructions will already be defined to refer to ROM locations
            // in pass 1. No @symbol instruction is followed by a memory access
            // or write instruction, so that this implementation does not interfere
            // with the 'static' part of the VM memory.

        }

        else if(s[0].equals("gt"))
        {
            // @SP
            // A=M-1	// Now A points to y
            // D=M		// now D holds y
            // @SP
            // A=M-1 	// Now A points to y
            // A=A-1 	// Now A points to x, ie, x is in M
            // D=M-D 	// D holds x-y

            // @ifgt.<cmdNo> // cmdNo refers to the number of the vm command (a unique identifier since a single vm function may have multiple eq commands
            // D;JGT
            // @SP
            // A=M-1 	// A points to y
            // A=A-1 	// A points to x
            // M=0 		// M holds 0 or false
            // @gtend.<cmdNo>
            // 0;JMP

            // (ifgt.<cmdNo>)
            // @SP
            // A=M-1 	// A points to y
            // A=A-1 	// A points to x
            // M=-1 	// M holds -1 or true

            // (gtend.<cmdNo>)
            // @SP
            // M=M-1 	// Decrement stack pointer

            asm[0] = "24";
            asm[1] = "@SP";
            asm[2] = "A=M-1";
            asm[3] = "D=M";
            asm[4] = "@SP";
            asm[5] = "A=M-1";
            asm[6] = "A=A-1";
            asm[7] = "D=M-D";

            asm[8] = "@ifgt."+Integer.toString(VM.cmdNo);
            asm[9] = "D;JGT";
            asm[10] = "@SP";
            asm[11] = "A=M-1";
            asm[12] = "A=A-1";
            asm[13] = "M=0";
            asm[14] = "@gtend."+Integer.toString(VM.cmdNo);
            asm[15] = "0;JMP";

            asm[16] = "(ifgt."+Integer.toString(VM.cmdNo)+")";
            asm[17] = "@SP";
            asm[18] = "A=M-1";
            asm[19] = "A=A-1";
            asm[20] = "M=-1";

            asm[21] = "(gtend."+Integer.toString(VM.cmdNo)+")";
            asm[22] = "@SP";
            asm[23] = "M=M-1";
        }

        else if(s[0].equals("lt"))
        {
            // @SP
            // A=M-1 	// Now A points to y
            // D=M 		// now D holds y
            // @SP
            // A=M-1 	// Now A points to y
            // A=A-1 	// Now A points to x, ie, x is in M
            // D=M-D 	// D holds x-y

            // @iflt.<cmdNo> // cmdNo refers to the number of the vm command (a unique identifier since a single vm function may have multiple eq commands
            // D;JLT
            // @SP
            // A=M-1 	// A points to y
            // A=A-1 	// A points to x
            // M=0 		// M holds 0 or false
            // @ltend.<cmdNo>
            // 0;JMP

            // (iflt.<cmdNo>)
            // @SP
            // A=M-1 	// A points to y
            // A=A-1 	// A points to x
            // M=-1 	// M holds -1 or true

            // (ltend.<cmdNo>)
            // @SP
            // M=M-1 	// Decrement stack pointer

            asm[0] = "24";
            asm[1] = "@SP";
            asm[2] = "A=M-1";
            asm[3] = "D=M";
            asm[4] = "@SP";
            asm[5] = "A=M-1";
            asm[6] = "A=A-1";
            asm[7] = "D=M-D";

            asm[8] = "@iflt."+Integer.toString(VM.cmdNo);
            asm[9] = "D;JLT";
            asm[10] = "@SP";
            asm[11] = "A=M-1";
            asm[12] = "A=A-1";
            asm[13] = "M=0";
            asm[14] = "@ltend."+Integer.toString(VM.cmdNo);
            asm[15] = "0;JMP";

            asm[16] = "(iflt."+Integer.toString(VM.cmdNo)+")";
            asm[17] = "@SP";
            asm[18] = "A=M-1";
            asm[19] = "A=A-1";
            asm[20] = "M=-1";

            asm[21] = "(ltend."+Integer.toString(VM.cmdNo)+")";
            asm[22] = "@SP";
            asm[23] = "M=M-1";
        }

        else if(s[0].equals("and"))
        {
            // @SP
            // A=M-1 	// Now A points to y
            // D=M 		// now D holds y
            // @SP
            // A=M-1 	// Now A points to y
            // A=A-1 	// Now A points to x
            // M=M&D 	// Now x holds x AND y
            // @SP
            // M=M-1 	// Decrement stack pointer

            asm[0] = "10";
            asm[1] = "@SP";
            asm[2] = "A=M-1";
            asm[3] = "D=M";
            asm[4] = "@SP";
            asm[5] = "A=M-1";
            asm[6] = "A=A-1";
            asm[7] = "M=M&D";
            asm[8] = "@SP";
            asm[9] = "M=M-1";
        }

        else if(s[0].equals("or"))
        {
            // @SP
            // A=M-1	// Now A points to y
            // D=M 		// now D holds y
            // @SP
            // A=M-1 	// Now A points to y
            // A=A-1 	// Now A points to x
            // M=M|D 	// Now x holds x OR y
            // @SP
            // M=M-1 	// Decrement stack pointer

            asm[0] = "10";
            asm[1] = "@SP";
            asm[2] = "A=M-1";
            asm[3] = "D=M";
            asm[4] = "@SP";
            asm[5] = "A=M-1";
            asm[6] = "A=A-1";
            asm[7] = "M=M|D";
            asm[8] = "@SP";
            asm[9] = "M=M-1";
        }

        else
        {
            // s = "not"

            // @SP
            // A=M 		// A points to front of stack
            // A=A-1 	// A points to x
            // M=!M 	// x holds !x

            asm[0] = "5";
            asm[1] = "@SP";
            asm[2] = "A=M";
            asm[3] = "A=A-1";
            asm[4] = "M=!M";
        }
    }

    private void parsePUSH(String[] s)
    {
        if(s[1].equals("constant"))
        {
            // @<constant>	// constant is in s[2]
            // D=A 			// Put constant in D
            // @SP
            // A=M 			// A points to the front of stack
            // M=D 			// Put constant at the front of the stack
            // @SP
            // M=M+1 		// Increment stack pointer

            asm[0] = "8";
            asm[1] = "@"+s[2];
            asm[2] = "D=A";
            asm[3] = "@SP";
            asm[4] = "A=M";
            asm[5] = "M=D";
            asm[6] = "@SP";
            asm[7] = "M=M+1";
        }

        else if(s[1].equals("static"))
        {
            // The assembly command @Symbol associates 'Symbol' with an address 16 onwards,
            // if the pair don't already exist. This can be exploited by associating the
            // static index n (writing it as xxx.n so that it is recognized
            // as a symbol and not a number, where xxx = filename.) with an address.
            // Although 'pop static 8' does not store stack head in the 8th index
            // of the 'static' segment of the VM, any subsequent 'push static 8' will
            // do the right thing.

            // @static.<filename>.<index>	// Address associated with Symbol is loaded.
            // D=M					// D holds contents of static(n).
            // @SP
            // A=M					// A points to head of stack
            // M=D					// Put the static variable at the head of the stack
            // @SP
            // M=M+1 				// Increment Stack pointer.

            asm[0] = "8";
            asm[1] = "@static."+VM.fileName+"."+s[2];
            asm[2] = "D=M";
            asm[3] = "@SP";
            asm[4] = "A=M";
            asm[5] = "M=D";
            asm[6] = "@SP";
            asm[7] = "M=M+1";
        }

        else if(s[1].equals("local"))
        {
            // @<index>
            // D=A		// D holds index
            // @LCL		// LCL or R1 points to the segment in RAM that holds the currently executing function's local variables
            // A=M		// A points to 'local' segment base
            // A=A+D	// A points to local(index)
            // D=M		// D holds local(index)
            // @SP
            // A=M		// A points to stack head
            // M=D		// The required number has been written into stack head
            // @SP
            // M=M+1	//Increment the stack base

            asm[0] = "12";
            asm[1] = "@"+s[2];
            asm[2] = "D=A";
            asm[3] = "@LCL";
            asm[4] = "A=M";
            asm[5] = "A=A+D";
            asm[6] = "D=M";
            asm[7] = "@SP";
            asm[8] = "A=M";
            asm[9] = "M=D";
            asm[10] = "@SP";
            asm[11] = "M=M+1";
        }

        else if(s[1].equals("argument"))
        {
            // @<index>
            // D=A		// D holds index
            // @ARG		// ARG or R2 points to the segment in RAM that holds the currently executing function's arguments.
            // A=M		// A points to 'argument' segment base
            // A=A+D	// A points to argument(index)
            // D=M		// D holds argument(index)
            // @SP
            // A=M		// A points to stack head
            // M=D		// The required number has been written into stack head
            // @SP
            // M=M+1	//Increment the stack base

            asm[0] = "12";
            asm[1] = "@"+s[2];
            asm[2] = "D=A";
            asm[3] = "@ARG";
            asm[4] = "A=M";
            asm[5] = "A=A+D";
            asm[6] = "D=M";
            asm[7] = "@SP";
            asm[8] = "A=M";
            asm[9] = "M=D";
            asm[10] = "@SP";
            asm[11] = "M=M+1";
        }

        else if(s[1].equals("this"))
        {
            // 'this' or R3 points to a location in the heap
            // So 'push this 3' should load the data present in the 3rd position
            // of the 'this' segment. In this respect, it is similar to 'LCL' and 'ARG'

            // @<index>
            // D=A		// D holds index
            // @THIS
            // A=M		// A points to 'this' segment
            // A=A+D	// A points to this(index)
            // D=M		// D holds required number
            // @SP
            // A=M		// A points to stack head
            // M=D		// Required number stored at stack head
            // @SP
            // M=M+1	// Increment stack pointer

            asm[0] = "12";
            asm[1] = "@"+s[2];
            asm[2] = "D=A";
            asm[3] = "@THIS";
            asm[4] = "A=M";
            asm[5] = "A=A+D";
            asm[6] = "D=M";
            asm[7] = "@SP";
            asm[8] = "A=M";
            asm[9] = "M=D";
            asm[10] = "@SP";
            asm[11] = "M=M+1";
        }

        else if(s[1].equals("that"))
        {
            // Similar to 'this'

            asm[0] = "12";
            asm[1] = "@"+s[2];
            asm[2] = "D=A";
            asm[3] = "@THAT";
            asm[4] = "A=M";
            asm[5] = "A=A+D";
            asm[6] = "D=M";
            asm[7] = "@SP";
            asm[8] = "A=M";
            asm[9] = "M=D";
            asm[10] = "@SP";
            asm[11] = "M=M+1";
        }

        else if(s[1].equals("pointer"))
        {
            // The 'pointer' segment consists of THIS and THAT, i.e, R3 and R4

            // @<index>
            // D=A		// D holds index
            // @THIS
            // A=A+D	// If index=0, A points to this. Else if index=1, A points to that.
            // D=M		// Now D holds contents of this or that
            // @SP
            // A=M		// A points to stack head
            // M=D		// contents of this or that is stored at stack head
            // @SP
            // M=M+1	// Increment stack pointer

            asm[0] = "11";
            asm[1] = "@"+s[2];
            asm[2] = "D=A";
            asm[3] = "@THIS";
            asm[4] = "A=A+D";
            asm[5] = "D=M";
            asm[6] = "@SP";
            asm[7] = "A=M";
            asm[8] = "M=D";
            asm[9] = "@SP";
            asm[10] = "M=M+1";
        }

        else
        {
            // s[1] is "temp"
            // The 'temp' segment lies between R5 and R12

            // @<index>
            // D=A		// D holds index
            // @R5		// R5 is the base of the 'temp' segment
            // A=A+D	// A points to temp(index)
            // D=M		// D holds required number
            // @SP
            // A=M		// A points to stack head
            // M=D		// Required number is written into stack
            // @SP
            // M=M+1	//Increment stack pointer

            asm[0] = "11";
            asm[1] = "@"+s[2];
            asm[2] = "D=A";
            asm[3] = "@R5";
            asm[4] = "A=A+D";
            asm[5] = "D=M";
            asm[6] = "@SP";
            asm[7] = "A=M";
            asm[8] = "M=D";
            asm[9] = "@SP";
            asm[10] = "M=M+1";
        }
    }

    private void parsePOP(String[] s)
    {
        if(s[1].equals("static"))
        {
            // Here too, we exploit the symbol-managing feature of the assembler

            // @SP
            // A=M-1		// A points to first element of stack
            // D=M			// First element of stack is stored in D
            // @<filename>.<index>
            // M=D			// First element of stack has been popped into a location in static segment.
            // @SP
            // M=M-1		// Decrement stack pointer

            asm[0] = "8";
            asm[1] = "@SP";
            asm[2] = "A=M-1";
            asm[3] = "D=M";
            asm[4] = "@static."+VM.fileName+"."+s[2];
            asm[5] = "M=D";
            asm[6] = "@SP";
            asm[7] = "M=M-1";
        }

        else if(s[1].equals("local"))
        {
            // @<index>
            // D=A		// index is stored in D
            // @LCL
            // D=M+D	// D has address of local(index)
            // @SP
            // A=M		// A points to stack head
            // M=D		// Stack head holds address of local(index)
            // A=A-1	// A points to the number to be popped
            // D=M		// D holds the required number
            // A=A+1	// A points to the stack head, which contains the address of local(index)
            // A=M		// A points to local(index)
            // M=D		// Required number written to local(index)
            // @SP
            // M=M-1	// Decrement stack pointer

            asm[0] = "15";
            asm[1] = "@"+s[2];
            asm[2] = "D=A";
            asm[3] = "@LCL";
            asm[4] = "D=M+D";
            asm[5] = "@SP";
            asm[6] = "A=M";
            asm[7] = "M=D";
            asm[8] = "A=A-1";
            asm[9] = "D=M";
            asm[10] = "A=A+1";
            asm[11] = "A=M";
            asm[12] = "M=D";
            asm[13] = "@SP";
            asm[14] = "M=M-1";
        }

        else if(s[1].equals("argument"))
        {
            // @<index>
            // D=A		// index is stored in D
            // @ARG
            // D=M+D	// D has address of argument(index)
            // @SP
            // A=M		// A points to stack head
            // M=D		// Stack head holds address of argument(index)
            // A=A-1	// A points to the number to be popped
            // D=M		// D holds the required number
            // A=A+1	// A points to the stack head, which contains the address of argument(index)
            // A=M		// A points to argument(index)
            // M=D		// Required number written to argument(index)
            // @SP
            // M=M-1	// Decrement stack pointer

            asm[0] = "15";
            asm[1] = "@"+s[2];
            asm[2] = "D=A";
            asm[3] = "@ARG";
            asm[4] = "D=M+D";
            asm[5] = "@SP";
            asm[6] = "A=M";
            asm[7] = "M=D";
            asm[8] = "A=A-1";
            asm[9] = "D=M";
            asm[10] = "A=A+1";
            asm[11] = "A=M";
            asm[12] = "M=D";
            asm[13] = "@SP";
            asm[14] = "M=M-1";
        }

        else if(s[1].equals("this"))
        {
            // @<index>
            // D=A		// index is stored in D
            // @THIS
            // D=M+D	// D has address of this(index)
            // @SP
            // A=M		// A points to stack head
            // M=D		// Stack head holds address of this(index)
            // A=A-1	// A points to the number to be popped
            // D=M		// D holds the required number
            // A=A+1	// A points to the stack head, which contains the address of this(index)
            // A=M		// A points to this(index)
            // M=D		// Required number written to this(index)
            // @SP
            // M=M-1	// Decrement stack pointer

            asm[0] = "15";
            asm[1] = "@"+s[2];
            asm[2] = "D=A";
            asm[3] = "@THIS";
            asm[4] = "D=M+D";
            asm[5] = "@SP";
            asm[6] = "A=M";
            asm[7] = "M=D";
            asm[8] = "A=A-1";
            asm[9] = "D=M";
            asm[10] = "A=A+1";
            asm[11] = "A=M";
            asm[12] = "M=D";
            asm[13] = "@SP";
            asm[14] = "M=M-1";
        }

        else if(s[1].equals("that"))
        {
            // @<index>
            // D=A		// index is stored in D
            // @ARG
            // D=M+D	// D has address of that(index)
            // @SP
            // A=M		// A points to stack head
            // M=D		// Stack head holds address of that(index)
            // A=A-1	// A points to the number to be popped
            // D=M		// D holds the required number
            // A=A+1	// A points to the stack head, which contains the address of that(index)
            // A=M		// A points to that(index)
            // M=D		// Required number written to that(index)
            // @SP
            // M=M-1	// Decrement stack pointer

            asm[0] = "15";
            asm[1] = "@"+s[2];
            asm[2] = "D=A";
            asm[3] = "@THAT";
            asm[4] = "D=M+D";
            asm[5] = "@SP";
            asm[6] = "A=M";
            asm[7] = "M=D";
            asm[8] = "A=A-1";
            asm[9] = "D=M";
            asm[10] = "A=A+1";
            asm[11] = "A=M";
            asm[12] = "M=D";
            asm[13] = "@SP";
            asm[14] = "M=M-1";
        }

        else if(s[1].equals("pointer"))
        {
            // @SP
            // A=M-1		// A points to first element in stack
            // D=M			// D holds number to be popped
            // if(index==0) @THIS. else @THAT
            // M=D			// Required number is stored in this or that
            // @SP
            // M=M-1		// Decrement stack pointer

            asm[0] = "8";
            asm[1] = "@SP";
            asm[2] = "A=M-1";
            asm[3] = "D=M";
            if(Integer.parseInt(s[2])==0) asm[4] = "@THIS";
            else asm[4] = "@THAT";
            asm[5] = "M=D";
            asm[6] = "@SP";
            asm[7] = "M=M-1";
        }

        else
        {
            // s[1] is "temp"

            // @<index>
            // D=A		// index is stored in D
            // @R5		// temp segment starts from R5
            // D=A+D	// D has address of temp(index)
            // @SP
            // A=M		// A points to stack head
            // M=D		// Stack head holds address of temp(index)
            // A=A-1	// A points to the number to be popped
            // D=M		// D holds the required number
            // A=A+1	// A points to the stack head, which contains the address of temp(index)
            // A=M		// A points to temp(index)
            // M=D		// Required number written to temp(index)
            // @SP
            // M=M-1	// Decrement stack pointer

            asm[0] = "15";
            asm[1] = "@"+s[2];
            asm[2] = "D=A";
            asm[3] = "@R5";
            asm[4] = "D=A+D";
            asm[5] = "@SP";
            asm[6] = "A=M";
            asm[7] = "M=D";
            asm[8] = "A=A-1";
            asm[9] = "D=M";
            asm[10] = "A=A+1";
            asm[11] = "A=M";
            asm[12] = "M=D";
            asm[13] = "@SP";
            asm[14] = "M=M-1";
        }
    }

    private void parseP(String[] s)
    {
        if(s[0].equals("label"))
        {
            // (label.<functionName>.s[1])	// Creates a label whose scope is the current function.
            // hence the requirement for function name.

            asm[0] = "2";
            asm[1] = "(label."+currentFunctionName+"."+s[1]+")";
        }

        else if(s[0].equals("goto"))
        {
            // @<functionName>.s[1]
            // 0;JMP

            asm[0] = "3";
            asm[1] = "@label."+currentFunctionName+"."+s[1];
            asm[2] = "0;JMP";
        }

        if(s[0].equals("if-goto"))
        {
            // @SP
            // A=M-1	// A points to topmost element in stack
            // D=M		// D holds topmost element
            // @SP
            // M=M-1	// Decrement stack pointer
            // @ifgotoend.<cmdNo>
            // D;JEQ	// Jumps to end if M(top of stack) has 0.
            // @label.<currentFunctionName>.s[1]
            // 0;JMP	// Jumps to specified label
            // (ifgotoend.<cmdNo>)

            asm[0] = "11";
            asm[1] = "@SP";
            asm[2] = "A=M-1";
            asm[3] = "D=M";
            asm[4] = "@SP";
            asm[5] = "M=M-1";
            asm[6] = "@ifgotoend."+Integer.toString(VM.cmdNo);
            asm[7] = "D;JEQ";
            asm[8] = "@label."+currentFunctionName+"."+s[1];
            asm[9] = "0;JMP";
            asm[10] = "(ifgotoend."+Integer.toString(VM.cmdNo)+")";

        }
    }
    private void parseF(String[] s)
    {
        if(s[0].equals("call"))
        {
            // s[2] contains no. of arguments. These arguments are already at the top of the stack (courtesy of the calling function)
            // Next, push the return location which is at the end of this code segment
            // Then save LCL, ARG, THIS and THAT of the calling function.
            // Then, set LCL to SP and ARG (to SP-1(for THAT)-1(THIS)-1(LCL)-1(ARG)-1(return addr)-n(no. of args, in s[2])
            // Then jump to f (in s[1])
            // We expect the 'function' command to take care of pushing and zeroing the local segment

            // push return addr
            // @returnaddr.<cmdNo>
            // D=A      // Return addr stored in D
            // @SP
            // A=M
            // M=D      // Return addr pushed onto stack
            // @SP
            // M=M+1    // Stack pointer incremented

            // push LCL,ARG,THIS,THAT

            // @LCL
            // D=M      // Lcl segment pointer stored in D
            // @SP
            // A=M
            // M=D      // Lcl segment pointer pushed onto stack
            // @SP
            // M=M+1    // Stack pointer incremented

            // @ARG
            // D=M      // Arg segment pointer stored in D
            // @SP
            // A=M
            // M=D      // Arg segment pointer pushed onto stack
            // @SP
            // M=M+1    // Stack pointer incremented

            // @THIS
            // D=M      // This segment pointer stored in D
            // @SP
            // A=M
            // M=D      // This segment pointer pushed onto stack
            // @SP
            // M=M+1    // Stack pointer incremented

            // @THAT
            // D=M      // That segment pointer stored in D
            // @SP
            // A=M
            // M=D      // That segment pointer pushed onto stack
            // @SP
            // M=M+1    // Stack pointer incremented

            // Set LCL to SP, ARG to SP-5-n where n is no. of arguments

            // @SP
            // D=M      // SP stored in D
            // @LCL
            // M=D      // LCL set to SP

            // @SP
            // D=M      // SP stored in D
            // @5
            // D=D-A    // SP-5 stored in D
            // @s[2]    // s[2] holds n, no. of args
            // D=D-A    // SP-5-n stored in D
            // @ARG
            // M=D      // ARG set to SP-5-n

            // Jump to called function
            // @function.s[1]
            // 0;JMP

            // (returnaddr.cmdNo)   // <- this is the point where return code jumps back to.

            asm[0] = "51";
            asm[1] = "@returnaddr." + VM.cmdNo;
            asm[2] = "D=A";
            asm[3] = "@SP";
            asm[4] = "A=M";
            asm[5] = "M=D";
            asm[6] = "@SP";
            asm[7] = "M=M+1";
            asm[8] = "@LCL";
            asm[9] = "D=M";
            asm[10] = "@SP";
            asm[11] = "A=M";
            asm[12] = "M=D";
            asm[13] = "@SP";
            asm[14] = "M=M+1";
            asm[15] = "@ARG";
            asm[16] = "D=M";
            asm[17] = "@SP";
            asm[18] = "A=M";
            asm[19] = "M=D";
            asm[20] = "@SP";
            asm[21] = "M=M+1";
            asm[22] = "@THIS";
            asm[23] = "D=M";
            asm[24] = "@SP";
            asm[25] = "A=M";
            asm[26] = "M=D";
            asm[27] = "@SP";
            asm[28] = "M=M+1";
            asm[29] = "@THAT";
            asm[30] = "D=M";
            asm[31] = "@SP";
            asm[32] = "A=M";
            asm[33] = "M=D";
            asm[34] = "@SP";
            asm[35] = "M=M+1";
            asm[36] = "@SP";
            asm[37] = "D=M";
            asm[38] = "@LCL";
            asm[39] = "M=D";
            asm[40] = "@SP";
            asm[41] = "D=M";
            asm[42] = "@5";
            asm[43] = "D=D-A";
            asm[44] = "@" + s[2];
            asm[45] = "D=D-A";
            asm[46] = "@ARG";
            asm[47] = "M=D";
            asm[48] = "@function." + s[1];
            asm[49] = "0;JMP";
            asm[50] = "(returnaddr." + VM.cmdNo + ")";


        }

        else if(s[0].equals("function"))
        {
            // Create a label to which calling functions may jump to.
            // Push l zeroes into the stack where l is the no. of local variables (in s[2])

            // (function.s[1])
            // @s[2]
            // D=A                                  // l is stored in D
            // (pushinglocalsloop.functionName)
            // @pushinglocalsfinished.functionName
            // D;JEQ                                // If D holds 0, jump to finished
            // @SP
            // A=M
            // M=0                                  // pushed one zero
            // @SP
            // M=M+1
            // D=D-1                                // Decrement D
            // @pushinglocalsloop.functionName
            // 0;JMP                                // jump to loop
            // (pushinglocalsfinished.functionName)

            asm[0] = "16";
            asm[1] = "(function." + s[1] + ")";
            asm[2] = "@" + s[2];
            asm[3] = "D=A";
            asm[4] = "(pushinglocalsloop." + s[1] + ")";
            asm[5] = "@pushinglocalsfinished." + s[1];
            asm[6] = "D;JEQ";
            asm[7] = "@SP";
            asm[8] = "A=M";
            asm[9] = "M=0";
            asm[10] = "@SP";
            asm[11] = "M=M+1";
            asm[12] = "D=D-1";
            asm[13] = "@pushinglocalsloop." + s[1];
            asm[14] = "0;JMP";
            asm[15] = "(pushinglocalsfinished." + s[1] + ")";

            currentFunctionName = s[1];
        }

        else
        {
            // s[0] is return

            // Pop return value, which is at the top of the stack, into ARG[0], which will be the top of the working stack of called function
            // Set SP to LCL of called function, i.e., ignore the whole local segment
            // Copy ARG of called function into a temp. var (say temp[0])
            // Pop THAT,THIS,ARG,LCL of calling function which are now at the top of stack
            // Store return address in a temp. var (say temp[1])
            // Set SP to temp[0]+1 (i.e, to ARG[0]+1(for return value))
            // Jump to temp[0] (i.e., to return addr)

            // @SP
            // A=M-1
            // D=M          // D holds return value
            // @ARG
            // A=M
            // M=D          // ARG[0] holds return value

            // @LCL
            // D=M          // D holds pointer to LCL[0]
            // @SP
            // M=D          // SP points to LCL[0]

            // @ARG
            // D=M          // D holds pointer to ARG[0]
            // @R5          // temp[0]
            // M=D          // temp[0] holds pointer to ARG[0]


            // @SP
            // A=M-1        // A points to first element in stack, which is now THAT of called function
            // D=M          // D holds THAT
            // @THAT
            // M=D          // THAT holds THAT of called function
            // @SP
            // M=M-1        // Decrement SP

            // @SP
            // A=M-1        // A points to first element in stack, which is now THIS of called function
            // D=M          // D holds THIS
            // @THIS
            // M=D          // THIS holds THIS of called function
            // @SP
            // M=M-1        // Decrement SP

            // @SP
            // A=M-1        // A points to first element in stack, which is now ARG of called function
            // D=M          // D holds ARG
            // @ARG
            // M=D          // ARG holds ARG of called function
            // @SP
            // M=M-1        // Decrement SP

            // @SP
            // A=M-1        // A points to first element in stack, which is now LCL of called function
            // D=M          // D holds LCL
            // @LCL
            // M=D          // LCL holds LCL of called function
            // @SP
            // M=M-1        // Decrement SP

            // @SP
            // A=M-1        // A points to first element in stack, which is now the return addr
            // D=M          // D holds return addr
            // @R6          // temp[1]
            // M=D          // temp[1] holds return addr
            // @SP
            // M=M-1        // Decrement SP

            // @R5          // temp[0], which holds ARG
            // D=M          // D points to ARG[0]
            // @SP
            // M=D+1        // Now SP points to ARG[0]+1

            // @R6
            // A=M          // A holds return addr
            // 0;JMP        // jump to calling function

             asm[0]="57";
             asm[1]="@SP";
             asm[2]="A=M-1";
             asm[3]="D=M";
             asm[4]="@ARG";
             asm[5]="A=M";
             asm[6]="M=D";
             asm[7]="@LCL";
             asm[8]="D=M";
             asm[9]="@SP";
             asm[10]="M=D";
             asm[11]="@ARG";
             asm[12]="D=M";
             asm[13]="@R5";
             asm[14]="M=D";
             asm[15]="@SP";
             asm[16]="A=M-1";
             asm[17]="D=M";
             asm[18]="@THAT";
             asm[19]="M=D";
             asm[20]="@SP";
             asm[21]="M=M-1";
             asm[22]="@SP";
             asm[23]="A=M-1";
             asm[24]="D=M";
             asm[25]="@THIS";
             asm[26]="M=D";
             asm[27]="@SP";
             asm[28]="M=M-1";
             asm[29]="@SP";
             asm[30]="A=M-1";
             asm[31]="D=M";
             asm[32]="@ARG";
             asm[33]="M=D";
             asm[34]="@SP";
             asm[35]="M=M-1";
             asm[36]="@SP";
             asm[37]="A=M-1";
             asm[38]="D=M";
             asm[39]="@LCL";
             asm[40]="M=D";
             asm[41]="@SP";
             asm[42]="M=M-1";
             asm[43]="@SP";
             asm[44]="A=M-1";
             asm[45]="D=M";
             asm[46]="@R6";
             asm[47]="M=D";
             asm[48]="@SP";
             asm[49]="M=M-1";
             asm[50]="@R5";
             asm[51]="D=M";
             asm[52]="@SP";
             asm[53]="M=D+1";
             asm[54]="@R6";
             asm[55]="A=M";
             asm[56]="0;JMP";



        }


    }
}
