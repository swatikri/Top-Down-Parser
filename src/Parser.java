
import java.util.*;

/* 		OBJECT-ORIENTED RECOGNIZER FOR SIMPLE EXPRESSIONS
 expr    -> term   (+ | -) expr | term
 term    -> factor (* | /) term | factor
 factor  -> int_lit | '(' expr ')'
 */
//Stack st = new Stack();
public class Parser {
	public static String[] local=new String[4];
    public static void main(String[] args) {
        System.out.println("Enter code!\n");
        Lexer.lex();
        new Program();
        Code.output();
        
    }
}

class Program { //program->decl stmts
    Decl d;
    Stmts s;
    public Program() {
        d = new Decl();
        s=new Stmts();
        Code.gen(Code.endcode());
    }
}

class Decl { //decls->int idlist ';'
    Idlist idl;
    
    public Decl() {
        if(Lexer.nextToken == Token.KEY_INT) {
            Lexer.lex();
            idl = new Idlist();
        }
        if (Lexer.nextToken==Token.SEMICOLON)
        {
            Lexer.lex();
        }
        
    }
}

class Idlist { //idlist->id [',' idlist]
    char ido;
    Idlist idl;
    public Idlist() {
        if(Lexer.nextToken == Token.ID) {
            ido = Lexer.ident;
            Code.varstack(ido); 
            Lexer.lex();
        }
        if(Lexer.nextToken == Token.COMMA) {
            Lexer.lex();
            idl = new Idlist();
        }
        
    }
}

class Stmts {  // stmt->stmt[stmts]
    Stmt s;
    Stmts sts;
    public Stmts() {
        s= new Stmt();
        
        if(Lexer.nextToken == Token.ID || Lexer.nextToken == Token.LEFT_BRACE || Lexer.nextToken == Token.KEY_IF || Lexer.nextToken == Token.KEY_FOR){
            
            sts= new Stmts();
        }
        
    }
    
}

class Stmt { //stmt->assign ';'|cmpd |  cond
    Assign ass;
    Cmpd cmpd;
    Cond cond;
    Loop loop;
    public Stmt() {
        switch(Lexer.nextToken) {
            case Token.ID:
                ass = new Assign();
                if (Lexer.nextToken==Token.SEMICOLON)
                {
                    Lexer.lex();
                }
                break;
            case Token.LEFT_BRACE:
                cmpd = new Cmpd();
                
                break;
            case Token.KEY_IF:
                cond = new Cond();
                break;
                
            case Token.KEY_FOR:
                
                loop = new Loop();
                break;
                
        }
    }
}

class Assign {  //assign -> id '=' expr
    char idt;
    Expr e;
    public Assign() {
        if(Lexer.nextToken == Token.ID) {
            idt= Lexer.ident;
            Lexer.lex();
        }
        if(Lexer.nextToken == Token.ASSIGN_OP) {
            Lexer.lex();
            e = new Expr();
            Code.gen(Code.idcode(idt));
        }
        
    }
}

class Cmpd {
    Stmts sts;
    public Cmpd() {
        Lexer.lex();
        sts = new Stmts();
        Lexer.lex(); // skip over '}'
    }
}

class Cond {   //if '(' rexp ')' stmt [ else stmt ]
    Rexp rexp;
    Stmt s1,s2;
    static public int elv;
    public int rem;
    public int ind;
    public int befelse;
    public int l;
    public int label;
    public int v;
    public int reti;
    public Stack<Integer> st = new Stack<Integer>();
    public Cond() {
        Lexer.lex();
        if(Lexer.nextToken == Token.LEFT_PAREN) {
            Lexer.lex();
            rexp = new Rexp();
            v=Code.ptr-1;  //stack
            st.push(Code.a[Code.ptr-1]); //stack
            Lexer.lex();
            s1=new Stmt();
            l=Code.ptr;
            l=l-1;
            befelse=Code.a[Code.ptr];
            if(Lexer.nextToken == Token.KEY_ELSE) {
                Integer mygoto=Code.ptr;
                Code.code[Code.codeptr]=Code.a[Code.ptr]+ ": goto";
                Code.codeptr=Code.codeptr+1;
                Code.ptr=Code.ptr+1;
                Code.a[Code.ptr]=Code.a[Code.ptr-1]+3;
                Lexer.lex();
                elv= Code.a[Code.ptr];
                rem=(int) st.pop();
                ind=Code.fif(rem);
                Code.iflabel(ind);
                
                s2=new Stmt();
                label=Code.a[Code.ptr];
                Code.code[mygoto]=Code.code[mygoto]+" "+ label ;
                //Code.codeptr++;
            }
            else
            {
                reti=Code.a[Code.ptr];
                Code.code[v]=Code.code[v]+reti;
            }
        }
        
    }
    
}

class Loop { //loop    -	>  for '(' 	[assign]	';' [rexp] ';' 	[assign]  ')' stmt
    Assign ass1, ass2;
    Rexp rexp;
    Stmt stmt;
    String parseme,ret;
    int start, end;
    int ifs,gotlab,startif;
    public Loop(){
        Lexer.lex();
        if(Lexer.nextToken == Token.LEFT_PAREN) {
            Lexer.lex();
            if(Lexer.nextToken == Token.ID)
            {ass1=new Assign();}
            
            Lexer.lex();
            ifs=Code.a[Code.ptr];
            rexp = new Rexp();
            Lexer.lex();
            Integer enddiff = null;
            Integer increment=0;
            start=Code.ptr;
            startif=start-1;
            if (Lexer.nextChar!=')')
            {
            	increment=1;
	            if(Lexer.nextToken == Token.ID)
	            {ass2=new Assign();}
	            end=Code.ptr-1;
	            Parser.local[0]=Code.code[start];
	            Parser.local[1]=Code.code[start+1];
	            Parser.local[2]=Code.code[start+2];
	            Parser.local[3]=Code.code[start+3];
	            
	            enddiff=Code.a[Code.ptr]-Code.a[Code.ptr-1];
	            Code.ptr=Code.ptr-4;
	            Code.codeptr=Code.codeptr-4;
	            for (int i=start; i<end+1; i++)
	            {
	                Code.code[i]="";
	            }
            }
            Lexer.lex();
            stmt=new Stmt();
            
            if (increment==1)
            {
	            Integer num1,num2,num3,num4;
	            String str1, str2, str3, str4;
	            
	            String[] parts=Parser.local[0].split(":");
	            num1=Integer.parseInt(parts[0]);
	            String[] str=parts[1].split(" ");
	            str1=str[1];
	            parts=Parser.local[1].split(":");
	            num2=Integer.parseInt(parts[0]);
	            str=parts[1].split(" ");
	            str2=str[1];
	            
	            parts=Parser.local[2].split(":");
	            num3=Integer.parseInt(parts[0]);
	            str=parts[1].split(" ");
	            str3=str[1];
	            
	            parts=Parser.local[3].split(":");
	            num4=Integer.parseInt(parts[0]);
	            str=parts[1].split(" ");
	            str4=str[1];
	            
	            Code.code[Code.codeptr]=Code.a[Code.ptr]+ ": " + str1;
	            Code.codeptr=Code.codeptr+1;
	            Code.ptr=Code.ptr+1;
	            
	            Integer diff=num2-num1;
	            Code.a[Code.ptr]=Code.a[Code.ptr-1]+diff;
	            Code.code[Code.codeptr]=Code.a[Code.ptr]+ ": " + str2;
	            Code.codeptr=Code.codeptr+1;
	            Code.ptr=Code.ptr+1;
	            
	            diff=num3-num2;
	            Code.a[Code.ptr]=Code.a[Code.ptr-1]+diff;
	            Code.code[Code.codeptr]=Code.a[Code.ptr]+ ": " + str3;
	            Code.codeptr=Code.codeptr+1;
	            Code.ptr=Code.ptr+1;
	            
	            diff=num4-num3;
	            Code.a[Code.ptr]=Code.a[Code.ptr-1]+diff;
	            Code.code[Code.codeptr]=Code.a[Code.ptr]+ ": " + str4;
	            Code.codeptr=Code.codeptr+1;
	            Code.ptr=Code.ptr+1;
	            
	            Code.a[Code.ptr]=Code.a[Code.ptr-1]+enddiff;
            }
          
	        gotlab=Code.a[Code.ptr];
            Code.code[Code.ptr]=gotlab+": goto "+ifs;
            Code.a[Code.ptr+1]=Code.a[Code.ptr] +3;
            Code.codeptr++;
            Code.ptr++;
            Code.code[startif]=Code.code[startif] + Code.a[Code.ptr];
        }
    }
}

class Rexp { //rexp -> expr('<'|'>'|'=='|'!=')expr
    Expr e1,e2;
    char relop;
    public Rexp() {
        e1= new Expr();
        if (Lexer.nextToken == Token.LESSER_OP || Lexer.nextToken == Token.GREATER_OP || Lexer.nextToken == Token.EQ_OP || Lexer.nextToken == Token.NOT_EQ) {
            relop = Lexer.nextChar;
            Lexer.lex();
            e2 = new Expr();
            Code.gen(Code.relopcode(relop));//after bracket here
            
        }
    }
}

class Expr   { // expr -> term (+ | -) expr | term
    Term t;
    Expr e;
    char op;
    
    public Expr() {
        t = new Term();
        if (Lexer.nextToken == Token.ADD_OP || Lexer.nextToken == Token.SUB_OP) {
            op = Lexer.nextChar;
            Lexer.lex();
            e = new Expr();
            Code.gen(Code.opcode(op));
        }
    }
}
class Term    { // term -> factor (* | /) term | factor
    Factor f;
    Term t;
    char op;
    
    public Term() {
        f = new Factor();
        if (Lexer.nextToken == Token.MULT_OP || Lexer.nextToken == Token.DIV_OP) {
            op = Lexer.nextChar;
            Lexer.lex();
            t = new Term();
            Code.gen(Code.opcode(op));
        }
    }
}

class Factor { // factor -> id | number | '(' expr ')'
    Expr e;
    int i;
    char im;
    public Factor() {
        switch (Lexer.nextToken) {
            case Token.INT_LIT: // number
                i = Lexer.intValue;
                Code.gen(Code.intcode(i));
                Lexer.lex();
                break;
            case Token.LEFT_PAREN: // '('
                Lexer.lex();
                e = new Expr();
                Lexer.lex(); // skip over ')'
                break;
            case Token.ID:
                im = Lexer.ident;
                Code.gen(Code.varcode(im));
                Lexer.lex();
                break;
            default:
                break;
        }
    }
}


class Code {
    static String[] code = new String[100];
    static int codeptr = 0;
    static int stackptr = 0;
    static char[] stack=new char[10];
    public static int[] a=new int[100];
    public static int ptr = 0;
    
    public static void gen(String s) {
        code[codeptr] = s;
        codeptr++;
    }
    
    public static void iflabel(int i) {
        code[i]=code[i]+Cond.elv;
    }
    
    public static void gotcode(int p, int q){
        for(int i=0;i<ptr;i++) {
            if(a[i]==p){
                code[i]=p+":"+ "goto"+q;
                //System.out.println(code[i]);
                
            }
        }
    }
    
    
    
    public static String intcode(int i) {
        if (i > 127) { a[ptr+1]=a[ptr]+3;ptr=ptr+1;return a[ptr-1] +": sipush " + i; }//ptr=ptr+3
        if (i > 5) {a[ptr+1]=a[ptr]+2;ptr=ptr+1; return a[ptr-1] +": bipush " + i;}
        a[ptr+1]=a[ptr]+1;
        ptr=ptr+1;
        return a[ptr-1] +": iconst_" + i;//ptr=ptr+1
    }
    public static String varcode(char im) {
        int find=0;
	       for(int i=0;i<=stackptr;i++){
               if(stack[i]==im) {
                   if(i<3) {
                       find=i+1;
                       a[ptr+1]=a[ptr]+1;
                       ptr=ptr+1;
                       return a[ptr-1] +": iload_" + find;}//ptr=ptr+1
                   if(i>=3) {
                       find=i+1;
                       a[ptr+1]=a[ptr]+2;
                       ptr=ptr+1;
                       return a[ptr-1] +": iload " + find;}//ptr=ptr+2
               }
           }
	       return "not found";
    }
    
    public static String opcode(char op) {
        switch(op) {
            case '+': { a[ptr+1]=a[ptr]+1;ptr=ptr+1; return a[ptr-1] +": iadd";}
            case '-': { a[ptr+1]=a[ptr]+1;ptr=ptr+1; return a[ptr-1] +": isub";}
            case '*': { a[ptr+1]=a[ptr]+1;ptr=ptr+1; return a[ptr-1] +": imul";}
            case '/': { a[ptr+1]=a[ptr]+1;ptr=ptr+1; return a[ptr-1] +": idiv";}
            default: return "";
        }
    }
    
    
    
    public static String relopcode(char op) {
        switch(op) {
            case '<': { a[ptr+1]=a[ptr]+3;ptr=ptr+1; return a[ptr-1] +": if_icmpge ";}
            case '>': { a[ptr+1]=a[ptr]+3;ptr=ptr+1; return a[ptr-1] +": if_icmple ";}
            case '!': { a[ptr+1]=a[ptr]+3;ptr=ptr+1; return a[ptr-1] +": if_icmpeq ";}
            case '=': { a[ptr+1]=a[ptr]+3;ptr=ptr+1; return a[ptr-1] +": if_icmpne ";}
            default: return "";
        }
    }
    
    public static void varstack(char id) {
        stack[stackptr] = id;
        stackptr++;
        
    }
    
    public static int fif(int rem) {
        for(int i=0;i<ptr;i++)
        {if(a[i]==rem)
            return i;}
        //else return 0;
        return 0;
        
    }
    
    public static String idcode(char id) {
        int find=0;
        
        for(int i=0;i<=stackptr;i++){
	           if(stack[i]==id) {
                   if(i<3) {
                       find=i+1;
                       a[ptr+1]=a[ptr]+1;
                       ptr=ptr+1;
                       return a[ptr-1]+": istore_" + find;} //ptr=ptr+1
                   if(i>=3) {
                       find=i+1;
                       a[ptr+1]=a[ptr]+2;
                       ptr=ptr+1;
                       return a[ptr-1]+": istore " + find;}//ptr=ptr+2
               }
        }
	       return "not found";
        
    }
    
    
    
    
    
    public static void output() {
        
        for (int i=0; i<codeptr; i++)
            
            System.out.println(code[i]);
    }
    public static String endcode()
    {
        return a[ptr]+": return";
    }
}
