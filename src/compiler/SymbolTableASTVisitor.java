package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.Node;
import compiler.lib.TypeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolTableASTVisitor extends BaseASTVisitor<Void, VoidException> {

    private final List<Map<String, STentry>> symTable = new ArrayList<>();
    // Class Table map every class name to its virtual table
    private final Map<String, Map<String, STentry>> classTable = new HashMap<>();

    private int nestingLevel = 0; // current nesting level
    private int decOffset = -2; // counter for offset of local declarations at current nesting level
    int stErrors = 0;

    SymbolTableASTVisitor() {
    }

    SymbolTableASTVisitor(boolean debug) {
        super(debug);
    } // enables print for debugging

    private STentry stLookup(String id) {
        int j = nestingLevel;
        STentry entry = null;
        while (j >= 0 && entry == null)
            entry = symTable.get(j--).get(id);
        return entry;
    }

    @Override
    public Void visitNode(ProgLetInNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = new HashMap<>();
        symTable.add(hm);
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        symTable.remove(0);
        return null;
    }

    @Override
    public Void visitNode(ProgNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(FunNode n) {
        if (print) printNode(n);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        List<TypeNode> parTypes = new ArrayList<>();
        for (ParNode par : n.parlist) parTypes.add(par.getType());
        STentry entry = new STentry(nestingLevel, new ArrowTypeNode(parTypes, n.retType), decOffset--);
        //inserimento di ID nella symtable
        if (hm.put(n.id, entry) != null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        //creare una nuova hashmap per la symTable
        nestingLevel++;
        Map<String, STentry> hmn = new HashMap<>();
        symTable.add(hmn);
        int prevNLDecOffset = decOffset; // stores counter for offset of declarations at previous nesting level
        decOffset = -2;

        int parOffset = 1;
        for (ParNode par : n.parlist)
            if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                System.out.println("Par id " + par.id + " at line " + n.getLine() + " already declared");
                stErrors++;
            }
        for (Node dec : n.declist) visit(dec);
        visit(n.exp);
        //rimuovere la hashmap corrente poiche' esco dallo scope
        symTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset; // restores counter for offset of declarations at previous nesting level
        return null;
    }

    @Override
    public Void visitNode(VarNode n) {
        if (print) printNode(n);
        visit(n.exp);
        Map<String, STentry> hm = symTable.get(nestingLevel);
        STentry entry = new STentry(nestingLevel, n.getType(), decOffset--);
        //inserimento di ID nella symtable
        if (hm.put(n.id, entry) != null) {
            System.out.println("Var id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }
        return null;
    }

    @Override
    public Void visitNode(PrintNode n) {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }

    @Override
    public Void visitNode(IfNode n) {
        if (print) printNode(n);
        visit(n.cond);
        visit(n.th);
        visit(n.el);
        return null;
    }

    @Override
    public Void visitNode(EqualNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(TimesNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(PlusNode n) {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(CallNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println("Fun id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        for (Node arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(IdNode n) {
        if (print) printNode(n);
        STentry entry = stLookup(n.id);
        if (entry == null) {
            System.out.println("Var or Par id " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            n.entry = entry;
            n.nl = nestingLevel;
        }
        return null;
    }

    @Override
    public Void visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    @Override
    public Void visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return null;
    }

    // OPERATOR EXTENSION

    @Override
    public Void visitNode(MinusNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(DivNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(GreaterEqualNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(LessEqualNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(NotNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.exp);
        return null;
    }


    @Override
    public Void visitNode(OrNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    @Override
    public Void visitNode(AndNode n) throws VoidException {
        if (print) printNode(n);
        visit(n.left);
        visit(n.right);
        return null;
    }

    // OOP EXTENSION

    @Override
    public Void visitNode(ClassNode n) throws VoidException {
        if (print) printNode(n);

        // TODO...


        return super.visitNode(n);
    }

    @Override
    public Void visitNode(MethodNode n) throws VoidException {
        // Similar to visitNode(FunNode n)
        if (print) printNode(n);
        var hm = symTable.get(nestingLevel);
        // Get parameters' types
        var parTypes = new ArrayList<TypeNode>();
        for (var par : n.parList) parTypes.add(par.getType());
        // Set method type
        var methodType = new MethodTypeNode(new ArrowTypeNode(parTypes, n.getType()));
        n.setType(methodType);

        // Enter the method scope
        nestingLevel++;
        // Create new hashmap for the method scope symbol table
        var hmn = new HashMap<String, STentry>();
        symTable.add(hmn);
        var prevNLDecOffset = decOffset; // store counter for offset of declarations at current nesting level
        var parOffset = 1;
        // Check if parameters are already declared
        for (var par : n.parList) {
            if (hmn.put(par.id, new STentry(nestingLevel, par.getType(), parOffset++)) != null) {
                System.out.println("Par id " + par.id + " at line " + par.getLine() + " already declared");
                stErrors++;
            }
        }
        for (var dec : n.decList) visit(dec);
        visit(n.exp);
        // Exit the method scope and remove the symbol table
        symTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset; // restores counter for offset of declarations at previous nesting level
                return null;
    }

    @Override
    public Void visitNode(ClassCallNode n) throws VoidException {
        if (print) printNode(n);
        // Sintax: ID.ID()
        var entry = stLookup(n.objectId); // object must be in the symbol table
        if (entry == null) {
            System.out.println("Object id " + n.objectId + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            if (entry.type instanceof RefTypeNode) {
                var objectClassId = ((RefTypeNode) entry.type).id;
                var methodEntry = classTable.get(objectClassId).get(n.methodId); // method must be in the class table
                if (methodEntry == null) {
                    System.out.println("Method id " + n.methodId + " at line " + n.getLine() + " not declared");
                    stErrors++;
                } else {
                    n.entry = entry;
                    n.methodEntry = methodEntry;
                    n.nl = nestingLevel;
                }
            } else {
                System.out.println("Object id " + n.objectId + " at line " + n.getLine() + " is not a class");
                stErrors++;
            }
        }
        return null;
    }

    @Override
    public Void visitNode(NewNode n) throws VoidException {
        if (print) printNode(n);
        // Sintax: new ID()
        // Control if the class to be instantiated has been declared
        if (!classTable.containsKey(n.id)) {
            System.out.println("Class " + n.id + " at line " + n.getLine() + " not declared");
            stErrors++;
        } else {
            var classEntry = symTable.get(0).get(n.id);
            if (classEntry == null) {
                System.out.println("Class " + n.id + " at line " + n.getLine() + " not declared at level 0");
                stErrors++;
            } else n.entry = classEntry;
        }
        for (var arg : n.argList) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(EmptyNode n) throws VoidException {
        // Syntax: null
        if (print) printNode(n);
        return null;
    }

}
