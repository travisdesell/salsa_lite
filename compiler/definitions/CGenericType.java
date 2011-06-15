package salsa_lite.compiler.definitions;


public class CGenericType extends CErrorInformation {
    public String name;
    public String extends_type;

    public CGenericType(String name) {
        this.name = name;
    }

    public CGenericType(String name, String extends_type) {
        this.name = name;
        this.extends_type = extends_type;
    }

    public String toString() {
        if (extends_type != null) return name + " extends " + extends_type;
        else return name;
    }
}
