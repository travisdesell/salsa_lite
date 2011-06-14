package salsa_lite.compiler.symbol_table;

import java.util.Vector;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;

import salsa_lite.compiler.definitions.CConstructor;
import salsa_lite.compiler.definitions.CExpression;
import salsa_lite.compiler.definitions.CMessageHandler;


public class SymbolMessage {
	boolean is_constructor = false;

	int id;
	String name;
	public Vector<SymbolType> arguments = new Vector<SymbolType>();
	SymbolType pass_type;
	SymbolType source_type;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getName() { return name; }
	public SymbolType getPassType() { return pass_type; }
	public SymbolType getSourceType() { return source_type; }
	public boolean isConstructor() { return is_constructor; }

	public boolean matches(Vector<SymbolType> arguments) {
		if (!is_constructor) return false;

		if (this.arguments.size() != arguments.size()) return false;

		for (int i = 0; i < arguments.size(); i++) {
			SymbolType arg1 = this.arguments.get(i);
			SymbolType arg2 = arguments.get(i);
			
			if (arg2.getName().equals("null")) {
				if (arg1.isPrimitive()) return false;
				else continue;
			}

			if (!arg1.getLongSignature().equals(arg2.getLongSignature())) return false;
		}
		return true;
	}

	public boolean matches(String messageName, Vector<SymbolType> arguments) {
		if (is_constructor) return false;

		if (messageName == null) {
			System.err.println("Error in match: passed messageName is null");
			System.err.println("Comparing to: " + getLongSignature());
			System.err.println("arguments are:");
			for (int i = 0; i < arguments.size(); i++) {
				System.err.println("\t" + arguments.get(i).getLongSignature());
			}
			System.err.println(".");
		}

		if (!messageName.equals(this.name)) return false;
		if (this.arguments.size() != arguments.size()) return false;

		for (int i = 0; i < arguments.size(); i++) {
			SymbolType arg1 = this.arguments.get(i);
			SymbolType arg2 = arguments.get(i);

			if (arg2.getName().equals("null")) {
				if (arg1.isPrimitive()) return false;
				else continue;
			}
			
			if (!arg1.getLongSignature().equals(arg2.getLongSignature())) return false;
		}

		return true;
	}

	public SymbolMessage(SymbolType pass_type, SymbolType source_type, String name) {
		this.pass_type = pass_type;
		this.name = name;
		this.id = -1;
		this.source_type = source_type;
	}


	public SymbolMessage(int id, SymbolType source_type, Constructor constructor) {
		this.id = id;
		this.source_type = source_type;
		this.is_constructor = true;
		this.name = null;
		this.pass_type = source_type;

		Class[] parameterTypes = constructor.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			arguments.add( SymbolTable.getSymbolType(parameterTypes[i].getCanonicalName()) );
		}
	}

	public SymbolMessage(int id, SymbolType source_type, CConstructor constructor) {
		this.id = id;
		this.source_type = source_type;
		this.is_constructor = true;
		this.name = null;
		this.pass_type = source_type;

		String[] argumentTypes = constructor.getArgumentTypes();
		for (int i = 0; i < argumentTypes.length; i++) {
			arguments.add( SymbolTable.getSymbolType(argumentTypes[i]) );
		}
	}

	public SymbolMessage(int id, SymbolType source_type, Method method) {
		this.id = id;
		this.source_type = source_type;
		this.name = method.getName();
		this.pass_type = SymbolTable.getSymbolType(method.getReturnType().getCanonicalName());

		Class[] parameterTypes = method.getParameterTypes();
		for (int i = 0; i < parameterTypes.length; i++) {
			arguments.add( SymbolTable.getSymbolType(parameterTypes[i].getCanonicalName()) );
		}
	}

	public SymbolMessage(int id, SymbolType source_type, CMessageHandler messageHandler) {
		this.id = id;
		this.source_type = source_type;
		this.name = messageHandler.name;
		this.pass_type = SymbolTable.getSymbolType(messageHandler.pass_type);

		String[] argumentTypes = messageHandler.getArgumentTypes();
		for (int i = 0; i < argumentTypes.length; i++) {
			arguments.add( SymbolTable.getSymbolType(argumentTypes[i]) );
		}
	}

	public String getSignature() {
		String signature;
		if (is_constructor) {
			signature = id + ":" + pass_type.getSignature() + " " + source_type.getSignature() + "("; 
		} else {
			signature = id + ":" + pass_type.getSignature() + " " + source_type.getSignature() + "." + name + "(";
		}
		for (int i = 0; i < arguments.size(); i++) {
			signature += " " + arguments.get(i).getSignature();
			
		}
		signature += " )";
		return signature;
	}

	public String getLongSignature() {
		String signature;
		if (is_constructor) {
			signature = id + ":" + pass_type.getLongSignature() + " " + source_type.getLongSignature() + "("; 
		} else {
			signature = id + ":" + pass_type.getLongSignature() + " " + source_type.getLongSignature() + "." + name + "(";
		}
		for (int i = 0; i < arguments.size(); i++) {
			signature += " " + arguments.get(i).getLongSignature();
			
		}
		signature += " )";
		return signature;
	}
}
