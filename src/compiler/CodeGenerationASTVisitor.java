package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.Node;

import static compiler.lib.FOOLlib.*;

public class CodeGenerationASTVisitor extends BaseASTVisitor<String, VoidException> {

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
        return nlJoin(
                "lfp", // load Control Link (pointer to frame of function "id" caller)
                argCode, // generate code for argument expressions in reversed order
                "lfp", getAR, // retrieve address of frame containing "id" declaration
                // by following the static chain (of Access Links)
                "stm", // set $tm to popped value (with the aim of duplicating top of stack)
                "ltm", // load Access Link (pointer to frame of function "id" declaration)
                "ltm", // duplicate top of stack
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
        ); // non avendo bge, inverto i due operandi con la ble
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
				"push 0",
				"b " + l2,
				l1 + ":",
				"push 1",
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
}