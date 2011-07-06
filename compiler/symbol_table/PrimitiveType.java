package salsa_lite.compiler.symbol_table;

import java.util.ArrayList;

public class PrimitiveType extends TypeSymbol {

    public PrimitiveType(String name) {
        this.name = name;
        this.module = "";
    }

    public TypeSymbol copy() {
        //primitive are immutable
        return this;
    }

    public TypeSymbol replaceGenerics(String genericTypesString) {
        //primitives are immutable and can't be generic
        System.err.println("Error: trying to replace generics on a primitive type, this should never happen.");
        throw new RuntimeException();
    }
}
