package salsa_lite.compiler.definitions;


public class CType extends CErrorInformation {
    public String name;
    public boolean is_token = false;

    public CType (String name) {
        this.name = name;
    }
}
