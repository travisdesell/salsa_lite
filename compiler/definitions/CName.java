package salsa_lite.compiler.definitions;

import java.util.ArrayList;


public class CName extends CErrorInformation {
    public String name;
    public ArrayList<CGenericType> generic_types = new ArrayList<CGenericType>();

    public CName() {
    }

    public CName(String name) {
        this.name = name;
    }


    public String toNonGenericName() {
        if (name.contains("<")) return name.substring(0, name.lastIndexOf('<'));
        else return name;
    }
}
