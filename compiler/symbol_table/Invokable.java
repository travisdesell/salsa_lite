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
        int[] match_distance = new int[invokableMap.values().size()];

        int min_distance = Integer.MAX_VALUE;
        int i = 0;
        for (Invokable current : new LinkedList<Invokable>(invokableMap.values())) {
            match_distance[i] = targetArguments.matches(current);

            if (match_distance[i] >= 0 && match_distance[i] < min_distance) {
                matches = 1;
                min_distance = match_distance[i];

                match = current;
            } else if (match_distance[i] >= 0 && match_distance[i] == min_distance) {
                matches++;
            }
            i++;
        }

        if (matches > 1) {
            int min_match = match_distance[1];
            int min_match_position = 1;
            int min_matches = 0;
            for (i = 1; i < match_distance.length; i++) {
                if (min_match >= 0 && min_match < match_distance[i]) {
                    min_match_position = i;
                    min_match = match_distance[i];
                    min_matches = 0;
                } else if (min_match >= 0 && min_match == match_distance[i]) {
                    min_matches++;
                }
            }

            if (min_matches > 1) {
                i = 0;
                System.err.println("COMPILER WARNING [Invokable.matchParameterTypes]: matching targetArguments on '" + targetArguments.getLongSignature() + "' had multiple matches:");
                for (Invokable current : new LinkedList<Invokable>(invokableMap.values())) {
                    if (match_distance[i] == min_match) {
                        System.err.println("\t [distance: " + match_distance[i] + "]" + current.getLongSignature());
                    }
                    i++;
                }
            } else {
                return match;
            }
        }

        if (match == null) {
            System.err.println("COMPILER ERROR [Invokable.matchParameterTypes]: could not find matching targetArguments for '" + targetArguments.getLongSignature() + "'.");
            System.err.println("known potential invokables:");
            i = 0;
            for (Invokable current : new LinkedList<Invokable>(invokableMap.values())) {
                System.err.println("\t" + current.getLongSignature());
            }
        }

        return match;
    }

    /**
     *  The current invokable is has the arguments we're trying to send to target
     *  returns -1 if there is no match, otherwise it returns
     *  the distance between types (in terms of how many superclasses away it is)
     */
    public int matches(Invokable target) throws SalsaNotFoundException {
        if (!name.equals(target.getName())) return -1;
        if (parameterTypes.length != target.parameterTypes.length) return -1;

        int match_distance;
        int total_match_distance = 0;
        for (int i = 0; i < parameterTypes.length; i++) {
            match_distance = this.parameterTypes[i].canMatch( target.parameterTypes[i] );
            total_match_distance += match_distance;
//            System.err.println("\t[" + parameterTypes[i].getLongSignature() + "] vs [" + target.parameterTypes[i].getLongSignature() + "] -- " + canMatch);

            if (match_distance < 0) return -1;
        }

        return total_match_distance;
    }
}
