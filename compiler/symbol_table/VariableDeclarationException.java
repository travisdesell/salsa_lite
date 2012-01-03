package salsa_lite.compiler.symbol_table;

public class VariableDeclarationException extends Exception {
    String name;
    String type;

    public VariableDeclarationException(String description, Exception e) {
        super(description, e);
    }

    public VariableDeclarationException(String name, String type, String description) {
        super(description);
        this.name = name;
        this.type = type;
    }

    public String toString() {
        if (name != null) {
            return "VariableDeclarationException: " + name + " could not be declared, type: '" + type + ", description: " + getMessage();
        } else {
            return super.toString();
        }
    }
}
