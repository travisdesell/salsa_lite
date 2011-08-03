package salsa_lite.compiler.symbol_table;

import java.util.ArrayList;

public class Symbol {

    public static TypeSymbol getGenericReplacement(String name, ArrayList<String> declaredGenericTypes, ArrayList<TypeSymbol> instantiatedGenericTypes) {
        TypeSymbol replacementType = null;

        for (int i = 0; i < declaredGenericTypes.size(); i++) {
            String compareType = declaredGenericTypes.get(i);

            compareType = TypeSymbol.removeBounds(compareType);

            if (compareType.equals( name )) {
                replacementType = instantiatedGenericTypes.get(i);
            }
        }

        return replacementType;
    }
}
