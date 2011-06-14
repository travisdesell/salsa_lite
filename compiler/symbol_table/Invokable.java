package salsa_lite.compiler.symbol_table;

import java.util.Vector;
import java.util.LinkedList;
import java.util.LinkedHashMap;

import salsa_lite.compiler.definitions.CExpression;

public class Invokable {

    int id;
    String name = "";
    TypeSymbol enclosingType;
    public TypeSymbol[] parameterTypes;

    public int getId()                      { return id; }
    public String getName()                 { return name; }
    public TypeSymbol getEnclosingType()    { return enclosingType; }
    public TypeSymbol[] getParameterTypes() { return parameterTypes; }

    public Invokable(TypeSymbol enclosingType, Vector<CExpression> parameterVector) {
        this.enclosingType = enclosingType;
        this.parameterTypes = Invokable.getParameterTypesFromExpressions(parameterVector);
    }

    public Invokable(String name, TypeSymbol enclosingType, Vector<CExpression> parameterVector) {
        this.enclosingType = enclosingType;
        this.parameterTypes = Invokable.getParameterTypesFromExpressions(parameterVector);
        this.name = name;
    }

    public Invokable(int id, TypeSymbol enclosingType) {
        this.id = id;
        this.enclosingType = enclosingType;
    }

    public Invokable(int id, String name, TypeSymbol enclosingType) {
        this.id = id;
        this.enclosingType = enclosingType;
        this.name = name;
    }

    public static TypeSymbol[] getParameterTypesFromExpressions(Vector<CExpression> expressions) {
        TypeSymbol[] expressionTypes = new TypeSymbol[expressions.size()];
        for (int i = 0; i < expressionTypes.length; i++) {
            expressionTypes[i] = expressions.get(i).getType();
        }
        return expressionTypes;
    }

    public String getSignature() {
        String signature;
        if (name.equals("")) {
            signature = enclosingType.getLongSignature() + "(";
        } else {
            signature = enclosingType.getLongSignature() + "." + name + "(";
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) signature += ", ";
            signature += parameterTypes[i].getSignature();
        }

        return signature + ")";
    }

    public String getLongSignature() {
        String signature;
        if (name.equals("")) {
            signature = enclosingType.getLongSignature() + "(";
        } else {
            signature = "[" + enclosingType.getLongSignature() + "]." + name + "(";
        }

        for (int i = 0; i < parameterTypes.length; i++) {
            if (i > 0) signature += ", ";
            signature += parameterTypes[i].getLongSignature();
        }

        return signature + ")";
    }

    public static Invokable matchInvokable(Invokable targetArguments, LinkedHashMap<String, ? extends Invokable> invokableMap) throws SalsaNotFoundException {
        Invokable match = null;
        int matches = 0;
        for (Invokable current : new LinkedList<Invokable>(invokableMap.values())) {
            if (targetArguments.matches(current)) {
                matches++;

                if (matches == 2) {
                    System.err.println("COMPILER WARNING [Invokable.matchParameterTypes]: matching targetArguments on '" + targetArguments.getLongSignature() + "' had multiple matches:");
                    System.err.println("\t" + match.getLongSignature());
                }
                match = current;
                if (matches > 1) {
                    System.err.println("\t" + match.getLongSignature());
                }
            }
        }

        if (match == null) {
            System.err.println("COMPILER ERROR [Invokable.matchParameterTypes]: could not find matching targetArguments for '" + targetArguments.getLongSignature() + "'.");
            System.err.println("known potential invokables:");
            for (Invokable current : new LinkedList<Invokable>(invokableMap.values())) {
                System.err.println("\t" + current.getLongSignature());
            }
        }

        return match;
    }

    /**
     *  The current invokable is has the arguments we're trying to send to target
     */
    public boolean matches(Invokable target) throws SalsaNotFoundException {
        if (!name.equals(target.getName())) return false;
        if (parameterTypes.length != target.parameterTypes.length) return false;

        for (int i = 0; i < parameterTypes.length; i++) {
            boolean canMatch = parameterTypes[i].canMatch( target.parameterTypes[i] );
//            System.err.println("\t[" + parameterTypes[i].getLongSignature() + "] vs [" + target.parameterTypes[i].getLongSignature() + "] -- " + canMatch);

            if (!canMatch) return false;
        }

        return true;
    }
}
