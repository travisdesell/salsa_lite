package salsa_lite.compiler.symbol_table;


public class SymbolArrayType extends SymbolType {

	public boolean isActor() { return type.isActor(); }

	SymbolType type;
	int dimensions;

	public SymbolArrayType(int dimensions, SymbolType type) {
		this.type = type;
		this.dimensions = dimensions;
		addMessageHandler(new SymbolMessage(SymbolTable.getSymbolType("int"), this, "length"));
	}

	public String getName() { 
		String name = type.getName();
		for (int i = 0; i < dimensions; i++) name += "[]";
		return name;
	}

	public String getSignature() {
		String signature = type.getSignature();
		for (int i = 0; i < dimensions; i++) signature += "[]";
		return signature;
	}

	public String getLongSignature() {
		String signature = type.getLongSignature();
		for (int i = 0; i < dimensions; i++) signature += "[]";
		return signature;
	}
}
