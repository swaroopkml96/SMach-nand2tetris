public class Grammar {

    public String getTokenType(String token){
        if (token==null) return null;
        if(this.isCharSymbol(token.toCharArray()[0])){
            // Type Symbol
            return "symbol";
        }
        else if(this.isKeyword(token)){
            // Type keyword
            return "keyword";
        }
        else if(Character.isDigit(token.toCharArray()[0])){
            // Type number
            return "integerConstant";
        }
        else{
            // Type identifier
            return "identifier";
        }
    }

    public boolean isCharSymbol(char ch){
        if(ch=='{'
                || ch=='}'
                || ch=='['
                || ch==']'
                || ch=='('
                || ch==')'
                || ch=='.'
                || ch==','
                || ch==';'
                || ch=='+'
                || ch=='-'
                || ch=='*'
                || ch=='/'
                || ch=='&'
                || ch=='|'
                || ch=='~'
                || ch=='<'
                || ch=='>'
                || ch=='=') {
            return true;
        }
            else return false;
    }

    public boolean isKeyword(String token){
        if(token.equals("class")
                || token.equals("constructor")
                || token.equals("function")
                || token.equals("method")
                || token.equals("field")
                || token.equals("static")
                || token.equals("var")
                || token.equals("int")
                || token.equals("char")
                || token.equals("boolean")
                || token.equals("void")
                || token.equals("true")
                || token.equals("false")
                || token.equals("null")
                || token.equals("this")
                || token.equals("let")
                || token.equals("do")
                || token.equals("if")
                || token.equals("else")
                || token.equals("while")
                || token.equals("return")) return true;
            else return false;
    }

    public boolean isclassVarDec(String token){
        if(token.equals("static") || token.equals("field")) return true;
        else return false;
    }

    public boolean issubroutineDec(String token){
        if(token.equals("constructor") || token.equals("function") || token.equals("method")) return true;
        else return false;
    }

    public boolean istypeOrIdentifier(String token){
        if(token.equals("int") || token.equals("char") || token.equals("boolean") || this.getTokenType(token).equals("identifier")) return true;
        else return false;
    }

    public boolean isvarDec(String token){
        if(token.equals("var")) return true;
        else return false;
    }

    public boolean isstatement(String token){
        if(token.equals("let") || token.equals("if") || token.equals("while") || token.equals("do") || token.equals("return")) return true;
        else return false;
    }

    public boolean isoperator(String token){
        if(token.equals("+")
                || token.equals("-")
                || token.equals("*")
                || token.equals("/")
                || token.equals("&")
                || token.equals("|")
                || token.equals("<")
                || token.equals(">")
                || token.equals("=")) return true;
        else return false;
    }

    public boolean isunaryOp(String token){
        if(token.equals("-") || token.equals("~")) return true;
        else return false;
    }

    public boolean iskeywordConstant(String token){
        if(token.equals("true")
                || token.equals("false")
                || token.equals("null")
                || token.equals("this")) return true;
        else return false;
    }


}
