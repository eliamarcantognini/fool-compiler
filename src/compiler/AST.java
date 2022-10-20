package compiler;

import compiler.lib.BaseASTVisitor;
import compiler.lib.DecNode;
import compiler.lib.Node;
import compiler.lib.TypeNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AST {

    public static class ProgLetInNode extends Node {
        final List<DecNode> declist;
        final Node exp;

        ProgLetInNode(List<DecNode> d, Node e) {
            declist = Collections.unmodifiableList(d);
            exp = e;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class ProgNode extends Node {
        final Node exp;

        ProgNode(Node e) {
            exp = e;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class FunNode extends DecNode {
        final String id;
        TypeNode retType;
        final List<ParNode> parlist;
        final List<DecNode> declist;
        final Node exp;

        FunNode(String i, TypeNode rt, List<ParNode> pl, List<DecNode> dl, Node e) {
            id = i;
            retType = rt;
            parlist = Collections.unmodifiableList(pl);
            declist = Collections.unmodifiableList(dl);
            exp = e;
        }

        void setType(TypeNode t) {type = t;}

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class ParNode extends DecNode {
        final String id;

        ParNode(String i, TypeNode t) {
            id = i;
            type = t;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class VarNode extends DecNode {
        final String id;
        final Node exp;

        VarNode(String i, TypeNode t, Node v) {
            id = i;
            type = t;
            exp = v;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class PrintNode extends Node {
        final Node exp;

        PrintNode(Node e) {
            exp = e;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IfNode extends Node {
        final Node cond;
        final Node th;
        final Node el;

        IfNode(Node c, Node t, Node e) {
            cond = c;
            th = t;
            el = e;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class EqualNode extends Node {
        final Node left;
        final Node right;

        EqualNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class TimesNode extends Node {
        final Node left;
        final Node right;

        TimesNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class PlusNode extends Node {
        final Node left;
        final Node right;

        PlusNode(Node l, Node r) {
            left = l;
            right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class CallNode extends Node {
        final String id;
        final List<Node> arglist;
        STentry entry;
        int nl;

        CallNode(String i, List<Node> p) {
            id = i;
            arglist = Collections.unmodifiableList(p);
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IdNode extends Node {
        final String id;
        STentry entry;
        int nl;

        IdNode(String i) {
            id = i;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class BoolNode extends Node {
        final Boolean val;

        BoolNode(boolean n) {
            val = n;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IntNode extends Node {
        final Integer val;

        IntNode(Integer n) {
            val = n;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class ArrowTypeNode extends TypeNode {
        final List<TypeNode> parlist;
        final TypeNode ret;

        ArrowTypeNode(List<TypeNode> p, TypeNode r) {
            parlist = Collections.unmodifiableList(p);
            ret = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class BoolTypeNode extends TypeNode {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    public static class IntTypeNode extends TypeNode {

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // OPERATOR EXTENSION

    // Grater Equal Node class
    public static class GreaterEqualNode extends Node {
        final Node left;
        final Node right;

        GreaterEqualNode(Node l, Node r) {
            this.left = l;
            this.right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // Less Equal Node class
    public static class LessEqualNode extends Node {
        final Node left;
        final Node right;

        LessEqualNode(Node l, Node r) {
            this.left = l;
            this.right = r;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // Not Node Class
    public static class NotNode extends Node {
        final Node exp;

        public NotNode(Node exp) {
            this.exp = exp;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // Minus Node Class
    public static class MinusNode extends Node {
        final Node left;
        final Node right;

        public MinusNode(Node left, Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // Div Node Class
    public static class DivNode extends Node {
        final Node left;
        final Node right;

        public DivNode(Node left, Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // Or Node Class
    public static class OrNode extends Node {
        final Node left;
        final Node right;

        public OrNode(Node left, Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // And Node Class
    public static class AndNode extends Node {
        final Node left;
        final Node right;

        public AndNode(Node left, Node right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // OOP Extension

    // Class Node Class
    public static class ClassNode extends DecNode {
        final String id;
        final String superId;
        final List<FieldNode> fieldList;
        final List<MethodNode> methodList;
        ClassTypeNode type;
        STentry superEntry;

        public ClassNode(String id, String superId, List<FieldNode> fieldList, List<MethodNode> methodList) {
            this.id = id;
            this.superId = superId;
            this.fieldList = Collections.unmodifiableList(fieldList);
            this.methodList = Collections.unmodifiableList(methodList);
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // Field Node Class
    public static class FieldNode extends DecNode {
        final String id;
        int offset;

        public FieldNode(String id, TypeNode type) {
            this.id = id;
            this.type = type;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // Method Node Class
    public static class MethodNode extends DecNode {
        final String id;
        final TypeNode retType; // return type
        final List<ParNode> parlist; // list of parameters
        final List<DecNode> declist; // list of declarations
        final Node exp;
        int offset;
        String label;

        public MethodNode(String id, TypeNode retType, List<ParNode> parlist, List<DecNode> declist, Node exp) {
            this.id = id;
            this.retType = retType;
            this.parlist = Collections.unmodifiableList(parlist);
            this.declist = Collections.unmodifiableList(declist);
            this.exp = exp;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }

        public void setType(MethodTypeNode methodType) {
            this.type = methodType;
        }
    }

    // Class Call Node Class
    public static class ClassCallNode extends Node {

        // call: ID1.ID2()
        final String objectId; // (ID1) - id of the class
        final String methodId; // (ID2) - id of the class method to be called
        final List<Node> arglist;

        STentry entry; // the object entry (ID1)
        STentry methodEntry; // the method entry (ID2)
        int nl;

        public ClassCallNode(String objectId, String methodId, List<Node> arglist) {
            this.objectId = objectId;
            this.methodId = methodId;
            this.arglist = arglist;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // New Node Class
    public static class NewNode extends Node {
        final String id;
        final List<Node> arglist;
        STentry entry;

        public NewNode(String id, List<Node> arglist) {
            this.id = id;
            this.arglist = arglist;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // Empty Node Class
    public static class EmptyNode extends DecNode {
        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // Class Type Node Class
    public static class ClassTypeNode extends TypeNode {
        // It's a class type. It stores all the information about the class.
        // Including the fields' and methods' types (the intherited ones, too)

        final ArrayList<ArrowTypeNode> allMethods; // all methods in the class
        final ArrayList<TypeNode> allFields; // all fields in the class

        public ClassTypeNode(ArrayList<ArrowTypeNode> allMethods, ArrayList<TypeNode> allFields) {
            this.allMethods = allMethods;
            this.allFields = allFields;
        }


        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // Method Type Node Class
    public static class MethodTypeNode extends TypeNode {
        // A wrapper class for ArrowTypeNode. It's used to store the method type in the class type.
        // Useful to understand if we have a method call or a function call.

        final ArrowTypeNode fun; // the method with all the information about it

        public MethodTypeNode(ArrowTypeNode fun) {
            this.fun = fun;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // Ref Type Node Class
    public static class RefTypeNode extends TypeNode {

        final String id; // the id of the class

        public RefTypeNode(String id) {
            this.id = id;
        }

        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }

    // Empty Type Node Class
    public static class EmptyTypeNode extends TypeNode {
        @Override
        public <S, E extends Exception> S accept(BaseASTVisitor<S, E> visitor) throws E {
            return visitor.visitNode(this);
        }
    }


}