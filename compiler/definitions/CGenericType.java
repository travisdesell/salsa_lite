package salsa_lite.compiler.definitions;


public class CGenericType extends CErrorInformation {
    public CName name, bound;
    public String modifier;

    public CGenericType(CName name) {
        this.name = name;
    }

    public CGenericType(CName name, String modifier, CName bound) {
        this.name = name;
        this.modifier = modifier;
        this.bound = bound;
    }

    public String toString() {
        if (modifier != null) return name.name + " " + modifier + " " + bound.name;
        else return name.name;
    }
}
