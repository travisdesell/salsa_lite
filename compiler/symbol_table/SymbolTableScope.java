package salsa_lite.compiler.symbol_table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class SymbolTableScope {

	boolean firstContinuation = true;

	boolean inJoinDirector = false;
	int joinDirectors = 0;
	int depth;
	SymbolTableScope parent = null;
	HashMap<String,VariableTypeSymbol> variableTypes = new HashMap<String,VariableTypeSymbol>();

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

    public VariableTypeSymbol getVariableType(String variableName, boolean recursive) {
		VariableTypeSymbol result = variableTypes.get(variableName);
		if (recursive && result == null && parent != null) {
			return parent.getVariableType(variableName, recursive);
		}
		return result;
    }

    public VariableTypeSymbol getVariableType(String variableName) {
        return getVariableType(variableName, true);
	}

	public void addVariableType(String variableName, VariableTypeSymbol variableType) {
		variableTypes.put(variableName, variableType);
	}

    public void printScope(boolean recursive) {
        printScope(recursive, "");
    }

    public void printScope(boolean recursive, String depth) {
        ArrayList<String> names = new ArrayList<String>(variableTypes.keySet());
        Collections.sort(names);

        for (String name : names) {
            System.err.println(depth + name + " -- " + variableTypes.get(name).getType().getLongSignature());
        }

        if (recursive && parent != null) parent.printScope(recursive, depth + "\t");
    }
}
