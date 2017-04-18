import java.io.IOException;

public class Parser {

    private TokenBuffer tb;
    private XMLHelper xml;
    private VMHelper vm;
    private Grammar myGrammar = new Grammar();
    private SymbolManager mysm = new SymbolManager();

    private int statementNo;
    private int staticIndex;
    private int fieldIndex;
    private int argumentIndex;
    private int localIndex;
    private int noOfArgs;

    private String className;
    private boolean isMethod;
    private boolean isConstructor;

    public Parser(Tokenizer t, XMLHelper x, VMHelper v) throws IOException {
        tb = new TokenBuffer(t);
        xml=x;
        vm=v;
    }

    void parse() throws IOException {
        // This is where it all begins, the deep, dark descent into the terminals.
        parseClass();
    }

    private void parseClass() throws IOException {
        // Grammar: 'class' className '{' classVarDec* subroutineDec* '}'

        // Sets the class name. We use it to name functions inside this class.
        // Also, in a class, staticIndex and fieldIndex should begin from 0.
        // Set statementNo to 0. It will hold the total no. of statements encountered.

        statementNo=0;
        staticIndex=0;
        fieldIndex=0;

        xml.write("<class>");

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'class'
        tb.advance();

        className = tb.getCurrentToken();

        this.parseclassName();   // for className
        tb.advance();
        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '{'
        tb.advance();

        // There may be 0 or more class variable declarations.
        while(myGrammar.isclassVarDec(tb.getCurrentToken())){
            this.parseclassVarDec();
            tb.advance();
        }

        // There may be 0 or more subroutine declarations
        while(myGrammar.issubroutineDec(tb.getCurrentToken())){
            this.parsesubroutineDec();
            tb.advance();
        }

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '}'

        xml.write("</class>");
        mysm.printTable();
    }

    private void parseclassVarDec() throws IOException {
        // Grammar: ('static'|'field') type varName (',' varName)* ';'

        // Associates, using the symbol table, each static variable with 'static <index>' and each field variable with 'this <index>'.

        xml.write("<classVarDec>");
        if(tb.getCurrentToken().equals("static")){
            // A line of static variable declarations
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'static'

            tb.advance();
            this.parsetype();

            tb.advance();

            // Associate variable with 'static <index>'
            mysm.setSegment("static");
            mysm.setIndex(staticIndex);
            mysm.setName(tb.getCurrentToken());
            mysm.add();
            staticIndex++;

            this.parsevarName();

            tb.advance();
            while(tb.getCurrentToken().equals(",")){
                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ','
                tb.advance();

                // Associate variable with 'static <index>'
                mysm.setIndex(staticIndex);
                mysm.setName(tb.getCurrentToken());
                mysm.add();
                staticIndex++;

                this.parsevarName();
                tb.advance();
            }

            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ';'
        }

        else{
            // A line of fields
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'field'

            tb.advance();
            this.parsetype();

            tb.advance();

            // Associate variable with 'this <index>'
            mysm.setSegment("this");
            mysm.setIndex(fieldIndex);
            mysm.setName(tb.getCurrentToken());
            mysm.add();
            fieldIndex++;

            this.parsevarName();
            tb.advance();
            while(tb.getCurrentToken().equals(",")){
                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ','
                tb.advance();

                // Associate variable with 'this <index>'
                mysm.setIndex(fieldIndex);
                mysm.setName(tb.getCurrentToken());
                mysm.add();
                fieldIndex++;

                this.parsevarName();
                tb.advance();
            }

            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ';'
        }

        xml.write("</classVarDec>");
    }

    private void parsetype() throws IOException {
        // Grammar: 'int'|'char'|'boolean'|className

        // This functions sets the type in the SymbolManager. Useful when adding something to the table. Harmless otherwise.

        if(tb.getCurrentToken().equals("int")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'int'
            mysm.setType("int");
        }

        else if(tb.getCurrentToken().equals("char")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'char'
            mysm.setType("char");
        }

        else if(tb.getCurrentToken().equals("boolean")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'boolean'
            mysm.setType("boolean");
        }

        else{
            mysm.setType(tb.getCurrentToken());
            this.parseclassName();
        }

        //xml.write("</type>");
    }

    private void parsesubroutineDec() throws IOException {
        // Grammar: ('constructor'|'function'|'method') ('void'|type) subroutineName '(' parameterList ')' subroutineBody

        // It sets the subroutineName and writes 'function <classname>.<subroutinename>' into the vm file.
        // The no. of local variables used is counted and appended to this line by parsevarDec() which is called by parsesubroutineBody(), which is called by this function.
        // Each variable in the parameter list is associated with 'argument <index>'
        // Each local variable in the subroutine body is associated with 'local <index>'

        // If the subroutine is a method, isMethod will be set. Then, two things must happen.
        //  (1) The parameter list should begin associating parameters starting from argument 1, because argument 0 is the hidden object reference.
        //  (2) The first two commands in the body should effect the operations that set argument 0 as the 'this' pointer.

        // If the subroutine is a constructor, the first two commands should call Memory.alloc after pushing no. of fields of the class.

        xml.write("<subroutineDec>");

        if(tb.getCurrentToken().equals("constructor")){
            isConstructor=true;

            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'constructor'
            tb.advance();

            if(tb.getCurrentToken().equals("void")){
                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'void'
                tb.advance();
            }
            else{
                this.parsetype();
                tb.advance();
            }

            String subroutineName=tb.getCurrentToken();
            this.parsesubroutineName();

            vm.write("function "+className+"."+subroutineName+" ");

            tb.advance();

            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '('
            tb.advance();

            this.parseparameterList();
            tb.advance();

            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ')'
            tb.advance();

            this.parsesubroutineBody();

            isConstructor=false;
        }

        else if(tb.getCurrentToken().equals("function")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'function'
            tb.advance();

            if(tb.getCurrentToken().equals("void")){
                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'void'
                tb.advance();
            }
            else{
                this.parsetype();
                tb.advance();
            }
            String subroutineName=tb.getCurrentToken();
            this.parsesubroutineName();

            vm.write("function "+className+"."+subroutineName+" ");

            tb.advance();

            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '('
            tb.advance();

            this.parseparameterList();
            tb.advance();

            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ')'
            tb.advance();

            this.parsesubroutineBody();
        }

        else{
            // current token is "method"

            // Before parsing the subroutine, we set isMethod
            isMethod =true;

            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'constructor'
            tb.advance();

            if(tb.getCurrentToken().equals("void")){
                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'void'
                tb.advance();
            }
            else{
                this.parsetype();
                tb.advance();
            }

            String subroutineName=tb.getCurrentToken();
            this.parsesubroutineName();

            vm.write("function "+className+"."+subroutineName+" ");

            tb.advance();

            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '('
            tb.advance();

            this.parseparameterList();
            tb.advance();

            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ')'
            tb.advance();

            this.parsesubroutineBody();

            isMethod = false;
        }

        xml.write("</subroutineDec>");
    }

    private void parseparameterList() throws IOException {
        //Grammar: (type varName (',' type varName)*)?
        xml.write("<parameterList>");

        // Associates each variable in the parameter list with 'argument <index>'
        // Also, for each parameter list (i.e., for each function) argument index must begin from 0.

        argumentIndex=0;

        // If the subroutine whose parameter list this is, was a method, isMethod will be set.
        // Then we start argumentIndex from 1, since argument[0] will be the hidden object reference.
        if(isMethod) argumentIndex=1;

        // Note: istypeOrIdentifier() will return true not only if 'int', 'char', or 'boolean', but even if any identifier, in which case, it is assumed to be a className
        if(myGrammar.istypeOrIdentifier(tb.getCurrentToken())){
            this.parsetype();
            tb.advance();

            // Associate the variable with 'argument <index>'
            mysm.setSegment("argument");
            mysm.setIndex(argumentIndex);
            mysm.setName(tb.getCurrentToken());
            mysm.add();
            argumentIndex++;

            this.parsevarName();
            tb.advance();

            while(tb.getCurrentToken().equals(",")){
                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ','
                tb.advance();
                this.parsetype();
                tb.advance();

                // Associate the variable with 'argument <index>'
                mysm.setSegment("argument");
                mysm.setIndex(argumentIndex);
                mysm.setName(tb.getCurrentToken());
                mysm.add();
                argumentIndex++;

                this.parsevarName();
                tb.advance();
            }

            // The problem is, at the end, this will advance into the ')' which is not part of the parameterList
            // structure. So, we need to retreat.


        }
        tb.retreat();
        xml.write("</parameterList>");
    }

    private void parsesubroutineBody() throws IOException {
        // Grammar: '{' varDec* statements '}'

        // This function counts up and prints the no. of local variables used into the VM file. Adding of variables to table will be taken care of by parsevarDec()
        // For each subroutine body, local index must begin from 0.

        localIndex=0;

        xml.write("<subroutineBody>");

        int totalLocals = 0; // used to indicate total no. of local variables.
        int localsInOneLine;

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '{'
        tb.advance();

        while(myGrammar.isvarDec(tb.getCurrentToken())){
            localsInOneLine = this.parsevarDec();
            totalLocals += localsInOneLine; // total no. of local variables += local variables encountered in this line
            tb.advance();
        }

        vm.write(Integer.toString(totalLocals));
        vm.writenl();

        // If the the subroutine whose body this is, was a method, isMethod will be set.
        // Then, we write the code to receive the first argument as a hidden object reference.

        if(isMethod){
            vm.writeln("push argument 0");
            vm.writeln("pop pointer 0");
        }

        // If the subroutine whose body this is, was a constructor, isConstructor will be set.
        // Then, we push no. of field variables and call Memory.alloc
        if(isConstructor){
            vm.writeln("push constant "+fieldIndex);
            vm.writeln("call Memory.alloc 1"); // 1 means we have pushed 1 argument
            vm.writeln("pop pointer 0"); // Returned value is 'this'
        }
        this.parsestatements();
        tb.advance();

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '}'

        xml.write("</subroutineBody>");
    }

    private int parsevarDec() throws IOException {
        // Grammar: 'var' type varName (',' varName)* ';'

        // This function associates each variable with 'local <index>'

        xml.write("<varDec>");

        int localsInOneLine=1;

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'var'
        tb.advance();

        this.parsetype();
        tb.advance();

        mysm.setSegment("local");
        mysm.setIndex(localIndex);
        mysm.setName(tb.getCurrentToken());
        mysm.add();
        localIndex++;

        this.parsevarName();
        tb.advance();

        while(tb.getCurrentToken().equals(",")){
            localsInOneLine++;

            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ','
            tb.advance();

            mysm.setSegment("local");
            mysm.setIndex(localIndex);
            mysm.setName(tb.getCurrentToken());
            mysm.add();
            localIndex++;

            this.parsevarName();
            tb.advance();
        }

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ';'

        xml.write("</varDec>");

        return localsInOneLine;
    }

    private void parseclassName() throws IOException {
        // Grammar: identifier
        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for identifier
    }

    private void parsesubroutineName() throws IOException {
        // Grammar: identifier
        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for identifier
    }

    private void parsevarName() throws IOException {
        // Grammar: identifier
        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for identifier
    }

    private void parsestatements() throws IOException {
        // Grammar: statement*
        xml.write("<statements>");

        while(myGrammar.isstatement(tb.getCurrentToken())){
            this.parsestatement();
            tb.advance();
        }

        tb.retreat();
        xml.write("</statements>");
    }

    private void parsestatement() throws IOException {
        // Grammar: letStatement|ifStatement|whileStatement|doStatement|returnStatement

        statementNo++;

        if(tb.getCurrentToken().equals("let")) this.parseletStatement();
        else if(tb.getCurrentToken().equals("if")) this.parseifStatement();
        else if(tb.getCurrentToken().equals("while")) this.parsewhileStatement();
        else if(tb.getCurrentToken().equals("do")) this.parsedoStatement();
        else this.parsereturnStatement();
    }

    private void parseletStatement() throws IOException {
        // Grammar: 'let' varName('[' expression ']')? '=' expression ';'

        // Remember the varName
        //      If array, we parse the index-expression and pop top of stack(index) into temp 1.
        // Next, we parse the right-hand side expression. Result will be stored on top.
        // If it was an array, we push varName, push index(temp 1), add, pop into pointer 1, pop (the result of RH exp) into that 0
        // Else, pop into segment+index of varName.
        boolean lhsIsArray=false;

        xml.write("<letStatement>");

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'let'
        tb.advance();

        String varName=tb.getCurrentToken();

        this.parsevarName();
        tb.advance();

        if(tb.getCurrentToken().equals("[")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '['
            tb.advance();
            this.parseexpression();
            tb.advance();
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ']'
            tb.advance();

            vm.writeln("pop temp 1");
            lhsIsArray=true;
        }

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '='
        tb.advance();
        this.parseexpression();
        tb.advance();

        if(lhsIsArray){
            vm.writeln("push "+mysm.getSegmentIndex(varName)); //push base address of array
            vm.writeln("push temp 1");  // push index
            vm.writeln("add");
            vm.writeln("pop pointer 1");    // that now points to array[index]
            vm.writeln("pop that 0");   // pop result of rhs into that[0].
        }

        else {
            vm.writeln("pop "+mysm.getSegmentIndex(varName));
        }

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ';'

        xml.write("</letStatement>");
    }

    private void parseifStatement() throws IOException {
        // Grammar: 'if' '(' expression ')' '{' statements '}' ('else' '{' statements '}')?

        // When parseexpression() returns, top of stack is either true or false.
        // If ~(top of stack) is true, jump to else part.

        int ifNo=statementNo;

        xml.write("<ifStatement>");

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'if'
        tb.advance();

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '('
        tb.advance();

        this.parseexpression();
        tb.advance();

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ')'
        tb.advance();

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '{'
        tb.advance();

        // At this point, true or false is in top of stack
        vm.writeln("not");
        vm.writeln("if-goto elsePart."+ifNo);

        this.parsestatements();
        tb.advance();

        vm.writeln("goto continue."+ifNo);
        vm.writeln("label elsePart."+ifNo);

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '}'
        tb.advance();

        if(tb.getCurrentToken().equals("else")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'else'
            tb.advance();
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '{'
            tb.advance();

            vm.writeln("label elsePart."+ifNo);

            this.parsestatements();
            tb.advance();
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '}'
            tb.advance();
        }

        vm.writeln("label continue."+ifNo);

        // Here also, the token buffer is overstepped. So we need to retreat.
        tb.retreat();

        xml.write("</ifStatement>");
    }

    private void parsewhileStatement() throws IOException {
        // Grammar: 'while' '(' expression ')' '{' statements '}'
        // When parseexpression() returns, true or false is on the top of the stack.

        // label whileBeginning
        // <parseExpression>
        // neg
        // if-goto whileEnd
        // <parseStatements>
        // label whileEnd

        int whileNo=statementNo;

        xml.write("<whileStatement>");

        vm.writeln("label whileBeginning."+whileNo);

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'while'
        tb.advance();

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '('
        tb.advance();

        this.parseexpression();
        tb.advance();

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ')'
        tb.advance();

        vm.writeln("not");
        vm.writeln("if-goto whileEnd."+whileNo);

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '{'
        tb.advance();

        this.parsestatements();
        tb.advance();

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '}'

        vm.writeln("goto whileBeginning."+whileNo);
        vm.writeln("label whileEnd."+whileNo);

        xml.write("</whileStatement>");
    }

    private void parsedoStatement() throws IOException {
        // Grammar: 'do' subroutineCall ';'
        xml.write("<doStatement>");

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'do'
        tb.advance();

        this.parsesubroutineCall();
        tb.advance();

        // After the called function returns, ignore the return value.
        vm.writeln("pop temp 0");

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ';'

        xml.write("</doStatement>");
    }

    private void parsereturnStatement() throws IOException {
        // Grammar: 'return' expression? ';'

        // functions not returning anything SHOULD return 0.
        // If the call to a function that returns something is placed in a let statement, it will be popped into some variable.
        // If the call is placed in some expression, it will get consumed in some arithmetic/logical operation.
        // If the call is placed in a do statement, returned 0 will be popped out (ignored).

        xml.write("<returnStatement>");

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'return'
        tb.advance();

        if(!tb.getCurrentToken().equals(";")) {
            this.parseexpression();
            tb.advance();
            // Result of expression will be on the top of the stack.
            // return
            vm.writeln("return");
        }
        else // Returning nothing. So push 0 and return
        {
            vm.writeln("push constant 0");
            vm.writeln("return");
        }

        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ';'

        xml.write("</returnStatement>");
    }

    private void parseexpression() throws IOException {
        // Grammar: term (op term)*

        // If we find an operator, we remember it and write its corresponding command after the second term has been parsed.

        xml.write("<expression>");

        this.parseterm();
        tb.advance();

        while(myGrammar.isoperator(tb.getCurrentToken())){
            // Remember the operator
            String operator = tb.getCurrentToken();
            this.parseop();
            tb.advance();
            this.parseterm();
            tb.advance();
            // write command corresponding to operator
            vm.writeln(this.getOperatorCommand(operator));
        }

        tb.retreat();

        xml.write("</expression>");
    }

    private void parseterm() throws IOException {
        // Grammar: integerConstant | stringConstant | keywordConstant | varName | varName '[' expression ']' | subroutineCall | '(' expression ')' | unaryOp term
        xml.write("<term>");

        if(tb.getCurrentTokenType().equals("integerConstant")){
            this.parseintegerConstant();
        }

        else if(tb.getCurrentTokenType().equals("stringConstant")){
            this.parsestringConstant();
        }

        else if(myGrammar.iskeywordConstant(tb.getCurrentToken())){
            this.parsekeywordConstant();
        }

        // Lookahead is necessary to decide between varName, varName[expr], and subroutineCall (both subroutineName() and className.subroutineName()
        else if(tb.getCurrentTokenType().equals("identifier")){
            // look ahead
            tb.advance();
            String nextToken = tb.getCurrentToken();
            tb.retreat();

            if(nextToken.equals("[")){
                // varName '[' expr ']'
                // push the varName, which is a reference
                // after parsing the expression, index is on the top of the stack.
                // 'add'. Now, address of varName[index] is on the top of the stack
                // pop into pointer 1 and push 'that 0' onto the stack
                String varName = tb.getCurrentToken();
                vm.writeln("push "+mysm.getSegmentIndex(varName));

                this.parsevarName();
                tb.advance();
                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '['
                tb.advance();
                this.parseexpression();
                tb.advance();
                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ']'

                vm.writeln("add");
                vm.writeln("pop pointer 1");
                vm.writeln("push that 0");
            }

            else if(nextToken.equals("(") || nextToken.equals(".")){
                // subroutineCall
                this.parsesubroutineCall();
            }

            else{
                // simply varName
                // resolve the varName, and push onto stack
                vm.writeln("push "+mysm.getSegmentIndex(tb.getCurrentToken()));
                this.parsevarName();
            }
        }

        else if(tb.getCurrentToken().equals("(")){
            // '(' expression ')'
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '('
            tb.advance();
            this.parseexpression();
            tb.advance();
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ')'
        }

        else if(myGrammar.isunaryOp(tb.getCurrentToken())){
            // unaryOp term
            // Remember the operator, and write corresponding command after parsing the term.
            String unaryOp = tb.getCurrentToken();
            this.parseunaryOp();
            tb.advance();
            this.parseterm();
            // Now write the command
            vm.writeln(this.getUnaryOpCommand(unaryOp));
        }

        xml.write("</term>");
    }


    private void parsesubroutineCall() throws IOException {
        // Grammar: subroutineName '(' expressionList ')' | (className|varName).subroutineName '(' expressionList ')'
        // Here, we need to check if the second token is '(' or '.'

        // The called subroutine is a constructor/function if call is of the type className.subroutineName()
        // In that case, just call the function (the function will have the name className.subroutineName)

        // The called subroutine is a method (belongs to a particular object) if call is of the type
        //  (1) subroutineName() (method of same class) or (2) varName.subroutineName()
        // In this case, the caller should push a reference to the object as one of the arguments
        // That is, varName.subroutineName(arguments) is actually interpreted as className.subroutineName(varName,arguments).
        //                                                                            ^ className is prefixed since this is the function's real name.


        tb.advance();

        if (tb.getCurrentToken().equals(("("))) {
            // The first type, of the form subroutineName(), not className.subroutineName()
            // Then, the called subroutine is a method
            // Call className.subroutineName(this,otherArguments)
            tb.retreat();

            String subroutineName = tb.getCurrentToken();
            // push the object reference (in this case, 'this')
            vm.writeln("push pointer 0");
            this.parsesubroutineName();
            tb.advance();

            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '('
            tb.advance();

            this.parseexpressionList(); // parseexpressionList() will push the other arguments
            tb.advance();

            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ')'

            vm.writeln("call " + this.className + "." + subroutineName + " " + (noOfArgs + 1));
        }
        else {
            // The second type
            tb.retreat();

            if (mysm.isVar(tb.getCurrentToken())) {

                // This is of the type varName.subroutineName()
                // call className.subroutineName(objectReference,arguments)
                // where className is the 'type' of object variable, and objectReference is its segment+index in the symbol table

                String varName = tb.getCurrentToken();

                this.parseclassNameOrVarName();
                tb.advance();

                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '.'
                tb.advance();

                String subroutineName = tb.getCurrentToken();
                this.parsesubroutineName();
                tb.advance();

                String calledClassName = mysm.getType(varName);
                String objectReference = mysm.getSegmentIndex(varName);

                // Push object reference as one of the arguments
                vm.writeln("push "+objectReference);

                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '('
                tb.advance();

                this.parseexpressionList();
                tb.advance();

                // Now call the subroutine
                vm.writeln("call "+calledClassName+"."+subroutineName+" "+(noOfArgs+1));

                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ')'
            }

            else{
                // This is of the type className.subroutineName
                // Then, called function is a 'function'.
                // Object reference is not pushed as one of the arguments

                String calledClassName = tb.getCurrentToken();

                this.parseclassNameOrVarName();
                tb.advance();

                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '.'
                tb.advance();

                String subroutineName = tb.getCurrentToken();
                this.parsesubroutineName();
                tb.advance();

                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '('
                tb.advance();

                this.parseexpressionList();
                tb.advance();

                // Now call the subroutine
                vm.writeln("call "+calledClassName+"."+subroutineName+" "+(noOfArgs));

                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ')'
            }
        }

    }

    private void parseexpressionList() throws IOException {
        // Grammar: (expression (',' expression)*)?
        xml.write("<expressionList>");

        // We use a local variable called noOfArgs for a reason.
        // There may be nested function calls.
        // When the inner function call returns with noOfArgs=0, when control returns to the method parsing the outer function call, it has its own local noOfArgs not affected.
        int noOfArgs=0;

        // If the first token is ')', there are no expressions
        boolean atleastOneExpression = !tb.getCurrentToken().equals(")");

        if(atleastOneExpression){
            this.parseexpression();
            noOfArgs++;
            tb.advance();

            while(tb.getCurrentToken().equals(",")){
                noOfArgs++;
                xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for ','
                tb.advance();
                this.parseexpression();
                tb.advance();
            }
        }

        this.noOfArgs=noOfArgs;

        tb.retreat();

        xml.write("</expressionList>");
    }

    private void parseop() throws IOException {
        // Grammar: '+'|'-'|'*'|'/'|'&'|'|'|'<'|'>'|'='

        if(tb.getCurrentToken().equals("+")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '+'
        }

        else if(tb.getCurrentToken().equals("-")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '-'
        }

        else if(tb.getCurrentToken().equals("*")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '*'
        }

        else if(tb.getCurrentToken().equals("/")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '/'
        }

        else if(tb.getCurrentToken().equals("&")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '&'
        }

        else if(tb.getCurrentToken().equals("|")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '|'
        }

        else if(tb.getCurrentToken().equals("<")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '<'
        }

        else if(tb.getCurrentToken().equals(">")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '>'
        }

        else if(tb.getCurrentToken().equals("=")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '='
        }
    }

    private void parseunaryOp() throws IOException {
        // Grammar: '-' | '~'

        if(tb.getCurrentToken().equals("-")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '-'
        }

        else if(tb.getCurrentToken().equals("~")){
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for '~'
        }
    }

    private void parsekeywordConstant() throws IOException {
        // Grammar: 'true' | 'false' | 'null' | 'this'

        if(tb.getCurrentToken().equals("true")){
            // true is '1111111111111111', which is bitwise not of '0000000000000000'
            // push constant 0
            // not
            vm.writeln("push constant 0");
            vm.writeln("not");
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'true'
        }

        else if(tb.getCurrentToken().equals("false")){
            // false is '0000000000000000'
            // push constant 0
            vm.writeln("push constant 0");
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'false'
        }

        else if(tb.getCurrentToken().equals("null")){
            // null is also '0000000000000000'
            // push constant 0
            vm.writeln("push constant 0");
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'null'
        }

        else if(tb.getCurrentToken().equals("this")){
            // 'this' is in pointer 0
            vm.writeln("push pointer 0");
            xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for 'this'
        }
    }

    private void parseintegerConstant() throws IOException {
        // No parsing required
        // push constant <integer>
        vm.writeln("push constant "+Integer.parseInt(tb.getCurrentToken()));
        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for integerConstant

    }

    private void parsestringConstant() throws IOException {
        // no parsing required
        // create a new string object. the address of the newly created object will be on the top of the stack
        // for each character, push the ASCII code of that character and call String.appendChar with 2 arguments (the character now pushed and the reference of the object previously returned)

        String stringConst = tb.getCurrentToken();
        int noOfChars = stringConst.length();
        vm.writeln("push constant "+noOfChars); // argument for String.new
        vm.writeln("call String.new 1");
        for(int i=0;i<noOfChars;i++){
            vm.writeln("push constant "+(int)stringConst.charAt(i));
            vm.writeln("call String.appendChar 2");
        }
        xml.write(tb.getCurrentTokenType(), tb.getCurrentToken());   // for stringConstant

    }

    private void parseclassNameOrVarName() throws IOException {
        this.parseclassName();
    }

    private String getOperatorCommand(String operator) {

        // Returns 'add' for '+', 'sub' for '-', etc., for all binary operators

        if(operator.equals("+")) return "add";
        else if(operator.equals("-")) return "sub";
        else if(operator.equals("*")) return "call Math.multiply 2";
        else if(operator.equals("/")) return "call Math.divide 2";
        else if(operator.equals("=")) return "eq";
        else if(operator.equals(">")) return "gt";
        else if(operator.equals("<")) return "lt";
        else if(operator.equals("&")) return "and";
        else //(operator.equals("|"))
            return "or";


    }

    private String getUnaryOpCommand(String unaryOp) {

        // Returns 'neg' for '-' and 'not' for '~'

        if(unaryOp.equals("-")) return "neg";
        else // unaryOp.equals("~")
            return "not";

    }

}
