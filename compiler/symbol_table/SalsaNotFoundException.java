package salsa_lite.compiler.symbol_table;


public class SalsaNotFoundException extends Exception {
    String module, name, type;

    public SalsaNotFoundException(String module, String name, String type) {
        this.module = module;
        this.name = name;
        this.type = type;
    }

    public String toString() {
        return "SalsaNotFoundException: " + type + " could not be found, package: '" + module + "', name: '" + name + "'";
    }
}
