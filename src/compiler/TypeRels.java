package compiler;

import compiler.AST.*;
import compiler.lib.TypeNode;

import java.util.HashMap;
import java.util.Map;

public class TypeRels {

    // map from class ID to superclass ID, it defines the inheritance relation of RefTypeNodes. Slide 30
    public static Map<String, String> superType = new HashMap<>(); // to update when visiting ClassNode within superId field

    public static boolean isSubtype(TypeNode a, TypeNode b) {

        // a is null and b is a class ref -> a is a subtype of b
        if ((a instanceof EmptyTypeNode) && (b instanceof RefTypeNode)) return true;

        if (a instanceof RefTypeNode cA && b instanceof RefTypeNode cB) {
            // a is a class ref and b is a class ref
            // if a and b are the same class -> a is a subtype of b
            if (cA.id.equals(cB.id)) return true;
            // check if a is a subtype of b
            return isSuperClass(cA, cB);
        }

        if ((a instanceof ArrowTypeNode fA) && (b instanceof ArrowTypeNode fB)) {
            // check if the return type of a is a subtype of the return type of b
            if (!isSubtype(fA.ret, fB.ret)) return false; // covariance of returns' type
            // check if parlist of a is the same size of parlist of b
            if (fA.parlist.size() != fB.parlist.size()) return false;
            // check if the parameters of a are subtypes of the parameters of b
            for (int i = 0; i < fA.parlist.size(); i++) {
                if (!isSubtype(fB.parlist.get(i), fA.parlist.get(i)))
                    return false; // contravariance of parameters' type
            }
            return true;
        }

        // this remains the same
        // evaluate if a <= b, where a and b are primitive types (IntTypeNode or BoolTypeNode)
//        if (a.getClass().equals(b.getClass()) || ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode)))
//            return true;
        // Rewritten like this to avoid the use of getClass() which throws an exception if a or b is null
        return (
                (a instanceof BoolTypeNode) && (b instanceof IntTypeNode))
                || ((a instanceof IntTypeNode) && (b instanceof IntTypeNode))
                || ((a instanceof BoolTypeNode) && (b instanceof BoolTypeNode)
        );
    }

    private static boolean isSuperClass(final RefTypeNode a, final RefTypeNode b) {
        var superClass = a.id;
        // TODO. Check if it works correctly.
        // Roll up the inheritance tree until we find the class b or we reach the top of the tree
        while (!superClass.isEmpty() && !b.id.equals(superClass))
            superClass = superType.get(superClass) == null ? "" : superType.get(superClass);
        return !superClass.isEmpty();
    }

    public static TypeNode getLowestCommonAncestor(final TypeNode a, final TypeNode b) {
        // Refers to slide 51

        // a is a class ref and b is null -> return a
        if (a instanceof RefTypeNode && b instanceof EmptyTypeNode) return a;
        // b is a class ref and a is null -> return b
        if (a instanceof EmptyTypeNode && b instanceof RefTypeNode) return b;

        if (a instanceof RefTypeNode cA && b instanceof RefTypeNode cB) {
            var classA = cA.id;
            var classB = cB.id;
            if (classA.equals(classB)) return a; // if a and b are the same class -> return a

            var superClassId = classA;
            while (!superClassId.isEmpty()) {
                superClassId = superType.get(classA) == null ? "" : superType.get(classA);
                var superClass = new RefTypeNode(superClassId);
                if (isSubtype(b, superClass)) return superClass;
                classA = superClassId;
            }
        }

        // a and b are primitive types
        if (isSubtype(a, new IntTypeNode()) && isSubtype(b, new IntTypeNode()))
            if (a instanceof IntTypeNode || b instanceof IntTypeNode) // if a or b is IntTypeNode -> return IntTypeNode
                return new IntTypeNode();
            else return new BoolTypeNode();

        return null; // Every other case
    }

}
