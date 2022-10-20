package compiler;

import compiler.AST.*;
import compiler.exc.VoidException;
import compiler.lib.BaseASTVisitor;
import compiler.lib.Node;
import compiler.lib.TypeNode;

import java.util.*;

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
        super(debug, true);
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

        n.setType(new ArrowTypeNode(parTypes, n.retType));

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

        var localDec = new HashSet<String>();
        // Symbol table
        var hm = symTable.get(0);
        // Virtual table
        var virtualTable = new HashMap<String, STentry>();
        ClassTypeNode classType = null;

        // inheritance like written in slide 22 and so on
        if (n.superId.isEmpty()) {
            // not inherits, no superclass
            classType = new ClassTypeNode(new ArrayList<>(), new ArrayList<>());
        } else {
            // inherits, superclass
            if (classTable.containsKey(n.superId)) {
                // super class exists
                n.superEntry = symTable.get(0).get(n.superId);
                // create new content copying the super class, so that we can add new methods and fields without using references
                var superType = (ClassTypeNode) n.superEntry.type;
                var superMethods = new ArrayList<>(superType.allMethods);
                var superFields = new ArrayList<>(superType.allFields);
                classType = new ClassTypeNode(superMethods, superFields);
                virtualTable = new HashMap<>(classTable.get(n.superId));
            } else {
                // super class does not exist
                System.out.println("Super class " + n.superId + " at line " + n.getLine() + " not declared");
                stErrors++;
            }
        }

        // create the current STEntry
        n.type = classType;
        var currentEntry = new STentry(0, classType, decOffset--);

        // Add entry symbol table at level 0
        if (hm.put(n.id, currentEntry) != null) {
            System.out.println("Class id " + n.id + " at line " + n.getLine() + " already declared");
            stErrors++;
        }

        // add virtual table in the symbol table, slide 23
        symTable.add(virtualTable);
        // add class name in class table
        classTable.put(n.id, virtualTable);

        // enter in the class scope
        nestingLevel++;

        // visit fields
        var fieldOffset = -(classType.allFields.size()) - 1; // negative offset because fields starts from the bottom of the heap
        // for each field, add it to the class type and to the virtual table
        for (var field : n.fieldList) {
            // No visit needed.

            if (localDec.contains(field.id)) {
                System.out.println("Field id " + field.id + " at line " + field.getLine() + " already declared");
                stErrors++;
            } else {
                localDec.add(field.id);
                if (virtualTable.containsKey(field.id)) {
                    // overriding
                    if (virtualTable.get(field.id).type instanceof MethodTypeNode) {
                        // overriding a method -> error
                        System.out.println("Method id " + field.id + " at line " + field.getLine() + " already declared");
                        stErrors++;
                    } else {
                        // overriding a field -> ok
                        var prevOffset = virtualTable.get(field.id).offset;
                        var entry = new STentry(nestingLevel, field.getType(), prevOffset);
                        field.offset = prevOffset;
                        virtualTable.put(field.id, entry);
                        // preserve the offset, put the new field in the class type
                        classType.allFields.set(-prevOffset - 1, field.getType());
                    }
                } else {
                    // adding new field, no overriding
                    var entry = new STentry(nestingLevel, field.getType(), fieldOffset);
                    virtualTable.put(field.id, entry);
                    field.offset = fieldOffset;
                    fieldOffset--; // decrement the offset because the last field is a the bottom of the heap
                    classType.allFields.add(field.getType());
                }
            }
        }

        // visit methods
        var prevNLDecOffset = decOffset; // stores counter for offset of declarations at previous nesting level
        // reset offset for declarations at current nesting level, because methods are stored in the heap
        // the first method is at offset 0 and the last at offset size-1
        decOffset = classType.allMethods.size();
        for (var method : n.methodList) {
            if (localDec.contains(method.id)) {
                System.out.println("Method id " + method.id + " at line " + method.getLine() + " already declared");
                stErrors++;
            } else {
                localDec.add(method.id);
                visit(method); // virtual table is updated in the visit method
                if (virtualTable.containsKey(method.id)) {
                    // overriding
                    if (virtualTable.get(method.id).type instanceof MethodTypeNode) {
                        // overriding a method -> ok
                        var prevOffset = virtualTable.get(method.id).offset;
                        var entry = new STentry(nestingLevel, method.getType(), prevOffset);
                        method.offset = prevOffset;
                        virtualTable.put(method.id, entry);
                        // preserve the offset, put the new method in the class type
                        classType.allMethods.set(prevOffset, ((MethodTypeNode) method.getType()).fun);
                    } else {
                        // overriding a field -> error
                        System.out.println("Field id " + method.id + " at line " + method.getLine() + " already declared");
                        stErrors++;
                    }
                } else {
                    // adding new method, no overriding
                    var entry = new STentry(nestingLevel, method.getType(), decOffset);
                    virtualTable.put(method.id, entry);
                    method.offset = decOffset;
                    decOffset++; // increment the offset because the last method is at the top of the heap
                    classType.allMethods.add(((MethodTypeNode) method.getType()).fun);
                }
            }
        }

        // exit from the class scope
        symTable.remove(nestingLevel--);
        decOffset = prevNLDecOffset; // restore counter for offset of declarations at previous nesting level

        return null;
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
        var methodType = new MethodTypeNode(new ArrowTypeNode(parTypes, n.retType));
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
        for (var dec : n.declist) visit(dec);
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
        for (var arg : n.arglist) visit(arg);
        return null;
    }

    @Override
    public Void visitNode(EmptyNode n) throws VoidException {
        // Syntax: null
        if (print) printNode(n);
        return null;
    }

}
