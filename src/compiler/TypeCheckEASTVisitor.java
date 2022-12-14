package compiler;

import compiler.AST.*;
import compiler.exc.IncomplException;
import compiler.exc.TypeException;
import compiler.lib.BaseEASTVisitor;
import compiler.lib.Node;
import compiler.lib.TypeNode;

import static compiler.TypeRels.*;

//visitNode(n) fa il type checking di un Node n e ritorna:
//- per una espressione, il suo tipo (oggetto BoolTypeNode o IntTypeNode)
//- per una dichiarazione, "null"; controlla la correttezza interna della dichiarazione
//(- per un tipo: "null"; controlla che il tipo non sia incompleto)
//
//visitSTentry(s) ritorna, per una STentry s, il tipo contenuto al suo interno
public class TypeCheckEASTVisitor extends BaseEASTVisitor<TypeNode, TypeException> {

    TypeCheckEASTVisitor() {
        super(true);
    } // enables incomplete tree exceptions

    TypeCheckEASTVisitor(boolean debug) {
        super(true, debug);
    } // enables print for debugging

    //checks that a type object is visitable (not incomplete)
    private TypeNode ckvisit(TypeNode t) throws TypeException {
        visit(t);
        return t;
    }

    @Override
    public TypeNode visitNode(ProgLetInNode n) throws TypeException {
        if (print) printNode(n);
        for (Node dec : n.declist)
            try {
                visit(dec);
            } catch (IncomplException e) {
                System.out.println("Incomplete declaration at line " + dec.getLine());
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        return visit(n.exp);
    }

    @Override
    public TypeNode visitNode(ProgNode n) throws TypeException {
        if (print) printNode(n);
        return visit(n.exp);
    }

    @Override
    public TypeNode visitNode(FunNode n) throws TypeException {
        if (print) printNode(n, n.id);
        for (Node dec : n.declist)
            try {
                visit(dec);
            } catch (IncomplException e) {
                System.out.println("Incomplete declaration at line " + dec.getLine());
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        if (!isSubtype(visit(n.exp), ckvisit(n.retType)))
            throw new TypeException("Wrong return type for function " + n.id, n.getLine());
        return null;
    }

    @Override
    public TypeNode visitNode(VarNode n) throws TypeException {
        if (print) printNode(n, n.id);
        if (!isSubtype(visit(n.exp), ckvisit(n.getType())))
            throw new TypeException("Incompatible value for variable " + n.id, n.getLine());
        return null;
    }

    @Override
    public TypeNode visitNode(PrintNode n) throws TypeException {
        if (print) printNode(n);
        return visit(n.exp);
    }

    @Override
    public TypeNode visitNode(IfNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.cond), new BoolTypeNode())))
            throw new TypeException("Non boolean condition in if", n.getLine());
        TypeNode t = visit(n.th);
        TypeNode e = visit(n.el);
        // old code
//        if (isSubtype(t, e)) return e;
//        if (isSubtype(e, t)) return t;
//        throw new TypeException("Incompatible types in then-else branches", n.getLine());
        // new code
        var lca = getLowestCommonAncestor(t, e);
        if (lca == null) {
            throw new TypeException("Incompatible types in then-else branches", n.getLine());
        } else {
            return lca;
        }
    }

    @Override
    public TypeNode visitNode(EqualNode n) throws TypeException {
        if (print) printNode(n);
        TypeNode l = visit(n.left);
        TypeNode r = visit(n.right);
        if (!(isSubtype(l, r) || isSubtype(r, l))) throw new TypeException("Incompatible types in equal", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(TimesNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode()) && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in multiplication", n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(PlusNode n) throws TypeException {
        if (print) printNode(n);
        if (!(isSubtype(visit(n.left), new IntTypeNode()) && isSubtype(visit(n.right), new IntTypeNode())))
            throw new TypeException("Non integers in sum", n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(CallNode n) throws TypeException {
        if (print) printNode(n, n.id);
        TypeNode t = visit(n.entry);
        if (!(t instanceof ArrowTypeNode) && !(t instanceof MethodTypeNode))
            throw new TypeException("Invocation of a non-function " + n.id, n.getLine());
        ArrowTypeNode fun;
        if (t instanceof MethodTypeNode mt) fun = mt.fun;
        else fun = (ArrowTypeNode) t;
        if (!(fun.parlist.size() == n.arglist.size()))
            throw new TypeException("Wrong number of parameters in the invocation of " + n.id, n.getLine());
        for (int i = 0; i < n.arglist.size(); i++)
            if (!(isSubtype(visit(n.arglist.get(i)), fun.parlist.get(i))))
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + n.id, n.getLine());
        return fun.ret;
    }

    @Override
    public TypeNode visitNode(IdNode n) throws TypeException {
        if (print) printNode(n, n.id);
        var t = visit(n.entry);
        if (t instanceof ArrowTypeNode)
            throw new TypeException("Wrong usage of function identifier " + n.id, n.getLine());
        if (t instanceof MethodTypeNode)
            throw new TypeException("Wrong usage of method identifier " + n.id, n.getLine());
        if (t instanceof ClassTypeNode) throw new TypeException("Wrong usage of class identifier " + n.id, n.getLine());
        return t;
    }

    @Override
    public TypeNode visitNode(BoolNode n) {
        if (print) printNode(n, n.val.toString());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(IntNode n) {
        if (print) printNode(n, n.val.toString());
        return new IntTypeNode();
    }

// gestione tipi incompleti	(se lo sono lancia eccezione)

    @Override
    public TypeNode visitNode(ArrowTypeNode n) throws TypeException {
        if (print) printNode(n);
        for (Node par : n.parlist) visit(par);
        visit(n.ret, "->"); //marks return type
        return null;
    }

    @Override
    public TypeNode visitNode(BoolTypeNode n) {
        if (print) printNode(n);
        return null;
    }

    @Override
    public TypeNode visitNode(IntTypeNode n) {
        if (print) printNode(n);
        return null;
    }

    // STentry (ritorna campo type)
    @Override
    public TypeNode visitSTentry(STentry entry) throws TypeException {
        if (print) printSTentry("type");
        return ckvisit(entry.type);
    }

    // OPERATOR EXTENSION

    @Override
    public TypeNode visitNode(MinusNode n) throws TypeException {
        if (print) printNode(n);
        if (!isSubtype(visit(n.left), new IntTypeNode()) || !isSubtype(visit(n.right), new IntTypeNode()))
            throw new TypeException("Non integers in subtraction", n.getLine());
        return new IntTypeNode();
    }

    @Override
    public TypeNode visitNode(DivNode n) throws TypeException {
        if (print) printNode(n);
        if (!isSubtype(visit(n.left), new IntTypeNode()) || !isSubtype(visit(n.right), new IntTypeNode()))
            throw new TypeException("Non integers in division", n.getLine());
        return super.visitNode(n);
    }

    @Override
    public TypeNode visitNode(GreaterEqualNode n) throws TypeException {
        if (print) printNode(n);
        TypeNode l = visit(n.left);
        TypeNode r = visit(n.right);
        if (!isSubtype(l, r) || !isSubtype(r, l))
            throw new TypeException("Incompatible types in greater equal", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(LessEqualNode n) throws TypeException {
        if (print) printNode(n);
        TypeNode l = visit(n.left);
        TypeNode r = visit(n.right);
        if (!isSubtype(l, r) || !isSubtype(r, l))
            throw new TypeException("Incompatible types in less equal", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(NotNode n) throws TypeException {
        if (print) printNode(n);
        if (!isSubtype(visit(n.exp), new BoolTypeNode())) throw new TypeException("Non boolean in not", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(OrNode n) throws TypeException {
        if (print) printNode(n);
        if (!isSubtype(visit(n.left), new BoolTypeNode()) || !isSubtype(visit(n.right), new BoolTypeNode()))
            throw new TypeException("Non boolean in or", n.getLine());
        return new BoolTypeNode();
    }

    @Override
    public TypeNode visitNode(AndNode n) throws TypeException {
        if (print) printNode(n);
        if (!isSubtype(visit(n.left), new BoolTypeNode()) || !isSubtype(visit(n.right), new BoolTypeNode()))
            throw new TypeException("Non boolean in and", n.getLine());
        return new BoolTypeNode();
    }

    // OOP EXTENSION


    @Override
    public TypeNode visitNode(ClassNode n) throws TypeException {
        if (print) printNode(n, n.id + ((n.superId == null) ? "" :  " extends " + n.superId));
        if (n.superId == null) {
            // No superclass.
            for (var m : n.methodList) visit(m);
        } else {
            // Superclass.
            superType.put(n.id, n.superId); // Update the superType map with the superclass of this class.

            var classType = n.type;
            var parentClassType = (ClassTypeNode) n.superEntry.type; // ParentCT in slide 49.

            // Check over fields.
            for (var field : n.fieldList) {
                // type checking optimization slide 49
                var pos = -field.offset - 1;
                if (pos < parentClassType.allFields.size()) {
                    // The field is already present in the superclass. Check if it is compatible. (It must be a subtype of the superclass field.)
                    // Control done here only for optimization purposes.
                    if (!isSubtype(classType.allFields.get(pos), parentClassType.allFields.get(pos))) {
                        throw new TypeException("Field " + field.id + " has type " + field.getType() + " but it should be " + parentClassType.allFields.get(pos), n.getLine());
                    }
                }
            }
            // Check over methods.
            for (var method : n.methodList) {
                // type checking optimization slide 49
                var pos = method.offset;
                if (pos < parentClassType.allMethods.size()) {
                    // The method is already present in the superclass. Check if the signatures are compatible. (They must be equal.)
                    // Control done here only for optimization purposes.
                    if (!isSubtype(classType.allMethods.get(pos), parentClassType.allMethods.get(pos))) {
                        throw new TypeException("Method " + method.id + " has type " + method.getType() + " but it should be " + parentClassType.allMethods.get(pos), n.getLine());
                    }
                }
            }
        }

        return null;
    }

    @Override
    public TypeNode visitNode(MethodNode n) throws TypeException {
        if (print) printNode(n, n.id);
        for (var dec : n.declist)
            try {
                visit(dec);
            } catch (IncomplException e) {
                System.out.println("Incomplete declaration at line " + dec.getLine());
            } catch (TypeException e) {
                System.out.println("Type checking error in a declaration: " + e.text);
            }
        if (!isSubtype(visit(n.exp), ckvisit(n.retType)))
            throw new TypeException("Wrong return type in method " + n.id, n.getLine());
        return null;
    }

    @Override
    public TypeNode visitNode(ClassCallNode node) throws TypeException {
        // Same as CallNode, but with a different error message.
        if (print) printNode(node, node.objectId + "." + node.methodId);
        if (node.methodEntry == null) return null;
        TypeNode t = node.methodEntry.type;
        if (!(t instanceof MethodTypeNode))
            throw new TypeException("Invocation of a non-function " + node.objectId + "." + node.methodId, node.getLine());
        ArrowTypeNode fun = ((MethodTypeNode) t).fun;
        if (!(fun.parlist.size() == node.arglist.size()))
            throw new TypeException("Wrong number of parameters in the invocation of " + node.objectId + "." + node.methodId, node.getLine());
        for (int i = 0; i < node.arglist.size(); i++)
            if (!(isSubtype(visit(node.arglist.get(i)), fun.parlist.get(i))))
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + node.objectId + "." + node.methodId, node.getLine());
        return fun.ret;
    }

    @Override
    public TypeNode visitNode(NewNode n) throws TypeException {
        if (print) printNode(n, n.id);
        if (n.entry == null) throw new TypeException("Class " + n.id + " not declared. Invalid type.", n.getLine());

        var classType = (ClassTypeNode) n.entry.type;
        // Check if the constructor is called with the right number of parameters.
        if (classType.allFields.size() != n.arglist.size())
            throw new TypeException("Wrong number of parameters in the invocation of " + n.id, n.getLine());

        // Check if the parameters are of the right type.
        for (int i = 0; i < n.arglist.size(); i++) {
            if (!isSubtype(visit(n.arglist.get(i)), classType.allFields.get(i)))
                throw new TypeException("Wrong type for " + (i + 1) + "-th parameter in the invocation of " + n.id, n.getLine());
        }

        return new RefTypeNode(n.id);
    }

    @Override
    public TypeNode visitNode(EmptyNode n) throws TypeException {
        if (print) printNode(n);
        return new EmptyTypeNode();
    }

    @Override
    public TypeNode visitNode(MethodTypeNode n) throws TypeException {
        if (print) printNode(n);
        visit(n.fun);
        return null;
    }

    @Override
    public TypeNode visitNode(RefTypeNode n) throws TypeException {
        if (print) printNode(n);
        return null;
    }

}