package salsa_lite.compiler.symbol_table;


public class SymbolVariable {
	String name;
	SymbolType type;

	public String getName() { return name; }
	public SymbolType getType() { return type; }

	public SymbolVariable(SymbolType type, String name) {
		this.type = type;
		this.name = name;
	}

	public String getSignature() {
		return type.getSignature() + "." + name;
	}

	public String getLongSignature() {
		return type.getLongSignature() + "." + name;
	}
}
