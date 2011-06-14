package salsa_lite.compiler.symbol_table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

import salsa_lite.compiler.definitions.CCompilationUnit;

public class SymbolTable {

	static String currentModule;
	static String runtimeModule;
	static String languageModule;
	static String workingDirectory;

	public static String currentImportModule;

	public static void setCurrentModule(String module) { currentModule = module; }
	public static void setWorkingDirectory(String directory) { workingDirectory = directory; }
	public static String getCurrentModule() { return currentModule; }
	public static String getRuntimeModule() { return runtimeModule; }
	public static String getLanguageModule() { return languageModule; }
	public static String getWorkingDirectory() { return workingDirectory; }


    private static HashMap<String,TypeSymbol> knownTypes = new HashMap<String,TypeSymbol>();
    private static TreeMap<String,String> namespace = new TreeMap<String,String>();

    public static boolean isExpressionContinuation = false;

    public static boolean isActor(String name) throws SalsaNotFoundException {
        TypeSymbol knownType = getTypeSymbol(name);

        if (knownType == null) {
            System.err.println("COMPILER ERROR: Could not determine if [" + name + "] is an actor, because it's type is unknown.");
            return false;
        } else return knownType instanceof ActorType;
    }

    public static boolean isObject(String name) throws SalsaNotFoundException {
        TypeSymbol knownType = getTypeSymbol(name);
       
        if (knownType == null) {
            System.err.println("COMPILER ERROR: Could not determine if [" + name + "] is an object, because it's type is unknown.");
            return false;
        } else return (knownType instanceof ObjectType) || (knownType instanceof ArrayType);
    }

    public static boolean isMutableObject(TypeSymbol knownType) {
        if (knownType instanceof ArrayType) return true;
        else if (knownType instanceof ObjectType) {
            ObjectType ot = (ObjectType)knownType;
            return ot.isMutable;
        } else {
            return false;
        }
    }

    public static boolean isPrimitive(String name) throws SalsaNotFoundException {
        TypeSymbol knownType = getTypeSymbol(name);

        if (knownType == null) {
            System.err.println("COMPILER ERROR: Could not determine if [" + name + "] is primitive, because it's type is unknown.");
            return false;
        } else return (knownType instanceof PrimitiveType);
    }

    public static boolean isArray(String name) throws SalsaNotFoundException {
        TypeSymbol knownType = getTypeSymbol(name);

        if (knownType == null) {
            System.err.println("COMPILER ERROR: Could not determine if [" + name + "] is primitive, because it's type is unknown.");
            return false;
        } else return (knownType instanceof ArrayType);
    }


    public static TypeSymbol getTypeSymbol(String name) throws SalsaNotFoundException {
        TypeSymbol knownType;
        
        String longSignature = namespace.get(name);
        if (longSignature != null) {
            knownType = knownTypes.get(longSignature);
        } else {
            knownType = knownTypes.get(name);
        }

        if (knownType == null) {
            return load(name);
        } else {
            return knownType;
        }
    }

    public static TypeSymbol load(String name) throws SalsaNotFoundException {
        TypeSymbol st;
        if (name.charAt(0) == '[' || name.charAt(name.length() - 1) == ']') {
            st = loadArray(name);
        } else if (ActorType.findSalsaFile(name) != null) {
            if (!name.contains(".")) st = loadActor( getCurrentModule() + name);
            else st = loadActor(name);
        } else {
            if (!name.contains(".")) st = loadObject( getCurrentModule() + name);
            else st = loadObject(name);
        }

        return st;
    }

    public static void loadPrimitive(String name) {
        if (knownTypes.get(name) != null) return;

        PrimitiveType pt = new PrimitiveType(name);
        knownTypes.put(name, pt);
        namespace.put(pt.getName(), pt.getLongSignature());                         //System.err.println("New namespace entry [primitive]: " + pt.getName() + " -- " + pt.getLongSignature());
    }

    public static void setImmutableObject(String name) {
        ((ObjectType)knownTypes.get(name)).isMutable = false;
    }

    public static ArrayType loadArray(String name) throws SalsaNotFoundException {
        if (knownTypes.get(name) != null) return (ArrayType)knownTypes.get(name);

        ArrayType arrayType = new ArrayType(name);
        knownTypes.put(arrayType.getLongSignature(), arrayType);
        return arrayType;
    }

    public static ObjectType loadObject(String name) throws SalsaNotFoundException {
        if (knownTypes.get(name) != null) return (ObjectType)knownTypes.get(name);

        ObjectType objectType = new ObjectType(name);
        knownTypes.put(objectType.getLongSignature(), objectType);
        namespace.put(objectType.getName(), objectType.getLongSignature());         //System.err.println("New namespace entry [object]: " + objectType.getName() + " -- " + objectType.getLongSignature());
        objectType.load();

        return objectType;
    }

    public static ActorType loadActor(String name) throws SalsaNotFoundException {
        TypeSymbol ts = knownTypes.get(name);
        if (ts != null) {
            if (!(ts instanceof ActorType)) {
                System.err.println("COMPILER ERROR [SymbolTable.loadActor]: tried to load an actor from name '" + name + "' but got a non-actor type '" + ts.getLongSignature() + "'.");
                throw new RuntimeException();
            }

            return (ActorType)ts;
        }

        ActorType actorType = new ActorType(name);
        knownTypes.put(actorType.getLongSignature(), actorType);
        namespace.put(actorType.getName(), actorType.getLongSignature());           //System.err.println("New namespace entry [actor]: " + actorType.getName() + " -- " + actorType.getLongSignature());
        actorType.load();

        return actorType;
    }

	static SymbolTableScope base_scope;
	static SymbolTableScope scope;

	public static void openScope() {
		SymbolTableScope newScope = new SymbolTableScope();
		newScope.parent = scope;

		if (scope == null) newScope.depth = 0;
		else newScope.depth = newScope.parent.depth + 1;

		scope = newScope;
	}

	public static void closeScope() {
		scope = scope.parent;
	}

    public static void addVariableType(String name, String type, boolean isToken, boolean isStatic) throws SalsaNotFoundException {
        TypeSymbol typeSymbol = getTypeSymbol(type);
        VariableTypeSymbol variableTypeSymbol = new VariableTypeSymbol(name, typeSymbol, isToken, isStatic);

        addVariableType(name, variableTypeSymbol);
    }

	public static void addVariableType(String name, VariableTypeSymbol type) {
		VariableTypeSymbol symbolInScope = scope.getVariableType(name, false);

        if (symbolInScope != null) {
            System.err.println("COMPILER WARNING [SymbolTable.addVariableType]: Conflict of declarations. '" + name + "' already declared in current scope as '" + symbolInScope.getType().getLongSignature() + "', trying to redefine as '" + type.getType().getLongSignature() + "'");

            if (!symbolInScope.getType().equals( type.getType() )) throw new RuntimeException();
        } else {
			scope.addVariableType(name, type);
        }
	}

    public static VariableTypeSymbol getVariableTypeSymbol(String name) throws SalsaNotFoundException {
		VariableTypeSymbol variableTypeSymbol = scope.getVariableType(name);

        if (variableTypeSymbol == null) {
            System.err.println("COMPILER WARNING [SymbolTable.getVariableType]: Lookup of type for variable '" + name + "' failed. Attempting to load it as a Class for static invocations.");
            //It might be a type for a static method
            TypeSymbol typeSymbol = load(name);

            addVariableType(name, name, false, true);
            variableTypeSymbol = scope.getVariableType(name);

            if (variableTypeSymbol == null) {
                System.err.println("COMPILER ERROR [SymbolTable.getVariableType]: Lookup of type for variable '" + name + "' failed.");
                throw new RuntimeException();
//            scope.printScope(true);
//                return null;
            }
        }

        return variableTypeSymbol;
    }

	public static TypeSymbol getVariableType(String name) throws SalsaNotFoundException {
        return getVariableTypeSymbol(name).getType();
	}

    public static boolean isToken(String name) throws SalsaNotFoundException {
        return getVariableTypeSymbol(name).isToken();
    }

    static TypeSymbol continuationType;

	public static void setContinuationType(TypeSymbol continuationType) {
		SymbolTable.continuationType = continuationType;
	}

	public static TypeSymbol getContinuationType() {
		return continuationType;
	}

	public static String currentMessageName = "";

	public static boolean continuesToPass = false;
	public static boolean token_pass_exception = false;
	public static boolean continuation_pass_exception = false;
	public static boolean messageContinues;
	public static boolean messageRequiresContinuation;
	public static boolean withinArguments = false;
	public static int generated_implicit_tokens = 0;
	public static int generated_expression_directors = 0;
	public static boolean implicitMessage = false;

	public static void newMessageHandler() {
		scope.joinDirectors = 0;
		generated_implicit_tokens = 0;
		generated_expression_directors = 0;
	}

	public static String getImplicitTokenName() {
		return "implicit_token_" + (++generated_implicit_tokens);
	}

	public static String getExpressionDirectorName() {
		return "ExpressionDirector" + (++generated_expression_directors);
	}

	static int generated_message_names = 0;
	public static String getNewLoopMessageName() {
		return "unnamed_" + (++generated_message_names) + "_loop";
	}

	public static String getNewUnnamedMessageName() {
		return "unnamed_" + (++generated_message_names);
	}

	public static void newJoinDirector() {
		scope.joinDirectors++;
		scope.inJoinDirector = true;
	}

	public static void closeJoinDirector() {
		scope.inJoinDirector = false;
	}

	public static String getJoinDirector() {
		return scope.getJoinDirector();
	}

	public static boolean firstContinuation() {
		return scope.firstContinuation();
	}

	public static void initializedFirstContinuation() {
		scope.firstContinuation = false;
	}

	public static TypeSymbol getDominatingType(TypeSymbol type1, TypeSymbol type2) throws SalsaNotFoundException {
//        System.err.println("getting dominating type2: " + type2.getLongSignature());
//        System.err.println("getting dominating type1: " + type1.getLongSignature());

        if (type1.getName().equals("String") || type2.getName().equals("String")) return SymbolTable.getTypeSymbol("String");
		else if (type1.getName().equals("double") || type2.getName().equals("double")) return SymbolTable.getTypeSymbol("double");
		else if (type1.getName().equals("float") || type2.getName().equals("float")) return SymbolTable.getTypeSymbol("float");
		else if (type1.getName().equals("short") || type2.getName().equals("short")) return SymbolTable.getTypeSymbol("short");
		else if (type1.getName().equals("long") || type2.getName().equals("long")) return SymbolTable.getTypeSymbol("long");
		else if (type1.getName().equals("int") || type2.getName().equals("int")) return SymbolTable.getTypeSymbol("int");
		else return type1;
	}

    public static void loadDefaultPrimitive(String name) throws SalsaNotFoundException {
        loadPrimitive(name);
        addVariableType(name, name, false, true);
    }

    public static void loadDefaultObject(String name, boolean isImmutable) throws SalsaNotFoundException {
        loadObject(name);
        if (isImmutable) setImmutableObject(name);
        addVariableType(getTypeSymbol(name).getName(), name, false, true);
    }

    public static void resetSymbolTable(CCompilationUnit cu) {
        knownTypes = new HashMap<String,TypeSymbol>();
        namespace = new TreeMap<String,String>();

        base_scope = null;
        scope = null;

        openScope();

		runtimeModule = "salsa_lite.";
		if (System.getProperty("local") != null) runtimeModule += "local";
		else if (System.getProperty("local_noref") != null) runtimeModule += "local_noref";
		else if (System.getProperty("local_fcs") != null) runtimeModule += "local_fcs";
		else if (System.getProperty("wwc") != null) runtimeModule += "wwc";
		languageModule = runtimeModule + ".language.";

        
        String module = cu.getModule();
        if (module == null) module = "";
        else module += ".";

        setCurrentModule(module);

        String currentFile = ActorType.findSalsaFile(module + cu.getName());
        if (currentFile == null) {
            System.err.println("COMPILER ERROR [SymbolTable.resetSymbolTable]: SALSA behavior '" + cu.getName() + "' is not in it's specfied package '" + module + "'");
            System.exit(0);
        }

        try {

            /**
             * Load the primitive types
             */
            loadDefaultPrimitive("void");
            loadDefaultPrimitive("null");
            loadDefaultPrimitive("ack");
            loadDefaultPrimitive("boolean");
            loadDefaultPrimitive("byte");
            loadDefaultPrimitive("char");
            loadDefaultPrimitive("short");
            loadDefaultPrimitive("int");
            loadDefaultPrimitive("long");
            loadDefaultPrimitive("float");
            loadDefaultPrimitive("double");

            /**
             * Make sure we know the symbols from everything in java.lang as these are imported by default
             */

            /**
             * Import interfaces from java.lang
             */
            loadDefaultObject("java.lang.Appendable", false);
            loadDefaultObject("java.lang.CharSequence", false);
            loadDefaultObject("java.lang.Cloneable", false);
            loadDefaultObject("java.lang.Comparable", false);
            loadDefaultObject("java.lang.Iterable", false);
            loadDefaultObject("java.lang.Readable", false);
            loadDefaultObject("java.lang.Runnable", false);
            //loadDefaultObject("java.lang.Thread.UncaughtExceptionHandler", false);

            /**
             * Import classes from java.lang
             */
            loadDefaultObject("java.lang.Boolean", true);
            loadDefaultObject("java.lang.Byte", true);
            loadDefaultObject("java.lang.Character", true);
            loadDefaultObject("java.lang.Class", false);
    //        loadDefaultObject("java.lang.ClassLoader", false);
            loadDefaultObject("java.lang.Compiler", false);
            loadDefaultObject("java.lang.Double", true);
            loadDefaultObject("java.lang.Enum", false);
            loadDefaultObject("java.lang.Float", true);
            loadDefaultObject("java.lang.InheritableThreadLocal", false);
            loadDefaultObject("java.lang.Integer", true);
            loadDefaultObject("java.lang.Long", true);
            loadDefaultObject("java.lang.Math", false);
            loadDefaultObject("java.lang.Number", true);
            loadDefaultObject("java.lang.Object", false);
            loadDefaultObject("java.lang.Package", false);
    //        loadDefaultObject("java.lang.Process", false);
    //        loadDefaultObject("java.lang.ProcessBuilder", false);
            loadDefaultObject("java.lang.Runtime", false);
            loadDefaultObject("java.lang.RuntimePermission", false);
            loadDefaultObject("java.lang.SecurityManager", false);
            loadDefaultObject("java.lang.Short", true);
            loadDefaultObject("java.lang.StackTraceElement", false);
            loadDefaultObject("java.lang.StrictMath", false);
            loadDefaultObject("java.lang.String", true);
            loadDefaultObject("java.lang.StringBuffer", false);
            loadDefaultObject("java.lang.System", false);
    //        loadDefaultObject("java.lang.Thread", false);
    //        loadDefaultObject("java.lang.ThreadGroup", false);
    //        loadDefaultObject("java.lang.ThreadLocal", false);
            loadDefaultObject("java.lang.Throwable", false);
            loadDefaultObject("java.lang.Void", false);

            /**
             * Import enums from java.lang
             */
    //        loadDefaultObject("java.lang.Thread.State", false);

            /**
             * Import exceptions from java.lang
             */
            loadDefaultObject("java.lang.ArithmeticException", false);
            loadDefaultObject("java.lang.ArrayIndexOutOfBoundsException", false);
            loadDefaultObject("java.lang.ArrayStoreException", false);
            loadDefaultObject("java.lang.ClassCastException", false);
            loadDefaultObject("java.lang.ClassNotFoundException", false);
            loadDefaultObject("java.lang.CloneNotSupportedException", false);
            loadDefaultObject("java.lang.EnumConstantNotPresentException", false);
            loadDefaultObject("java.lang.Exception", false);
            loadDefaultObject("java.lang.IllegalAccessException", false);
            loadDefaultObject("java.lang.IllegalArgumentException", false);
            loadDefaultObject("java.lang.IllegalMonitorStateException", false);
            loadDefaultObject("java.lang.IllegalStateException", false);
            loadDefaultObject("java.lang.IndexOutOfBoundsException", false);
            loadDefaultObject("java.lang.InstantiationException", false);
            loadDefaultObject("java.lang.InterruptedException", false);
            loadDefaultObject("java.lang.NegativeArraySizeException", false);
            loadDefaultObject("java.lang.NoSuchFieldException", false);
            loadDefaultObject("java.lang.NoSuchMethodException", false);
            loadDefaultObject("java.lang.NullPointerException", false);
            loadDefaultObject("java.lang.NumberFormatException", false);
            loadDefaultObject("java.lang.RuntimeException", false);
            loadDefaultObject("java.lang.SecurityException", false);
            loadDefaultObject("java.lang.StringIndexOutOfBoundsException", false);
            loadDefaultObject("java.lang.TypeNotPresentException", false);
            loadDefaultObject("java.lang.UnsupportedOperationException", false);

            /**
             * Import errors from java.lang
             */
            loadDefaultObject("java.lang.AbstractMethodError", false);
            loadDefaultObject("java.lang.AssertionError", false);
            loadDefaultObject("java.lang.ClassCircularityError", false);
            loadDefaultObject("java.lang.ClassFormatError", false);
            loadDefaultObject("java.lang.Error", false);
            loadDefaultObject("java.lang.ExceptionInInitializerError", false);
            loadDefaultObject("java.lang.IllegalAccessError", false);
            loadDefaultObject("java.lang.IncompatibleClassChangeError", false);
            loadDefaultObject("java.lang.InstantiationError", false);
            loadDefaultObject("java.lang.InternalError", false);
            loadDefaultObject("java.lang.LinkageError", false);
            loadDefaultObject("java.lang.NoClassDefFoundError", false);
            loadDefaultObject("java.lang.NoSuchFieldError", false);
            loadDefaultObject("java.lang.NoSuchMethodError", false);
            loadDefaultObject("java.lang.OutOfMemoryError", false);
            loadDefaultObject("java.lang.StackOverflowError", false);
            loadDefaultObject("java.lang.ThreadDeath", false);
            loadDefaultObject("java.lang.UnknownError", false);
            loadDefaultObject("java.lang.UnsatisfiedLinkError", false);
            loadDefaultObject("java.lang.UnsupportedClassVersionError", false);
            loadDefaultObject("java.lang.VerifyError", false);
            loadDefaultObject("java.lang.VirtualMachineError", false);

            /**
             * Load basic salsa_lite objects
             */
            ObjectType messageType = new ObjectType(runtimeModule + ".Message", SymbolTable.getTypeSymbol("Object"));
            FieldSymbol simpleMessageField = new FieldSymbol(messageType, "SIMPLE_MESSAGE", SymbolTable.getTypeSymbol("int"));
            messageType.fields.put(simpleMessageField.getLongSignature(), simpleMessageField);
            FieldSymbol argumentsField = new FieldSymbol(messageType, "arguments", SymbolTable.getTypeSymbol("Object[]"));
            messageType.fields.put(argumentsField.getLongSignature(), argumentsField);

            knownTypes.put( messageType.getLongSignature(), messageType );
            namespace.put( messageType.getName(), messageType.getLongSignature() );
            setImmutableObject(runtimeModule + ".Message");
            addVariableType("Message", "Message", false, true);


            ObjectType stageServiceType = new ObjectType(runtimeModule + ".StageService");
            MethodSymbol sendMessageMethod = new MethodSymbol(0, stageServiceType, "sendMessage", SymbolTable.getTypeSymbol("void"), new TypeSymbol[]{ SymbolTable.getTypeSymbol("Message") }, true);
            stageServiceType.method_handlers.put(sendMessageMethod.getLongSignature(), sendMessageMethod);

            knownTypes.put( stageServiceType.getLongSignature(), stageServiceType );
            namespace.put( stageServiceType.getName(), stageServiceType.getLongSignature() );
            addVariableType("StageService", "StageService", false, true);


            ObjectType stageType = new ObjectType(runtimeModule + ".Stage");
            FieldSymbol messageField = new FieldSymbol(stageType, "message", SymbolTable.getTypeSymbol("Message"));
            stageType.fields.put(messageField.getLongSignature(), messageField);

            knownTypes.put( stageType.getLongSignature(), stageType );
            namespace.put( stageType.getName(), stageType.getLongSignature() );
            addVariableType("Stage", "Stage", false, true);



            ActorType localActorType = new ActorType(runtimeModule + ".LocalActor", SymbolTable.getTypeSymbol("Object"));
            FieldSymbol stageField = new FieldSymbol(localActorType, "stage", SymbolTable.getTypeSymbol("Stage"));
            localActorType.fields.put(stageField.getLongSignature(), stageField);

            knownTypes.put(localActorType.getLongSignature(), localActorType);
            namespace.put(localActorType.getName(), localActorType.getLongSignature());
            addVariableType("stage", "Stage", false, true);     //should add an actor's parent fields to namespace as well instead of this hack

            FieldSymbol targetField = new FieldSymbol(messageType, "target", SymbolTable.getTypeSymbol("LocalActor"));
            messageType.fields.put(targetField.getLongSignature(), targetField);

            if (!cu.getName().equals("ContinuationDirector")) {
                //On the chance we're compiling ContinuationDirector
                FieldSymbol continuationDirectorField = new FieldSymbol(messageType, "continuationDirector", SymbolTable.getTypeSymbol(runtimeModule + ".language.ContinuationDirector"));
                messageType.fields.put(continuationDirectorField.getLongSignature(), continuationDirectorField);
            }

            ActorType currentActorType = new ActorType( getCurrentModule() + cu.getName() );
            knownTypes.put(currentActorType.getLongSignature(), currentActorType);
            namespace.put(currentActorType.getName(), currentActorType.getLongSignature());
            cu.getImportDeclarationCode();                      //this will load the current actor's dependencies
            currentActorType.load(cu, getCurrentModule());

            if (cu.getName().equals("ContinuatinoDirector")) {
                //On the chance we're compiling ContinuationDirector
                FieldSymbol continuationDirectorField = new FieldSymbol(messageType, "continuationDirector", SymbolTable.getTypeSymbol(runtimeModule + ".language.ContinuationDirector"));
                messageType.fields.put(continuationDirectorField.getLongSignature(), continuationDirectorField);
            }

        } catch (SalsaNotFoundException snfe) {
            System.err.println("COMPILER ERROR [SymbolTable.resetSymbolTable]: " + snfe.toString());
            snfe.printStackTrace();
            System.exit(0);
        }

//        loadActor(runtimeModule + ".ContinuationDirector");
//        loadActor(runtimeModule + ".TokenDirector");
//        loadActor(runtimeModule + ".ImplicitTokenDirector");
//        loadActor(runtimeModule + ".JoinDirector");
//        loadActor(runtimeModule + ".MessageDirector");
    }

    /**
     *  For testing purposes
     */
    public static void main(String[] arguments) {
//        resetSymbolTable();

        ArrayList<TypeSymbol> types = new ArrayList<TypeSymbol>( knownTypes.values() );
        Collections.sort(types);

        System.out.println();
        System.out.println("KNOWN OBJECTS:");
        for (TypeSymbol ts : types) {
            if (!(ts instanceof ObjectType)) continue;

            System.out.println(ts.getLongSignature());

            ObjectType ot = (ObjectType)ts;

            LinkedList<FieldSymbol> fields = new LinkedList<FieldSymbol>(ot.fields.values());
            for (FieldSymbol fs : fields) System.out.println("\t" + fs.getLongSignature());

            LinkedList<ConstructorSymbol> constructors = new LinkedList<ConstructorSymbol>(ot.constructors.values());
            for (ConstructorSymbol cs : constructors) System.out.println("\t" + cs.getLongSignature());

            LinkedList<MethodSymbol> method_handlers = new LinkedList<MethodSymbol>(ot.method_handlers.values());
            for (MethodSymbol ms : method_handlers) System.out.println("\t" + ms.getLongSignature());
        }

        System.out.println();
        System.out.println("KNOWN ACTORS:");
        for (TypeSymbol ts : types) {
            if (!(ts instanceof ActorType)) continue;

            System.out.println(ts.getLongSignature());

            ActorType at = (ActorType)ts;

            LinkedList<FieldSymbol> fields = new LinkedList<FieldSymbol>(at.fields.values());
            for (FieldSymbol fs : fields) System.out.println("\t" + fs.getLongSignature());

            LinkedList<ConstructorSymbol> constructors = new LinkedList<ConstructorSymbol>(at.constructors.values());
            for (ConstructorSymbol cs : constructors) System.out.println("\t" + cs.getLongSignature());

            LinkedList<MessageSymbol> message_handlers = new LinkedList<MessageSymbol>(at.message_handlers.values());
            for (MessageSymbol ms : message_handlers) System.out.println("\t" + ms.getLongSignature());
        }


        System.out.println();
        System.out.println("KNOWN ARRAYS:");
        for (TypeSymbol ts : types) {
            if (!(ts instanceof ArrayType)) continue;

            System.out.println(ts.getLongSignature());

            ArrayType at = (ArrayType)ts;

            LinkedList<FieldSymbol> fields = new LinkedList<FieldSymbol>(at.fields.values());
            for (FieldSymbol fs : fields) System.out.println("\t" + fs.getLongSignature());
        }

    }

    public static void printNamespace() {
        System.out.println();
        System.out.println("NAMESPACE:");
        for (Map.Entry<String,String> entry : namespace.entrySet()) {
            System.out.println(entry.getKey() + " -- " + entry.getValue());
        }
    }

}
