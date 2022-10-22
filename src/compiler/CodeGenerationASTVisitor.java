package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.Node;
import svm.ExecuteVM;

import java.util.ArrayList;
import java.util.List;

import static compiler.lib.FOOLlib.*;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

    List<List<String>> dispatchTables = new ArrayList<>();

    CodeGenerationASTVisitor() {
    }

    CodeGenerationASTVisitor(boolean debug) {
        super(false, debug);
    } //enables print for debugging

    @Override
    public String visitNode(ProgLetInNode n) {
        if (print) printNode(n);
        String declCode = null;
        for (Node dec : n.declist) declCode = nlJoin(declCode, visit(dec));
        return nlJoin(
                "push 0",
                declCode, // generate code for declarations (allocation)
                visit(n.exp),
                "halt",
                getCode()
        );
    }

    @Override
    public String visitNode(ProgNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.exp),
                "halt"
        );
    }

    @Override
    public String visitNode(FunNode n) {
        if (print) printNode(n, n.id);
        String declCode = null, popDecl = null, popParl = null;
        for (Node dec : n.declist) {
            declCode = nlJoin(declCode, visit(dec));
            popDecl = nlJoin(popDecl, "pop");
        }
        for (int i = 0; i < n.parlist.size(); i++) popParl = nlJoin(popParl, "pop");
        String funl = freshFunLabel();
        putCode(
                nlJoin(
                        funl + ":",
                        "cfp", // set $fp to $sp value
                        "lra", // load $ra value
                        declCode, // generate code for local declarations (they use the new $fp!!!)
                        visit(n.exp), // generate code for function body expression
                        "stm", // set $tm to popped value (function result)
                        popDecl, // remove local declarations from stack
                        "sra", // set $ra to popped value
                        "pop", // remove Access Link from stack
                        popParl, // remove parameters from stack
                        "sfp", // set $fp to popped value (Control Link)
                        "ltm", // load $tm value (function result)
                        "lra", // load $ra value
                        "js"  // jump to to popped address
                )
        );
        return "push " + funl;
    }

    @Override
    public String visitNode(VarNode n) {
        if (print) printNode(n, n.id);
        return visit(n.exp);
    }

    @Override
    public String visitNode(PrintNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.exp),
                "print"
        );
    }

    @Override
    public String visitNode(IfNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.cond),
                "push 1",
                "beq " + l1,
                visit(n.el),
                "b " + l2,
                l1 + ":",
                visit(n.th),
                l2 + ":"
        );
    }

    @Override
    public String visitNode(EqualNode n) {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "beq " + l1,
                "push 0",
                "b " + l2,
                l1 + ":",
                "push 1",
                l2 + ":"
        );
    }

    @Override
    public String visitNode(TimesNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "mult"
        );
    }

    @Override
    public String visitNode(PlusNode n) {
        if (print) printNode(n);
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "add"
        );
    }

    @Override
    public String visitNode(CallNode n) {
        if (print) printNode(n, n.id);
        String argCode = null, getAR = null;
        for (int i = n.arglist.size() - 1; i >= 0; i--) argCode = nlJoin(argCode, visit(n.arglist.get(i)));
        for (int i = 0; i < n.nl - n.entry.nl; i++) getAR = nlJoin(getAR, "lw");
        var code = nlJoin(
                "lfp", // load Control Link (pointer to frame of function "id" caller)
                argCode, // generate code for argument expressions in reversed order
                "lfp", getAR, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                "stm", // set $tm to popped value (with the aim of duplicating top of stack)
                "ltm", // load Access Link (pointer to frame of function "id" declaration)
                "ltm" // duplicate top of stack
        );

        if (n.entry.type instanceof MethodTypeNode)
            return nlJoin(
                    code,
                    "lw", // load the address of the class's method // new one command for method
                    "push " + n.entry.offset, "add", // compute address of "id" declaration
                    "lw", // load address of "id" function
                    "js"  // jump to popped address (saving address of subsequent instruction in $ra)
            );
        else // no changes for functions
            return nlJoin(
                    code,
                    "push " + n.entry.offset, "add", // compute address of "id" declaration
                    "lw", // load address of "id" function
                    "js"  // jump to popped address (saving address of subsequent instruction in $ra)
            );
    }

    @Override
    public String visitNode(IdNode n) {
        if (print) printNode(n, n.id);
        String getAR = null;
        for (int i = 0; i < n.nl - n.entry.nl; i++) getAR = nlJoin(getAR, "lw");
        return nlJoin(
                "lfp", getAR, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                "push " + n.entry.offset, "add", // compute address of "id" declaration
                "lw" // load value of "id" variable
        );
    }

    @Override
    public String visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return "push " + (n.val ? 1 : 0);
    }

    @Override
    public String visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return "push " + n.val;
    }

    // OPERATOR EXTENSION
    @Override
    public String visitNode(MinusNode n) throws VoidException {
        if (print) printNode(n);
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "sub"
        );
    }

    @Override
    public String visitNode(DivNode n) throws VoidException {
        if (print) printNode(n);
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "div"
        );
    }

    // x >= y
    @Override
    public String visitNode(GreaterEqualNode n) throws VoidException {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.right),
                visit(n.left),
                "bleq " + l1,
                "push 0",
                "b " + l2,
                l1 + ":",
                "push 1",
                l2 + ":"
        ); // non avendo bge, inverto i due operandi e utilizzo la ble
        // x >= y equivale a y <= x
        // confronto l'operando di destra con quello di sinistra
        // se l'operando di destra è minore o uguale allora salto a l1
        // e faccio push 1, altrimenti faccio push 0 e salto a l2
    }

    @Override
    public String visitNode(LessEqualNode n) throws VoidException {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.left),
                visit(n.right),
                "bleq " + l1,
                "push 0",
                "b " + l2,
                l1 + ":",
                "push 1",
                l2 + ":"
        ); // confronto l'operando di sinistra con quello di destra
        // se l'operando di sinistra è minore o uguale allora salto a l1
        // e faccio push 1, altrimenti faccio push 0 e salto a l2
    }

    @Override
    public String visitNode(NotNode n) throws VoidException {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.exp),
                "push 1",
                "beq " + l1,
                "push 1",
                "b " + l2,
                l1 + ":",
                "push 0",
                l2 + ":"
        ); // se l'exp è 0 push 1, altrimenti push 0
        // inverto il valore in quanto il not è 1 se l'exp è 0 e 0 se l'exp è 1
    }

    @Override
    public String visitNode(OrNode n) throws VoidException {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.left),
                "push 1",
                "beq " + l1,
                visit(n.right),
                "push 1",
                "beq " + l1,
                "push 0",
                "b " + l2,
                l1 + ":",
                "push 1",
                l2 + ":"
        ); // se l'operando di sinistra è 1 vado a l1 e pusho 1, infatti nell'or basta un true per essere true l'exp
        // altrimenti proseguo analizzando l'operando di destra
        // se l'operando di destra è 1 vado a l1 e pusho 1, altrimenti vuol dire che entrambi left e right
        // sono false quindi pusho 0 e vado a l2
    }

    @Override
    public String visitNode(AndNode n) throws VoidException {
        if (print) printNode(n);
        String l1 = freshLabel();
        String l2 = freshLabel();
        return nlJoin(
                visit(n.left),
                "push 0",
                "beq " + l1,
                visit(n.right),
                "push 0",
                "beq " + l1,
                "push 1",
                "b " + l2,
                l1 + ":",
                "push 0",
                l2 + ":"
        ); // se l'operando di sinistra è 0 vado a l1 e pusho 0, infatti nell'and basta un false per essere false l'exp
        // altrimenti proseguo analizzando l'operando di destra, se l'operando di destra è 0 vado a l1 e pusho 0
        // altrimenti vuol dire che entrambi left e right sono true quindi pusho 1 e vado a l2
    }

    @Override
    public String visitNode(ClassNode n) throws VoidException {
        if (print) printNode(n);

        var dispatchTable = new ArrayList<String>(); // No superclass -> empty dispatch table

        // inherits -> copy superclass' dispatch table, offset ref to slide 40
        if (n.superId != null)
            dispatchTable.addAll(dispatchTables.get(-n.superEntry.offset - 2)); // copy value and not its reference!!

        // visit methods
        for (var m : n.methodList) {
            visit(m);
            var label = m.label;
            var offset = m.offset;
            // update dispatch table
            if (offset < dispatchTable.size()) // offset already passed -> override
                dispatchTable.set(offset, label); // overriding
            else
                dispatchTable.add(offset, label); // not overriding
        }

        dispatchTables.add(dispatchTable); // add dispatch table to the others
        String dispatchTableCode = null;
        for (var label : dispatchTable) {
            dispatchTableCode = nlJoin(
                    dispatchTableCode,
                    "push " + label, // push on stack the method label (the address)
                    "lhp", // loah $hp on stack
                    "sw", // pop the two values and store the method label at the address pointed by $hp
                    incrementHeapPointer() // increment $hp
            );
        }

        return nlJoin(
                "lhp", // load $hp on stack, the address of the dispatch pointer to return
                dispatchTableCode // load the dispatch table on the heap
        );
    }

    @Override
    public String visitNode(MethodNode n) throws VoidException {
        if (print) printNode(n);
        String declCode = null, popDecl = null, popParl = null;

        for (var dec : n.declist) {
            // create declarations code
            declCode = nlJoin(declCode, visit(dec));
            // for every declaration, add a pop to pop the value from the stack
            popDecl = nlJoin(popDecl, "pop");
        }
        for (var p : n.parlist) popParl = nlJoin(popParl, "pop");
        n.label = freshFunLabel();  // generate label and set label to method node

        // same as functions
        putCode(
                nlJoin(
                        n.label + ":",
                        "cfp", // set $fp to $sp value
                        "lra", // load $ra value
                        declCode, // generate code for local declarations (they use the new $fp!!!)
                        visit(n.exp), // generate code for function body expression
                        "stm", // set $tm to popped value (function result)
                        popDecl, // remove local declarations from stack
                        "sra", // set $ra to popped value
                        "pop", // remove Access Link from stack
                        popParl, // remove parameters from stack
                        "sfp", // set $fp to popped value (Control Link)
                        "ltm", // load $tm value (function result)
                        "lra", // load $ra value
                        "js"  // jump to to popped address
                )
        );

        return null; // Empty code. Ref to slide 38
    }

    @Override
    public String visitNode(ClassCallNode n) throws VoidException {
        if (print) printNode(n, n.objectId + "." + n.methodId);

        // same as CallNode
        String argCode = null, getAR = null;
        for (int i = n.arglist.size() - 1; i >= 0; i--)
            argCode = nlJoin(argCode, visit(n.arglist.get(i)));
        for (int i = 0; i < n.nl - n.entry.nl; i++)
            getAR = nlJoin(getAR, "lw");
        return nlJoin(
                "lfp", // load Control Link (pointer to frame of function "id" caller)
                argCode, // generate code for argument expressions in reversed order
                "lfp", getAR, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                // above is the same as call node, below is different
                // ID1
                "push " + n.entry.offset, "add", // push offset of id1 declaration on stack and compute its address
                "lw", // load address of id1 declaration
                "stm", // set $tm to popped value (with the aim of duplicating top of stack)
                "ltm", // load Access Link (pointer to frame of function "id" declaration)
                "ltm", // duplicate top of stack
                // ID2
                "lw", // load the address of the class's method // new one command for method
                "push " + n.methodEntry.offset, "add", // push offset of id2 declaration on stack and compute its address
                "lw", // load address of "id" function
                "js"  // jump to popped address (saving address of subsequent instruction in $ra)
        );
    }

    @Override
    public String visitNode(NewNode n) throws VoidException {
        if (print) printNode(n);

        // recall over all arguments to put them (for each arg) on the stack. Ref to slide 45
        String args = null;
        for (var arg : n.arglist) args = nlJoin(args, visit(arg));

        // move each value from the stack to the heap. Ref to slide 45
        for (var arg : n.arglist)
            args = nlJoin(
                    args,
                    "lhp", // load $hp value
                    "sw", // pop two values: the second one is written at the memory address pointed by hp
                    incrementHeapPointer() // increment $hp by 1
            );

        var address = ExecuteVM.MEMSIZE + n.entry.offset; // get the address of the class in the heap

        return nlJoin(
                args,
                "push " + address, // push the address of the class in the heap to the stack
                "lw", // put on the stack the value of $address (the class) from memory
                "lhp", // load $hp value (the dispatch pointer address)
                "sw", // store in $hp the dispatch pointer
                "lhp", // load on stack the $hp value (the dispatch pointer address)
                incrementHeapPointer() // increment $hp by 1
        );
    }

    @Override
    public String visitNode(EmptyNode n) throws VoidException {
        if (print) printNode(n);
        return "push -1"; // put on stack -1, none object pointer has this value. Ref to slide 42
    }

    // Refactored method to increment heap pointer by one
    private String incrementHeapPointer() {
        return nlJoin(
                "lhp", "push 1", // load $hp value and push a 1 on the stack
                "add", // sum $hp and 1
                "shp" // set $hp to popped value (the result of the sum)
        );
    }


}