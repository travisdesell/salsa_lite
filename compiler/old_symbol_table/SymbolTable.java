package salsa_lite.compiler.symbol_table;

import java.lang.reflect.Modifier;
import java.lang.reflect.Method;

import java.util.Vector;
import java.util.Hashtable;
import java.util.StringTokenizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.ParseException;

import salsa_lite.compiler.definitions.CConstructor;
import salsa_lite.compiler.definitions.CCompilationUnit;
import salsa_lite.compiler.definitions.CExpression;

public class SymbolTable {

	public static boolean continuesToPass = false;

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


	public static String currentImportName;
	public static SymbolType currentImportSymbol;

	public static Vector<String> bufferedNames;
	public static Hashtable<String, SymbolType> bufferedTypes;

	public static Hashtable<String, SymbolType> knownTypes;

	static {
		resetSymbolTable();
	}

	public static void resetSymbolTable() {
		currentModule = "";
		runtimeModule = "salsa_lite.";
		if (System.getProperty("local") != null) runtimeModule += "local";
		else if (System.getProperty("local_noref") != null) runtimeModule += "local_noref";
		else if (System.getProperty("local_fcs") != null) runtimeModule += "local_fcs";
		else if (System.getProperty("wwc") != null) runtimeModule += "wwc";
		languageModule = runtimeModule + ".language.";

		base_scope = new SymbolTableScope();
		scope = base_scope;

		currentImportName = "";
		currentImportSymbol = null;

		knownTypes = new Hashtable<String, SymbolType>();
		bufferedTypes = new Hashtable<String, SymbolType>();
		bufferedNames = new Vector<String>();

		knownTypes.put("ack", new SymbolType("ack"));
		knownTypes.put("void", new SymbolType("void"));
		knownTypes.put("boolean", new SymbolType("boolean"));
		knownTypes.put("char", new SymbolType("char"));
		knownTypes.put("byte", new SymbolType("byte"));
		knownTypes.put("short", new SymbolType("short"));
		knownTypes.put("int", new SymbolType("int"));
		knownTypes.put("long", new SymbolType("long"));
		knownTypes.put("float", new SymbolType("float"));
		knownTypes.put("double", new SymbolType("double"));
		knownTypes.put("null", new SymbolType("null"));

		SymbolType stringType = new SymbolType("String");
		knownTypes.put("String", stringType);
		knownTypes.put("java.lang.String", stringType);

		if (System.getProperty("local") != null) {
			knownTypes.put("LocalActor", new SymbolType("LocalActor", languageModule));
		} else if (System.getProperty("local_noref") != null) {
			knownTypes.put("LocalActor", new SymbolType("LocalActor", languageModule));
		} else if (System.getProperty("local_fcs") != null) {
			knownTypes.put("LocalActor", new SymbolType("LocalActor", languageModule));
		} else if (System.getProperty("wwc") != null) {
			knownTypes.put("WWCActor", new SymbolType("WWCActor", languageModule));
		}
	}

	public static boolean isActor(String type) {
		SymbolType symbolType = knownTypes.get(type);
		if (symbolType != null) return symbolType.isActor();

		System.err.println("SymbolTable error: Lookup of '" + type + "' failed in SymbolTable.isActor");
		return false;
	}

	public static boolean isObject(String type) {
		SymbolType symbolType = knownTypes.get(type);
		if (symbolType != null) return symbolType.isObject();

		System.err.println("SymbolTable error: Lookup of '" + type + "' failed in SymbolTabel.isObject");
		return false;
	}

    public static boolean isEnum(String type) {
		SymbolType symbolType = knownTypes.get(type);
		if (symbolType != null) return symbolType.isEnum();

		System.err.println("SymbolTable error: Lookup of '" + type + "' failed in SymbolTabel.isObject");
		return false;
    }

    public static boolean isMutableObject(String type) {
        SymbolType symbolType = knownTypes.get(type);

        if (symbolType == null) {
            System.err.println("SymbolTable error: Lookup of '" + type + "' failed in SymbolTabel.isMutableObject");
            return false;
        }
        
        if (symbolType.isActor()) return false;
        if (symbolType.isPrimitive()) return false;

        String name = symbolType.getName();

        if (name.equals("Message") ||
            name.equals("String") ||
            name.equals("java.lang.String") ||
            name.equals("Boolean") ||
            name.equals("java.lang.Boolean") ||
            name.equals("Short") ||
            name.equals("java.lang.Short") ||
            name.equals("Float") ||
            name.equals("java.lang.Float") ||
            name.equals("Double") ||
            name.equals("java.lang.Double") ||
            name.equals("Character") ||
            name.equals("java.lang.Character") ||
            name.equals("Integer") ||
            name.equals("java.lang.Integer") ||
            name.equals("Long") ||
            name.equals("java.lang.Long") ||
            name.equals("Byte") ||
            name.equals("java.lang.Byte")) return false;

       return true;
    }

	public static String currentMessageName = "";

	public static SymbolType getSymbolType(String type) {
		int array_dimensions = getArrayDimensions(type);
		type = stripArrayDimensions(type);

		SymbolType symbolType = knownTypes.get(type);
		if (symbolType == null) {
			if (currentImportName.equals(type)) {
				symbolType = currentImportSymbol;
			} else if (bufferedNames.contains(type)) {
				symbolType = bufferedTypes.get(type);
			} else {
//				System.err.println("Attempting to buffer type: " + type);
				symbolType = new SymbolType(type);
				if (type.lastIndexOf(".") < 0) {
					System.err.println("setting module for symbolType: " + symbolType.getLongSignature() + " to " + SymbolTable.currentImportModule);
					symbolType.module = SymbolTable.currentImportModule;
				}

				bufferedNames.add(type);
				bufferedTypes.put(type, symbolType);
//				System.err.println("Adding " + type + " to type buffer");
				if (currentImportName.equals("")) importBufferedTypes();
			}
		}

		if (symbolType == null) {
			System.err.println("SymbolTable error: Lookup of '" + type + "' failed in SymbolTable.getSymbolType -- this should not be possible");
			return null;
		}

		if (array_dimensions > 0) {
			symbolType = new SymbolArrayType(array_dimensions, symbolType);
		}
		return symbolType;
	}

    public static SymbolType importTypeAsEnum(String module, String name) {
        SymbolType symbolType = importType(module, name);
        symbolType.is_enum = true;
        symbolType.is_actor = false;
        return symbolType;
    }

    public static SymbolType importTypeAsObject(String module, String name) {
        SymbolType symbolType = importType(module, name);
        symbolType.is_object = true;
        symbolType.is_actor = false;
        return symbolType;
    }

	public static SymbolType importType(String module, String name) {
		if (name.equals(currentImportName)) return currentImportSymbol;

		System.err.println("IMPORTING TYPE: " + module + " " + name);
		System.err.println("SETTING CURRENT IMPORT MODULE TO " + module + " FROM " + SymbolTable.currentImportModule);
		SymbolTable.currentImportModule = module;

		SymbolType symbolType = knownTypes.get(name);
		SymbolType fullSymbolType = knownTypes.get(module + name);
		if (symbolType != null) {
			if (fullSymbolType == null) {
	 			System.err.println("SymbolTable error: Import of " + module + name + " conflicted with import of " + symbolType.getLongSignature());
			}
			return symbolType;
		}
		symbolType = bufferedTypes.get(name);
		fullSymbolType = bufferedTypes.get(module + name);
		if (symbolType != null) {
			if (fullSymbolType == null) {
				System.err.println("SymbolTable error: import of " + module + name + " conflicted with buffered import of " + symbolType.getLongSignature());
			}
			return symbolType;
		}

		currentImportName = name;
		symbolType = new SymbolType(module, name);
		currentImportSymbol = symbolType;

		symbolType.loadSymbolType();

//		System.err.println("ADDING KNOWN TYPE: " + name + " -- " + symbolType.getLongSignature());
//		System.err.println("ADDING KNOWN TYPE: " + module + name + " -- " + symbolType.getLongSignature());
		knownTypes.put(module + name, symbolType);
		knownTypes.put(name, symbolType);

		System.err.println("RESETTING CURRENT IMPORT MODULE TO \"\"");
		SymbolTable.currentImportModule = "";
		currentImportName = "";
		currentImportSymbol = null;
		importBufferedTypes();
		return symbolType;
	}

    public static SymbolType importTypeAsEnum(String name) {
        SymbolType symbolType = importType(name);
        symbolType.is_enum = true;
        return symbolType;
    }

    public static SymbolType importTypeAsObject(String name) {
        SymbolType symbolType = importType(name);
        symbolType.is_object = true;
        return symbolType;
    }

	public static SymbolType importType(String name) {
		if (name.equals(currentImportName)) return currentImportSymbol;

		System.err.println("IMPORTING TYPE: " + name);

		SymbolType symbolType = knownTypes.get(name);
		if (symbolType != null) return symbolType;
		symbolType = bufferedTypes.get(name);
		if (symbolType != null) return symbolType;

		currentImportName = name;
		symbolType = new SymbolType(name);
		currentImportSymbol = symbolType;

		symbolType.loadSymbolType();

//		System.err.println("ADDING KNOWN TYPE: " + name + " -- " + symbolType.getLongSignature());
		knownTypes.put(name, symbolType);

		currentImportName = "";
		currentImportSymbol = null;
		importBufferedTypes();
		return symbolType;
	}

	public static void importBufferedTypes() {
		while (bufferedNames.size() > 0) {
			String name = bufferedNames.remove(0);
			SymbolType bufferedType = bufferedTypes.get(name);

			currentImportName = name;
			currentImportSymbol = bufferedType;

			bufferedType.loadSymbolType();

//			System.err.println("ADDING KNOWN TYPE: " + name + " -- " + bufferedType.getLongSignature());
			knownTypes.put(name, bufferedType);
		}
		currentImportName = "";
		currentImportSymbol = null;
	}

	public static String getConstructorErrorMessage(String type, String[] argumentTypes) {
		String error_message = "SymbolTable error: Lookup of constructor '" + type + "(";
		for (int i = 0; i < argumentTypes.length; i++) error_message += " " + argumentTypes[i];
		error_message += " )' failed in SymbolTable.getConstructor -- ";
		return error_message;
	}

	public static SymbolMessage getConstructor(String type, Vector<CExpression> arguments) {
		int i;
		SymbolType symbolType = getSymbolType(type);

		String[] argumentTypes = CExpression.getArgumentTypes(arguments);
		if (symbolType == null) {
			System.err.println(getConstructorErrorMessage(type, argumentTypes) + "lookup of target type '" + type + "' failed");
			return null;
		}

		Vector<SymbolType> argumentSymbolTypes = new Vector<SymbolType>();
		for (i = 0; i < argumentTypes.length; i++) {
			SymbolType argumentType = getSymbolType(argumentTypes[i]);

			if (argumentType == null) {
				System.err.println(getConstructorErrorMessage(type, argumentTypes) + "lookup of argument[" + i + "] '" + argumentTypes[i] + "' failed");
				return null;
			}
			argumentSymbolTypes.add(argumentType);
		}

		int matches = 0;
		SymbolMessage match = null; 
		for (i = 0; i < symbolType.getConstructors().size(); i++) {
			SymbolMessage constructor = symbolType.getConstructors().get(i);
			if (constructor.matches(argumentSymbolTypes)) {
				matches++;
				if (matches == 2) {
					System.err.println(getConstructorErrorMessage(type, argumentTypes) + "multiple matching constructors:");
					System.err.println("\t" + match.getLongSignature());
				}
				match = constructor;
				if (matches > 1) {
					System.err.println("\t" + match.getLongSignature());
				}
			}
		}

		if (matches == 0) {
			System.err.println(getConstructorErrorMessage(type, argumentTypes) + "could not find matching method");
			System.err.println( "known constructors:");
			for (i = 0; i < symbolType.getConstructors().size(); i++) {
				System.err.println( "\t" + symbolType.getConstructors().get(i).getLongSignature() );
			}
			System.err.println( "." );
			return null;
		}
		return match;
	}

	public static int getConstructorId(String type, Vector<CExpression> arguments) {
		SymbolMessage message = getConstructor(type, arguments);
		if (message == null) return -1;
		return message.getId();
	}

	public static String getMessageErrorMessage(String type, String messageName, String[] argumentTypes) {
		String error_message = "SymbolTable error: Lookup of message " + type + "<-" + messageName + "(";
		for (int i = 0; i < argumentTypes.length; i++) error_message += " " + argumentTypes[i];
		error_message += " ) failed in SymbolTable.getMessage -- ";

		return error_message;
	}

	public static SymbolMessage getMessage(String type, String messageName, Vector<CExpression> arguments) {
		int i;
		SymbolType symbolType = getSymbolType(type);

		String[] argumentTypes = CExpression.getArgumentTypes(arguments);
		if (symbolType == null) {
			System.err.println( getMessageErrorMessage(type, messageName, argumentTypes) + "Lookup of target type '" + type + "' failed");
			return null;
		}

		Vector<SymbolType> argumentSymbolTypes = new Vector<SymbolType>();
		for (i = 0; i < argumentTypes.length; i++) {
			SymbolType argumentType = getSymbolType(argumentTypes[i]);

			if (argumentType == null) {
				System.err.println( getMessageErrorMessage(type, messageName, argumentTypes) + "lookup of argument[" + i + "] '" + argumentTypes[i] + "' failed" );
				return null;
			}
			argumentSymbolTypes.add(argumentType);
		}

		int matches = 0;
		SymbolMessage match = null;
		for (i = 0; i < symbolType.getMessageHandlers().size(); i++) {
			SymbolMessage messageHandler = symbolType.getMessageHandlers().get(i);
			if (messageHandler.matches(messageName, argumentSymbolTypes)) {
				matches++;
				if (matches == 2) {
					System.err.println( getMessageErrorMessage(type, messageName, argumentTypes) + "multiple matching message handlers:");
					System.err.println("\t" + match.getLongSignature());
				}
				match = messageHandler;
				if (matches > 1) {
					System.err.println("\t" + match.getLongSignature());
				}
			}
		}
		if (matches == 0) {
			System.err.println( getMessageErrorMessage(type, messageName, argumentTypes) + "could not find matching method");
			System.err.println( "known message handlers:");
			for (i = 0; i < symbolType.getMessageHandlers().size(); i++) {
				System.err.println( "\t" + symbolType.getMessageHandlers().get(i).getLongSignature() );
			}
			System.err.println( "." );
			return null;
		}
		return match;
	}

	public static int getMessageId(String type, String messageName, Vector<CExpression> arguments) {
		SymbolMessage message = getMessage(type, messageName, arguments);
		return message.getId();
	}

	public static String getMessageResultType(String type, String messageName, Vector<CExpression> arguments) {
		SymbolMessage message = getMessage(type, messageName, arguments);
		return message.getPassType().getName();
	}

    public static String getMethodInvocationResultType(String type, String messageName, Vector<CExpression> arguments) {
		SymbolMessage message = getMessage(type, messageName, arguments);
		return message.getPassType().getName();
    }

	public static String getFieldType(String type, String fieldName) {
		SymbolType symbolType = getSymbolType(type);

		if (symbolType == null) {
			System.err.println("SymbolTable error: Lookup of field '" + fieldName + "' on '" + type + "' failed in SymbolTable.getFieldType -- could not find " + type);

            System.err.println("known fields:");
            for (int i = 0; i < symbolType.getFields().size(); i++) {
                System.err.println("\t" + symbolType.getFields().get(i));
            }

			return null;
		}

		for (int i = 0; i < symbolType.getFields().size(); i++) {
			SymbolVariable field = symbolType.getFields().get(i);
			if (field.getName().equals(fieldName)) return field.getType().getModule() + field.getType().getName();
		}

		System.err.println("SymbolTable error: Lookup of field '" + fieldName + "' on '" + type + "' failed in SymbolTable.getFieldType -- could not find field");
        System.err.println("known fields:");
        for (int i = 0; i < symbolType.getFields().size(); i++) {
            System.err.println("\t" + symbolType.getFields().get(i));
        }

		return null;
	}

	public static void addVariableType(String name, String type) {
		SymbolType symbolType = getSymbolType(type);

		if (symbolType != null) {
			scope.addVariableType(name, symbolType);
			return;
		}

		System.err.println("SymbolTable error: Lookup of type '" + type + "' for variable '" + name + "' failed in SymbolTable.addVariableType -- could not find type");
	}

	public static String getVariableType(String name) {
		SymbolType symbolType = scope.getVariableType(name);

		if (name.equals("null")) {
			System.err.println("getting variable type: " + name + " got symbolType: " + symbolType.getLongSignature());
		}

		if (symbolType != null) return symbolType.getName();

		symbolType = getSymbolType(name);
		if (symbolType != null) return symbolType.getName();

		System.err.println("SymbolTable error: Lookup of variable " + name + " failed in SymbolTable.getVariableType -- could not find type");
		return null;
	}


	static String continuationType;

	public static void setContinuationType(String continuationType) {
		SymbolTable.continuationType = continuationType;
	}
	public static String getContinuationType() {
		return continuationType;
	}

	public static int getArrayDimensions(String type) {
		int dimensions = 0;
		int position = type.length() - 1;
		while (position > 0 && type.charAt(position) == ']') {
			position -= 2;
			dimensions++;
		}
		return dimensions;
	}

	public static String stripArrayDimensions(String type) {
		String basetype = type;

		while (isArray(basetype)) basetype = basetype.substring(0, basetype.length() - 2);

		return basetype;
	}

	public static boolean isArray(String type) {
		return type.length() > 2 && type.charAt(type.length()-2) == '[' && type.charAt(type.length()-1) == ']';
	}

	public static boolean isToken(String name) {
		return false;
	}

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

	public static String getDominatingType(String type1, String type2) {
		if (type1.equals("String") || type2.equals("String")) return "String";
		else if (type1.equals("double") || type2.equals("double")) return "double";
		else if (type1.equals("float") || type2.equals("float")) return "float";
		else if (type1.equals("short") || type2.equals("short")) return "short";
		else if (type1.equals("long") || type2.equals("long")) return "long";
		else if (type1.equals("int") || type2.equals("int")) return "int";
		else return type1;
	}
	
	public static String toObject(String type) {
		if (type.equals("short")) return "Short";
		else if (type.equals("int")) return "Integer";
		else if (type.equals("long")) return "Long";
		else if (type.equals("float")) return "Float";
		else if (type.equals("double")) return "Double";
		else if (type.equals("boolean")) return "Boolean";
		else if (type.equals("char")) return "Character";
		else if (type.equals("byte")) return "Byte";
		else return type;
	}

	public static boolean isPrimitive(String type) {
		SymbolType symbolType = getSymbolType(type);

		if (symbolType != null) return symbolType.isPrimitive();

		System.err.println("Compiler Error: could not determine if " + type + " was primitive -- could not load type");
		return false;
	}
}
