package salsa_lite.compiler.symbol_table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

import salsa_lite.compiler.definitions.CCompilationUnit;
import salsa_lite.compiler.definitions.CompilerErrors;

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


    public static HashMap<String,TypeSymbol> knownTypes = new HashMap<String,TypeSymbol>();
    public static TreeMap<String,String> namespace = new TreeMap<String,String>();

    public static boolean isExpressionContinuation = false;
    public static boolean is_mobile_actor = false;

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
        return getTypeSymbol(name, null);
    }

    public static TypeSymbol getTypeSymbol(String name, String superType) throws SalsaNotFoundException {
        TypeSymbol knownType = null;
        String baseType = name;
        String arrayDims = "";
        String genericDeclarations = "";

        if (name.charAt(0) == '[' || name.charAt(name.length() - 1) == ']') {
            arrayDims = ArrayType.getArrayDims(baseType);
            baseType = ArrayType.getBaseType(baseType);
        } else {
            baseType = name;
        }

        if (baseType.charAt(0) == '?') {
            if (baseType.startsWith("? extends")) {
//                System.err.println("returning wildcard type: " + baseType);
                knownType = new ObjectType("?", SymbolTable.getTypeSymbol(baseType.substring(10, baseType.length())));
            } else if (baseType.startsWith("? super")) {
//                System.err.println("superType: " + superType);
                knownType = new ObjectType("?", SymbolTable.getTypeSymbol(superType));
            } else {
//                System.err.println("superType: " + superType);
                knownType = new ObjectType("?", SymbolTable.getTypeSymbol(superType));
            }
        } else if (baseType.contains("<")) {
            genericDeclarations = baseType.substring(baseType.indexOf("<"), baseType.length());
            baseType = baseType.substring(0, baseType.indexOf("<"));
        }

        if (knownType == null) {
            String longSignature = namespace.get(baseType);
            if (longSignature != null) {
                knownType = knownTypes.get(longSignature);
            } else {
                knownType = knownTypes.get(baseType);
            }
        }

        if (knownType == null) {
            knownType = importEither(baseType);
        }

        if (!genericDeclarations.equals("")) {
            knownType = knownType.copy().replaceGenerics(genericDeclarations);
        }

        if (!arrayDims.equals("")) {
            knownType = new ArrayType(knownType, arrayDims);
        }

        return knownType;
    }

    public static TypeSymbol importEither(String name) throws SalsaNotFoundException {
        TypeSymbol st;
       
        if (ActorType.findSalsaFile(name) != null) {
            if (!name.contains(".")) st = importActor( getCurrentModule() + name);
            else st = importActor(name);
        } else {
            if (!name.contains(".")) st = importObject( getCurrentModule() + name);
            else st = importObject(name);
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

    public static ObjectType importObject(String name) throws SalsaNotFoundException {
        if (knownTypes.get(name) != null) return (ObjectType)knownTypes.get(name);

        ObjectType objectType = new ObjectType(name);
        knownTypes.put(objectType.getLongSignature(), objectType);
        namespace.put(objectType.getName(), objectType.getLongSignature());         //System.err.println("New namespace entry [object]: " + objectType.getName() + " -- " + objectType.getLongSignature());
        objectType.load();

        return objectType;
    }

    public static ActorType importActor(String name) throws SalsaNotFoundException {
        TypeSymbol ts = knownTypes.get(name);
        if (ts != null) {
            if (!(ts instanceof ActorType)) {
                System.err.println("COMPILER ERROR [SymbolTable.importActor]: tried to load an actor from name '" + name + "' but got a non-actor type '" + ts.getLongSignature() + "'.");
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

    public static void addGenericVariableType(String name, TypeSymbol typeSymbol) {
        addVariableType(name, new VariableTypeSymbol(name, typeSymbol, false, false, true));
    }

    public static void addVariableType(String name, String type, boolean isToken, boolean isStatic) throws SalsaNotFoundException {
        TypeSymbol typeSymbol = getTypeSymbol(type);
        VariableTypeSymbol variableTypeSymbol = new VariableTypeSymbol(name, typeSymbol, isToken, isStatic, false);

        addVariableType(name, variableTypeSymbol);
    }

	public static void addVariableType(String name, VariableTypeSymbol type) {
		VariableTypeSymbol symbolInScope = scope.getVariableType(name, false);

        if (symbolInScope != null) {
//            System.err.println("COMPILER WARNING [SymbolTable.addVariableType]: Conflict of declarations. '" + name + "' already declared in current scope as '" + symbolInScope.getType().getLongSignature() + "', trying to redefine as '" + type.getType().getLongSignature() + "'");

            if (!symbolInScope.getType().equals( type.getType() )) throw new RuntimeException();
        } else {
			scope.addVariableType(name, type);
        }
	}

    public static VariableTypeSymbol getVariableTypeSymbol(String name) throws SalsaNotFoundException {
		VariableTypeSymbol variableTypeSymbol = scope.getVariableType(name);

        if (variableTypeSymbol == null) {
//            System.err.println("COMPILER WARNING [SymbolTable.getVariableType]: Lookup of type for variable '" + name + "' failed. Attempting to load it as a Class for static invocations.");
            //It might be a type for a static method
            TypeSymbol typeSymbol = getTypeSymbol(name);

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

    public static boolean isGeneric(String name) throws SalsaNotFoundException {
        return getVariableTypeSymbol(name).isGeneric();
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
    public static boolean continuationTokenMessage = false;
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

    public static void importDefaultPrimitive(String name, boolean isToken) throws SalsaNotFoundException {
        loadPrimitive(name);
        addVariableType(name, name, isToken, true);
    }

    public static void importDefaultPrimitive(String name) throws SalsaNotFoundException {
        loadPrimitive(name);
        addVariableType(name, name, false, true);
    }

    public static void importDefaultObject(String name, boolean isImmutable) throws SalsaNotFoundException {
        importObject(name);
        if (isImmutable) setImmutableObject(name);
        addVariableType(getTypeSymbol(name).getName(), name, false, true);
    }

    public static void resetSymbolTable(CCompilationUnit cu) {
        knownTypes = new HashMap<String,TypeSymbol>();
        namespace = new TreeMap<String,String>();

        is_mobile_actor = false;

        base_scope = null;
        scope = null;

        openScope();

        currentModule = "";

		runtimeModule = "salsa_lite.runtime";
		languageModule = runtimeModule + ".language.";


        try {

            /**
             * Load the primitive types
             */
            importDefaultPrimitive("void");
            importDefaultPrimitive("null");
            importDefaultPrimitive("boolean");
            importDefaultPrimitive("byte");
            importDefaultPrimitive("char");
            importDefaultPrimitive("short");
            importDefaultPrimitive("int");
            importDefaultPrimitive("long");
            importDefaultPrimitive("float");
            importDefaultPrimitive("double");

            importDefaultPrimitive("ack", true);

            /**
             * Make sure we know the symbols from everything in java.lang as these are imported by default
             */

            /**
             * Import interfaces from java.lang
             */
            importDefaultObject("java.lang.Appendable", false);
            importDefaultObject("java.lang.CharSequence", false);
            importDefaultObject("java.lang.Cloneable", false);
            importDefaultObject("java.lang.Comparable", false);
            importDefaultObject("java.lang.Iterable", false);
            importDefaultObject("java.lang.Readable", false);
            importDefaultObject("java.lang.Runnable", false);
            //importDefaultObject("java.lang.Thread.UncaughtExceptionHandler", false);

            /**
             * Import classes from java.lang
             */
            importDefaultObject("java.lang.Boolean", true);
            importDefaultObject("java.lang.Byte", true);
            importDefaultObject("java.lang.Character", true);
            importDefaultObject("java.lang.Class", false);
    //        importDefaultObject("java.lang.ClassLoader", false);
            importDefaultObject("java.lang.Compiler", false);
            importDefaultObject("java.lang.Double", true);
            importDefaultObject("java.lang.Enum", false);
            importDefaultObject("java.lang.Float", true);
            importDefaultObject("java.lang.InheritableThreadLocal", false);
            importDefaultObject("java.lang.Integer", true);
            importDefaultObject("java.lang.Long", true);
            importDefaultObject("java.lang.Math", false);
            importDefaultObject("java.lang.Number", true);
            importDefaultObject("java.lang.Package", false);
    //        importDefaultObject("java.lang.Process", false);
    //        importDefaultObject("java.lang.ProcessBuilder", false);
            importDefaultObject("java.lang.Runtime", false);
            importDefaultObject("java.lang.RuntimePermission", false);
            importDefaultObject("java.lang.SecurityManager", false);
            importDefaultObject("java.lang.Short", true);
            importDefaultObject("java.lang.StackTraceElement", false);
            importDefaultObject("java.lang.StrictMath", false);
            importDefaultObject("java.lang.String", true);
            importDefaultObject("java.lang.StringBuffer", false);
            importDefaultObject("java.lang.System", false);
    //        importDefaultObject("java.lang.Thread", false);
    //        importDefaultObject("java.lang.ThreadGroup", false);
    //        importDefaultObject("java.lang.ThreadLocal", false);
            importDefaultObject("java.lang.Throwable", false);
            importDefaultObject("java.lang.Void", false);

            /**
             * Import enums from java.lang
             */
    //        importDefaultObject("java.lang.Thread.State", false);

            /**
             * Import exceptions from java.lang
             */
            importDefaultObject("java.lang.ArithmeticException", false);
            importDefaultObject("java.lang.ArrayIndexOutOfBoundsException", false);
            importDefaultObject("java.lang.ArrayStoreException", false);
            importDefaultObject("java.lang.ClassCastException", false);
            importDefaultObject("java.lang.ClassNotFoundException", false);
            importDefaultObject("java.lang.CloneNotSupportedException", false);
            importDefaultObject("java.lang.EnumConstantNotPresentException", false);
            importDefaultObject("java.lang.Exception", false);
            importDefaultObject("java.lang.IllegalAccessException", false);
            importDefaultObject("java.lang.IllegalArgumentException", false);
            importDefaultObject("java.lang.IllegalMonitorStateException", false);
            importDefaultObject("java.lang.IllegalStateException", false);
            importDefaultObject("java.lang.IndexOutOfBoundsException", false);
            importDefaultObject("java.lang.InstantiationException", false);
            importDefaultObject("java.lang.InterruptedException", false);
            importDefaultObject("java.lang.NegativeArraySizeException", false);
            importDefaultObject("java.lang.NoSuchFieldException", false);
            importDefaultObject("java.lang.NoSuchMethodException", false);
            importDefaultObject("java.lang.NullPointerException", false);
            importDefaultObject("java.lang.NumberFormatException", false);
            importDefaultObject("java.lang.RuntimeException", false);
            importDefaultObject("java.lang.SecurityException", false);
            importDefaultObject("java.lang.StringIndexOutOfBoundsException", false);
            importDefaultObject("java.lang.TypeNotPresentException", false);
            importDefaultObject("java.lang.UnsupportedOperationException", false);

            /**
             * Import errors from java.lang
             */
            importDefaultObject("java.lang.AbstractMethodError", false);
            importDefaultObject("java.lang.AssertionError", false);
            importDefaultObject("java.lang.ClassCircularityError", false);
            importDefaultObject("java.lang.ClassFormatError", false);
            importDefaultObject("java.lang.Error", false);
            importDefaultObject("java.lang.ExceptionInInitializerError", false);
            importDefaultObject("java.lang.IllegalAccessError", false);
            importDefaultObject("java.lang.IncompatibleClassChangeError", false);
            importDefaultObject("java.lang.InstantiationError", false);
            importDefaultObject("java.lang.InternalError", false);
            importDefaultObject("java.lang.LinkageError", false);
            importDefaultObject("java.lang.NoClassDefFoundError", false);
            importDefaultObject("java.lang.NoSuchFieldError", false);
            importDefaultObject("java.lang.NoSuchMethodError", false);
            importDefaultObject("java.lang.OutOfMemoryError", false);
            importDefaultObject("java.lang.StackOverflowError", false);
            importDefaultObject("java.lang.ThreadDeath", false);
            importDefaultObject("java.lang.UnknownError", false);
            importDefaultObject("java.lang.UnsatisfiedLinkError", false);
            importDefaultObject("java.lang.UnsupportedClassVersionError", false);
            importDefaultObject("java.lang.VerifyError", false);
            importDefaultObject("java.lang.VirtualMachineError", false);
        
            /**
             *  Make sure the actor is in the correct file
             */
            String module = cu.getModule();
            String name = cu.getName().toNonGenericName();

            if (module == null) module = "";
            else module += ".";

            setCurrentModule(module);

            String currentFile = ActorType.findSalsaFile(module + name);
            if (currentFile == null) {
                CompilerErrors.printErrorMessage("COMPILER ERROR [SymbolTable.resetSymbolTable]: SALSA behavior '" + module + name + "' is not in it's specfied package '" + module + "'", cu.getName());
                throw new RuntimeException();
            }

            /**
             * Load basic salsa_lite objects
             */
            ObjectType messageType = new ObjectType(runtimeModule + ".Message", SymbolTable.getTypeSymbol("Object"));
            FieldSymbol simpleMessageField = new FieldSymbol(messageType, "SIMPLE_MESSAGE", SymbolTable.getTypeSymbol("int"));
            messageType.fields.add(simpleMessageField);
            FieldSymbol argumentsField = new FieldSymbol(messageType, "arguments", SymbolTable.getTypeSymbol("Object[]"));
            messageType.fields.add(argumentsField);

            knownTypes.put( messageType.getLongSignature(), messageType );
            namespace.put( messageType.getName(), messageType.getLongSignature() );
            setImmutableObject(runtimeModule + ".Message");
            addVariableType("Message", "Message", false, true);


            ObjectType stageServiceType = new ObjectType(runtimeModule + ".StageService");
            MethodSymbol sendMessageMethod = new MethodSymbol(0, stageServiceType, "sendMessage", SymbolTable.getTypeSymbol("void"), new TypeSymbol[]{ SymbolTable.getTypeSymbol("Message") }, true);
            stageServiceType.method_handlers.add(sendMessageMethod);

            knownTypes.put( stageServiceType.getLongSignature(), stageServiceType );
            namespace.put( stageServiceType.getName(), stageServiceType.getLongSignature() );
            addVariableType("StageService", "StageService", false, true);


            ObjectType stageType = new ObjectType(runtimeModule + ".Stage");
            FieldSymbol messageField = new FieldSymbol(stageType, "message", SymbolTable.getTypeSymbol("Message"));
            stageType.fields.add(messageField);

            knownTypes.put( stageType.getLongSignature(), stageType );
            namespace.put( stageType.getName(), stageType.getLongSignature() );
            addVariableType("Stage", "Stage", false, true);


            ActorType localActorType = null;
            localActorType = new ActorType(runtimeModule + ".Actor", SymbolTable.getTypeSymbol("Object"));

            FieldSymbol stageField = new FieldSymbol(localActorType, "stage", SymbolTable.getTypeSymbol("Stage"));
            localActorType.fields.add(stageField);

            knownTypes.put(localActorType.getLongSignature(), localActorType);
            namespace.put(localActorType.getName(), localActorType.getLongSignature());
            addVariableType("stage", "Stage", false, true);     //should add an actor's parent fields to namespace as well instead of this hack

            ActorType remoteActorType = null;
            remoteActorType = new ActorType(runtimeModule + ".RemoteActor", SymbolTable.getTypeSymbol("Actor"));
            MessageSymbol getHostMessage = new MessageSymbol(0, "getHost", remoteActorType, SymbolTable.getTypeSymbol("String"), new TypeSymbol[]{});
            MessageSymbol getPortMessage = new MessageSymbol(1, "getPort", remoteActorType, SymbolTable.getTypeSymbol("int"), new TypeSymbol[]{});
            MessageSymbol getNameMessage = new MessageSymbol(2, "getName", remoteActorType, SymbolTable.getTypeSymbol("String"), new TypeSymbol[]{});
            remoteActorType.message_handlers.add(getHostMessage);
            remoteActorType.message_handlers.add(getPortMessage);
            remoteActorType.message_handlers.add(getNameMessage);
            knownTypes.put(remoteActorType.getLongSignature(), remoteActorType);
            namespace.put(remoteActorType.getName(), remoteActorType.getLongSignature());

            ActorType mobileActorType = null;
            mobileActorType = new ActorType(runtimeModule + ".MobileActor", SymbolTable.getTypeSymbol("Actor"));
            MessageSymbol migrateMessage = new MessageSymbol(0, "migrate", mobileActorType, SymbolTable.getTypeSymbol("ack"), new TypeSymbol[]{ SymbolTable.getTypeSymbol("String"), SymbolTable.getTypeSymbol("int") });
            mobileActorType.message_handlers.add(migrateMessage);
            knownTypes.put(mobileActorType.getLongSignature(), mobileActorType);
            namespace.put(mobileActorType.getName(), mobileActorType.getLongSignature());


            FieldSymbol targetField = null;
            targetField = new FieldSymbol(messageType, "target", SymbolTable.getTypeSymbol("Actor"));
            messageType.fields.add(targetField);

            if (!cu.getName().equals("ContinuationDirector")) {
                //On the chance we're compiling ContinuationDirector
                FieldSymbol continuationDirectorField = new FieldSymbol(messageType, "continuationDirector", SymbolTable.getTypeSymbol(runtimeModule + ".language.ContinuationDirector"));
                messageType.fields.add(continuationDirectorField);
            }

            openScope();
            String actorName = cu.getName().name;

            if (actorName.contains("<")) actorName = actorName.substring(0, actorName.indexOf("<"));
            ActorType currentActorType = new ActorType( getCurrentModule() + actorName );
            knownTypes.put(currentActorType.getLongSignature(), currentActorType);
            namespace.put(actorName, currentActorType.getLongSignature());
            cu.getImportDeclarationCode();                      //this will load the current actor's dependencies
            currentActorType.load(cu, getCurrentModule());

            if (cu.getName().equals("ContinuationDirector")) {
                //On the chance we're compiling ContinuationDirector
                FieldSymbol continuationDirectorField = new FieldSymbol(messageType, "continuationDirector", SymbolTable.getTypeSymbol(runtimeModule + ".language.ContinuationDirector"));
                messageType.fields.add(continuationDirectorField);
            }
            closeScope();

        } catch (SalsaNotFoundException snfe) {
            System.err.println("COMPILER ERROR [SymbolTable.resetSymbolTable]: " + snfe.toString());
            snfe.printStackTrace();
            System.exit(0);
        }

//        importActor(runtimeModule + ".ContinuationDirector");
//        importActor(runtimeModule + ".TokenDirector");
//        importActor(runtimeModule + ".ImplicitTokenDirector");
//        importActor(runtimeModule + ".JoinDirector");
//        importActor(runtimeModule + ".MessageDirector");
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

            ArrayList<FieldSymbol> fields = new ArrayList<FieldSymbol>(ot.fields);
            for (FieldSymbol fs : fields) System.out.println("\t" + fs.getLongSignature());

            ArrayList<ConstructorSymbol> constructors = ot.constructors;
            for (ConstructorSymbol cs : constructors) System.out.println("\t" + cs.getLongSignature());

            ArrayList<MethodSymbol> method_handlers = ot.method_handlers;
            for (MethodSymbol ms : method_handlers) System.out.println("\t" + ms.getLongSignature());
        }

        System.out.println();
        System.out.println("KNOWN ACTORS:");
        for (TypeSymbol ts : types) {
            if (!(ts instanceof ActorType)) continue;

            System.out.println(ts.getLongSignature());

            ActorType at = (ActorType)ts;

            ArrayList<FieldSymbol> fields = at.fields;
            for (FieldSymbol fs : fields) System.out.println("\t" + fs.getLongSignature());

            ArrayList<ConstructorSymbol> constructors = at.constructors;
            for (ConstructorSymbol cs : constructors) System.out.println("\t" + cs.getLongSignature());

            ArrayList<MessageSymbol> message_handlers = at.message_handlers;
            for (MessageSymbol ms : message_handlers) System.out.println("\t" + ms.getLongSignature());
        }


        System.out.println();
        System.out.println("KNOWN ARRAYS:");
        for (TypeSymbol ts : types) {
            if (!(ts instanceof ArrayType)) continue;

            System.out.println(ts.getLongSignature());

            ArrayType at = (ArrayType)ts;

            ArrayList<FieldSymbol> fields = at.fields;
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
