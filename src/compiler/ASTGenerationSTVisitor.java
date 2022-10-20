package compiler;

import compiler.AST.*;
import compiler.FOOLParser.*;
import compiler.lib.DecNode;
import compiler.lib.Node;
import compiler.lib.TypeNode;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

import static compiler.lib.FOOLlib.extractCtxName;
import static compiler.lib.FOOLlib.lowerizeFirstChar;

public class ASTGenerationSTVisitor extends FOOLBaseVisitor<Node> {

    String indent;
    public boolean print;

    ASTGenerationSTVisitor() {
    }

    ASTGenerationSTVisitor(boolean debug) {
        print = debug;
    }

    private void printVarAndProdName(ParserRuleContext ctx) {
        String prefix = "";
        Class<?> ctxClass = ctx.getClass(), parentClass = ctxClass.getSuperclass();
        if (!parentClass.equals(ParserRuleContext.class)) // parentClass is the var context (and not ctxClass itself)
            prefix = lowerizeFirstChar(extractCtxName(parentClass.getName())) + ": production #";
        System.out.println(indent + prefix + lowerizeFirstChar(extractCtxName(ctxClass.getName())));
    }

    @Override
    public Node visit(ParseTree t) {
        if (t == null) return null;
        String temp = indent;
        indent = (indent == null) ? "" : indent + "  ";
        Node result = super.visit(t);
        indent = temp;
        return result;
    }

    @Override
    public Node visitProg(ProgContext c) {
        if (print) printVarAndProdName(c);
        return visit(c.progbody());
    }

    @Override
    public Node visitLetInProg(LetInProgContext c) {
        if (print) printVarAndProdName(c);
        List<DecNode> declist = new ArrayList<>();
        for (var dec: c.cldec()) declist.add((DecNode) visit(dec));
        for (DecContext dec : c.dec()) declist.add((DecNode) visit(dec));
        return new ProgLetInNode(declist, visit(c.exp()));
    }

    @Override
    public Node visitNoDecProg(NoDecProgContext c) {
        if (print) printVarAndProdName(c);
        return new ProgNode(visit(c.exp()));
    }

    @Override
    public Node visitVardec(VardecContext c) {
        if (print) printVarAndProdName(c);
        Node n = null;
        if (c.ID() != null) { //non-incomplete ST
            n = new VarNode(c.ID().getText(), (TypeNode) visit(c.type()), visit(c.exp()));
            n.setLine(c.VAR().getSymbol().getLine());
        }
        return n;
    }

    @Override
    public Node visitFundec(FundecContext c) {
        if (print) printVarAndProdName(c);
        List<ParNode> parList = new ArrayList<>();
        for (int i = 1; i < c.ID().size(); i++) {
            ParNode p = new ParNode(c.ID(i).getText(), (TypeNode) visit(c.type(i)));
            p.setLine(c.ID(i).getSymbol().getLine());
            parList.add(p);
        }
        List<DecNode> decList = new ArrayList<>();
        for (DecContext dec : c.dec()) decList.add((DecNode) visit(dec));
        Node n = null;
        if (c.ID().size() > 0) { //non-incomplete ST
            n = new FunNode(c.ID(0).getText(), (TypeNode) visit(c.type(0)), parList, decList, visit(c.exp()));
            n.setLine(c.FUN().getSymbol().getLine());
        }
        return n;
    }

    @Override
    public Node visitIntType(IntTypeContext c) {
        if (print) printVarAndProdName(c);
        return new IntTypeNode();
    }

    @Override
    public Node visitBoolType(BoolTypeContext c) {
        if (print) printVarAndProdName(c);
        return new BoolTypeNode();
    }

    @Override
    public Node visitInteger(IntegerContext c) {
        if (print) printVarAndProdName(c);
        int v = Integer.parseInt(c.NUM().getText());
        return new IntNode(c.MINUS() == null ? v : -v);
    }

    @Override
    public Node visitTrue(TrueContext c) {
        if (print) printVarAndProdName(c);
        return new BoolNode(true);
    }

    @Override
    public Node visitFalse(FalseContext c) {
        if (print) printVarAndProdName(c);
        return new BoolNode(false);
    }

    @Override
    public Node visitIf(IfContext c) {
        if (print) printVarAndProdName(c);
        Node ifNode = visit(c.exp(0));
        Node thenNode = visit(c.exp(1));
        Node elseNode = visit(c.exp(2));
        Node n = new IfNode(ifNode, thenNode, elseNode);
        n.setLine(c.IF().getSymbol().getLine());
        return n;
    }

    @Override
    public Node visitPrint(PrintContext c) {
        if (print) printVarAndProdName(c);
        return new PrintNode(visit(c.exp()));
    }

    @Override
    public Node visitPars(ParsContext c) {
        if (print) printVarAndProdName(c);
        return visit(c.exp());
    }

    @Override
    public Node visitId(IdContext c) {
        if (print) printVarAndProdName(c);
        Node n = new IdNode(c.ID().getText());
        n.setLine(c.ID().getSymbol().getLine());
        return n;
    }

    @Override
    public Node visitCall(CallContext c) {
        if (print) printVarAndProdName(c);
        List<Node> arglist = new ArrayList<>();
        for (ExpContext arg : c.exp()) arglist.add(visit(arg));
        Node n = new CallNode(c.ID().getText(), arglist);
        n.setLine(c.ID().getSymbol().getLine());
        return n;
    }

    // TIMES and DIV
    @Override
    public Node visitTimesDiv(TimesDivContext c) {
        if (print) printVarAndProdName(c);
        Node n = null;
        if (c.TIMES() != null) {
            n = new TimesNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.TIMES().getSymbol().getLine());        // setLine added
        } else if (c.DIV() != null) {
            n = new DivNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.DIV().getSymbol().getLine());        // setLine added
        }
        return n;
    }

    // PLUS and MINUS
    @Override
    public Node visitPlusMinus(PlusMinusContext c) {
        if (print) printVarAndProdName(c);
        Node n = null;
        if (c.PLUS() != null) {
            n = new PlusNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.PLUS().getSymbol().getLine()); // setLine added
        } else if (c.MINUS() != null) {
            n = new MinusNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.MINUS().getSymbol().getLine()); // setLine added
        }
        return n;
    }

    /*
     * <=, >=, ==, ||, &&, !
     */

    // EQUALS, GREATER EQUALS, LESS EQUALS
    @Override
    public Node visitComp(CompContext c) {
        if (print) printVarAndProdName(c);
        Node n = null;
        if (c.EQ() != null) {
            n = new EqualNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.EQ().getSymbol().getLine());
        } else if (c.GE() != null) {
            n = new GreaterEqualNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.GE().getSymbol().getLine());
        } else if (c.LE() != null) {
            n = new LessEqualNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.LE().getSymbol().getLine());
        }
        return n;
    }

    // AND and OR
    @Override
    public Node visitAndOr(AndOrContext c) {
        if (print) printVarAndProdName(c);
        Node n = null;
        if (c.AND() != null) {
            n = new AndNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.AND().getSymbol().getLine());
        } else if (c.OR() != null) {
            n = new OrNode(visit(c.exp(0)), visit(c.exp(1)));
            n.setLine(c.OR().getSymbol().getLine());
        }
        return n;
    }

    // NOT
    @Override
    public Node visitNot(NotContext c) {
        if (print) printVarAndProdName(c);
        Node n = new NotNode(visit(c.exp()));
        n.setLine(c.NOT().getSymbol().getLine());
        return n;
    }

    // OOP EXTENSION


    @Override
    public Node visitCldec(CldecContext ctx) {
        if (print) printVarAndProdName(ctx);

        // if there is a superclass, start will be 2, otherwise 1
        int start = 1;
        String superId = "";
        // if there is a superclass, get the id of the superclass
        if (ctx.EXTENDS() != null) superId = ctx.ID(start++).getText();

        var fields = new ArrayList<FieldNode>();
        var methods = new ArrayList<MethodNode>();

        // get all the fields
        for (int i = start, j = 0; i < ctx.ID().size(); i++, j++) {
            var f = new FieldNode(ctx.ID(i).getText(), (TypeNode) visit(ctx.type(j)));
            f.setLine(ctx.ID(i).getSymbol().getLine());
            fields.add(f);
        }

        // get all the methods
        for (var m : ctx.methdec()) methods.add((MethodNode) visit(m));

        Node n = null;
        if (ctx.ID().size() > 0 ) {
            n = new ClassNode(ctx.ID(0).getText(), superId, fields, methods);
            n.setLine(ctx.ID(0).getSymbol().getLine());
        }
        return n;
    }

    @Override
    public Node visitMethdec(MethdecContext ctx) {
        if (print) printVarAndProdName(ctx);
        // visit parameters and add them to parlist
        var parList = new ArrayList<ParNode>();
        for (int i = 1; i < ctx.ID().size(); i++) {
            var p = new ParNode(ctx.ID(i).getText(), (TypeNode) visit(ctx.type(i)));
            p.setLine(ctx.ID(i).getSymbol().getLine());
            parList.add(p);
        }
        // visit declarations and add them to declist
        var decList = new ArrayList<DecNode>();
        for (var dec : ctx.dec()) decList.add((DecNode) visit(dec));

        Node n = null;
        if (ctx.ID().size() > 0){
            n = new MethodNode(ctx.ID(0).getText(), (TypeNode) visit(ctx.type(0)), parList, decList, visit(ctx.exp()));
            n.setLine(ctx.FUN().getSymbol().getLine());
        }
        return n;
    }

    @Override
    public Node visitNew(NewContext ctx) {
        if (print) printVarAndProdName(ctx);

        var argList = new ArrayList<Node>(); // list of arguments
        for (var arg : ctx.exp()) argList.add(visit(arg)); // visit each argument
        var n = new NewNode(ctx.ID().getText(), argList); // create new node
        n.setLine(ctx.ID().getSymbol().getLine()); // set line

        return n;
    }

    @Override
    public Node visitNull(NullContext ctx) {
        if (print) printVarAndProdName(ctx);
        var n = new EmptyNode();
        n.setLine(ctx.NULL().getSymbol().getLine());
        return n;
    }

    @Override
    public Node visitDotCall(DotCallContext ctx) {
        if (print) printVarAndProdName(ctx);
        var argList = new ArrayList<Node>(); // list of arguments
        for (var arg : ctx.exp()) argList.add(visit(arg)); // visit each argument and populate list
        // new ClassCallNode(objectID, methodID, arguments)
        var n = new ClassCallNode(ctx.ID(0).getText(), ctx.ID(1).getText(), argList); // create new node
        n.setLine(ctx.ID(0).getSymbol().getLine()); // set line
        return n;
    }

    @Override
    public Node visitIdType(IdTypeContext ctx) {
        if (print) printVarAndProdName(ctx);
        var n = new RefTypeNode(ctx.ID().getText());
        n.setLine(ctx.ID().getSymbol().getLine());
        return n;
    }
}
