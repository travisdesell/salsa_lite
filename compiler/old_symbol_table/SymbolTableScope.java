package salsa_lite.compiler.symbol_table;

import java.util.Hashtable;


public class SymbolTableScope {

	boolean firstContinuation = true;

	boolean inJoinDirector = false;
	int joinDirectors = 0;
	int depth;
	SymbolTableScope parent = null;
	Hashtable<String,SymbolType> variableTypes = new Hashtable<String,SymbolType>();

	public boolean firstContinuation() {
		if (!firstContinuation) return false;
		else {
			if (parent == null) return true;
			else return parent.firstContinuation();
		}
	}

	public boolean inJoinDirector() {
		if (inJoinDirector) return true;
		else {
			if (parent == null) return false;
			else return parent.inJoinDirector();
		}
	}

	public String getJoinDirector() {
		if (inJoinDirector) return "join_director_" + depth + "_" + joinDirectors;
		else {
			if (parent == null) return null;
			else return parent.getJoinDirector();
		}
	}

	public SymbolType getVariableType(String variableName) {
		SymbolType result = variableTypes.get(variableName);
		if (result == null && parent != null) {
			return parent.getVariableType(variableName);
		}
		return result;
	}

	public void addVariableType(String variableName, SymbolType variableType) {
		variableTypes.put(variableName, variableType);
	}

}
