package compiler;

import compiler.AST.*;
import compiler.lib.*;

import java.util.HashMap;
import java.util.Map;

public class TypeRels {

	// map from class ID to superclass ID, it defines the inheritance relation of RefTypeNodes. Slide 30
	public static Map<String, String> superType = new HashMap<>(); // to update when visiting ClassNode within superId field

	public static boolean isSubtype(TypeNode a, TypeNode b) {

		// a is null and b is a class ref -> a is a subtype of b
		if ((a instanceof EmptyTypeNode) && (b instanceof RefTypeNode))
			return true;

		// null can't be a super class
		if (b instanceof EmptyTypeNode)
			return false;

		if (a instanceof RefTypeNode cA && b instanceof RefTypeNode cB) {
			// a is a class ref and b is a class ref
			// if a and b are the same class -> a is a subtype of b
			if (cA.id.equals(cB.id)) return true;
			// check if a is a subtype of b
			return isSuperClass(cA, cB);
		}

		if ((a instanceof ArrowTypeNode fA) && (b instanceof ArrowTypeNode fB)) {
			// TODO. Implement this case. Slide 31
			return true;
		}

		// this remains the same
		// evaluate if a <= b, where a and b are primitive types (IntTypeNode or BoolTypeNode)
		return a.getClass().equals(b.getClass()) || ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode));
	}

	private static boolean isSuperClass(final RefTypeNode a, final RefTypeNode b) {
		var superClass = a.id;
		// TODO. Check if it works correctly.
		while (superClass != null && !superClass.equals(b.id)) superClass = superType.get(superClass);
		return superClass != null;
	}

}
