package salsa_lite.compiler.symbol_table;

import salsa_lite.compiler.definitions.CExpression;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Vector;
import java.util.StringTokenizer;

import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

public abstract class TypeSymbol implements Comparable<TypeSymbol> {
    TypeSymbol superType = null;
    public TypeSymbol getSuperType()    { return superType; }

    ArrayList<TypeSymbol> implementsTypes = new ArrayList<TypeSymbol>();

    String name;
    public String getName()             { return name; }

    String module;
    public String getModule()           { return module; }

    public String getSignature()        { return name + getDeclaredGenericsString(); }
    public String getLongSignature()    { return module + name + getDeclaredGenericsString(); }

    public String toString()            { return getLongSignature(); }

    public String getDeclaredGenericsString() {
        if (!isGeneric()) return "";
        String s = "<";

        for (String dg : declaredGenericTypes) {
            if (s.length() > 1) s += ",";
            s += dg;
        }

        s += ">";
        return s;
    }

    public boolean isInterface = false;

    public int compareTo(TypeSymbol other) {
        return getLongSignature().compareTo(other.getLongSignature());
    }

    public boolean equals(TypeSymbol other) {
        return this.getLongSignature().equals( other.getLongSignature() );
    }

	public ArrayList<FieldSymbol> fields                = new ArrayList<FieldSymbol>();
    public ArrayList<ConstructorSymbol> constructors    = new ArrayList<ConstructorSymbol>();

	public FieldSymbol          getField(int i)             { return fields.get(i); }
	public ConstructorSymbol    getConstructor(int i)       { return constructors.get(i); }

    public ConstructorSymbol    getConstructor(Vector<CExpression> expressions) throws SalsaNotFoundException {
        return (ConstructorSymbol)Invokable.matchInvokable(new Invokable(this, expressions), constructors);
    }

    public boolean isGeneric() {
        return declaredGenericTypes.size() > 0;
    }

    public ArrayList<String> declaredGenericTypes = new ArrayList<String>();
    public ArrayList<String> getDeclaredGenericTypes() {
        return declaredGenericTypes;
    } 

    public abstract TypeSymbol copy();
    public abstract TypeSymbol replaceGenerics(String genericTypesString) throws SalsaNotFoundException;

    public static String genericStringToName(String gs) {
        if (gs.startsWith("class ") || gs.startsWith("interface ")) {
            gs = gs.substring( gs.indexOf(" ") + 1, gs.length() );
        }

        if (!gs.contains("<") && (gs.contains(" extends") || gs.contains(" super"))) {
            gs = gs.substring( 0, gs.indexOf(" ") );
            System.err.println("WARNING: stripping '" + gs.substring(gs.indexOf(" ") + 1, gs.length()));
        }

        return gs;
    }

    public static String getGenericsString(TypeSymbol type, ArrayList<String> declaredGenericTypes, ArrayList<TypeSymbol> instantiatedGenericTypes) {
        ArrayList<String> otherDeclaredGenerics = type.getDeclaredGenericTypes();

        String genericsString = "<";
        for (int i = 0; i < otherDeclaredGenerics.size(); i++) {
            for (int j = 0; j < declaredGenericTypes.size(); j++) {
                if (otherDeclaredGenerics.get(i).equals( declaredGenericTypes.get(j) )) {
                    if (genericsString.length() > 1) genericsString += ",";
                    genericsString += instantiatedGenericTypes.get(j).getLongSignature();
                }
            }
        }

        genericsString += ">";

        return genericsString;
    }

    public static String removeBounds(String compareType) {
        int superIndex = compareType.indexOf(" super ");
        int extendsIndex = compareType.indexOf(" extends ");
        int bracketIndex = compareType.indexOf("<");

        if (bracketIndex < 0) bracketIndex = compareType.length();

        if (superIndex > 0 && superIndex < bracketIndex) {
            compareType = compareType.substring(0, compareType.indexOf(" super "));
        } else if (extendsIndex > 0 && extendsIndex < bracketIndex) {
            compareType = compareType.substring(0, compareType.indexOf(" extends "));
        }

        return compareType;
    }

    public static void addGenericType(String gt, String superType) throws SalsaNotFoundException {
        String extendsGeneric = null, superGeneric = null;
        if (gt.contains(" super ")) {
//            System.err.println("super in declared generic: " + gt);
            superGeneric = gt.substring( gt.indexOf(" super ") + " super ".length(), gt.length());
            addGenericType(superGeneric, superType);
        } else if (gt.contains(" extends ")) {
//            System.err.println("extends in declared generic: " + gt);
            extendsGeneric = gt.substring( gt.indexOf(" extends ") + " extends ".length(), gt.length());
            addGenericType(extendsGeneric, superType);
        }

        TypeSymbol genericType = null;
        try {
            genericType = SymbolTable.getTypeSymbol( gt );
        } catch (SalsaNotFoundException snfe) {
            //                    System.err.println("Could not get declared generic type for '" + gt + "'");
            //                    System.err.println("Going to try and create it.");
            genericType = null;
        }

        if (genericType == null) {
            if (superType.equals("Object")) {
                if (extendsGeneric != null) genericType = new ObjectType( gt, SymbolTable.getTypeSymbol(extendsGeneric) );
                else genericType = new ObjectType( gt, SymbolTable.getTypeSymbol(superType) );
            } else {
                if (extendsGeneric != null) genericType = new ActorType( gt, SymbolTable.getTypeSymbol(extendsGeneric) );
                else genericType = new ActorType( gt, SymbolTable.getTypeSymbol(superType) );
            }

            SymbolTable.addGenericVariableType( gt, genericType );
        }
    }

    public static ArrayList<TypeSymbol> parseGenerics(String genericTypesString, ArrayList<String> declaredGenericTypes, boolean fromObject) throws SalsaNotFoundException {
        int i = 0;
        ArrayList<TypeSymbol> instantiatedGenericTypes = new ArrayList<TypeSymbol>();

        genericTypesString = genericTypesString.substring(1, genericTypesString.length() - 1);
//        System.err.println("PARSING GENERICS: < " + genericTypesString + " >");

        StringTokenizer st = new StringTokenizer(genericTypesString, ",");
        while (st.hasMoreTokens()) {
            String generic_type = st.nextToken();
            if (generic_type.charAt(0) == ' ') generic_type = generic_type.substring(1, generic_type.length());

            int j;
            int bracket_count = 0;
            for (j = 0; j < generic_type.length(); j++) {
                if (generic_type.charAt(j) == '<') bracket_count++;
                if (generic_type.charAt(j) == '>') bracket_count--;
            }

            if (bracket_count > 0) {
//                System.err.println("generic_type: " + generic_type);
                int nextIndex = generic_type.indexOf(">");
                if (nextIndex > 0) {
                    generic_type += st.nextToken(">") + ">";
                    bracket_count--;
                }
                while (bracket_count > 0) {
                    nextIndex = generic_type.indexOf(">", nextIndex);
                    generic_type += st.nextToken(">") + ">";
                    bracket_count--;
                }
//                System.err.println("generic_type: " + generic_type + ", nextIndex: " + nextIndex);
            }

            /**
             *  This needs to be done because of (i think) a bug in Java.
             *  In a subclass, the package/class name gets duplicated.
             */

            if (generic_type.contains("$")) {
                if (!generic_type.contains("<") || (generic_type.indexOf("<") > generic_type.indexOf("$"))) {
                    String tmpName = generic_type;
                    tmpName = tmpName.substring(0, tmpName.indexOf("$"));
                    int len = tmpName.length();
                    int half = len/2;

                    String firstHalf = tmpName.substring(0, half);
                    String secondHalf = tmpName.substring(half, tmpName.length());
//                    System.err.println("subclass: " + generic_type);
//                    System.err.println("first half: " + firstHalf);
//                    System.err.println("second half: " + secondHalf);

                    if (new String("." + firstHalf).equals(secondHalf)) {
                        generic_type = firstHalf + generic_type.substring(generic_type.indexOf("$"), generic_type.length());
                    }
                }
            }

//            System.err.println("genericTypesString: " + genericTypesString + " -- token: " + generic_type);

            TypeSymbol ts = null;
            if (fromObject) {
                ts = SymbolTable.getTypeSymbol(generic_type, "java.lang.Object");
            } else {
                ts = SymbolTable.getTypeSymbol(generic_type, "LocalActor");
            }
            instantiatedGenericTypes.add(ts);

//            if (generic_type.contains(" super ") || declaredGenericTypes.get(i).contains(" super ") || ts.getLongSignature().contains(" super ")) {
//                System.err.println("TODO: Need to check to see if '" + generic_type + "' can replace '" + declaredGenericTypes.get(i) + "'");
//            } else if (generic_type.contains(" extends ") || declaredGenericTypes.get(i).contains(" extends ") || ts.getLongSignature().contains(" extends ")) {
//                System.err.println("TODO: Need to check to see if '" + generic_type + "' can replace '" + declaredGenericTypes.get(i) + "'");
//            } 

            if (declaredGenericTypes.get(i).contains(" super ") || declaredGenericTypes.get(i).contains(" extends ")) {
                String gt = declaredGenericTypes.get(i);

                if (gt.equals(ts.getLongSignature())) {
                } else if (gt.contains(" super ")) {
        //            System.err.println("super in declared generic: " + gt);
                    String superGeneric = gt.substring( gt.indexOf(" super ") + " super ".length(), gt.length());

                    System.err.println("TODO: Need to check to see if '" + generic_type + "' can replace '" + gt + "'");

                } else if (gt.contains(" extends ")) {
        //            System.err.println("extends in declared generic: " + gt);
                    String extendsGeneric = gt.substring( gt.indexOf(" extends ") + " extends ".length(), gt.length());

//                    System.err.println("TODO: Need to check to see if '" + generic_type + "' can replace '" + gt + "'");

                    TypeSymbol extendsType = SymbolTable.getTypeSymbol(extendsGeneric);
                    TypeSymbol replacementType = ts;
                    if (ts.canMatch(extendsType) < 0) {
                        throw new SalsaNotFoundException("no module", "no name", "Could not replace generic type '" + gt + "' with '" + ts + "'");
                    }
                }
            }

            i++;
        }
        return instantiatedGenericTypes;
    }

    public static String getReplacedGeneric(String typeString, ArrayList<TypeSymbol> instantiatedGenericTypes, ArrayList<String> declaredGenericTypes) {
        for (int i = 0; i < declaredGenericTypes.size(); i++) {
            if (typeString.equals(declaredGenericTypes.get(i))) {
                return instantiatedGenericTypes.get(i).getLongSignature();
            }
        }
        return "";
     }

    public TypeSymbol getFieldType(String fieldName) {
        for (FieldSymbol f : fields) {
            if (f.getName().equals(fieldName)) return f.getType();
        }

        if (superType != null) {
            return superType.getFieldType(fieldName);
        }
        return null;
    }

    /**
     *  This type matches the target if the target is the same type, or a superclass of this type
     */
    public int canMatch(TypeSymbol target) {
        return canMatch(target, 0);
    }

    public int canMatch(TypeSymbol target, int distance) {
//        System.err.println("distance: " + distance + ", testing '" + getSignature() + "' vs '" + target.getSignature() + "'");

        if (this.equals(target)) return distance;

        if (target.getName().equals("null")) {
//            System.err.println("target's name is null for match against '" + this.getLongSignature());
            if (this instanceof PrimitiveType) return -1;
            else return distance; // null can match any actor, array or object
        }

        if (!(target instanceof PrimitiveType) && this instanceof PrimitiveType) {
            try {
                if (this.getName().equals("null")) return distance;

                TypeSymbol nonPrimitive = SymbolTable.getTypeSymbol( this.toNonPrimitiveString() );
                return nonPrimitive.canMatch(target, distance + 1);
            } catch (SalsaNotFoundException snfe) {
                System.err.println("Could not find TypeSymbol for non-primivite version of primitive type '" + toNonPrimitiveString() + "'.");
                System.err.println("This should never happen.");
                throw new RuntimeException();
            }
        }

        if (target instanceof PrimitiveType && !(this instanceof PrimitiveType)) {
            try {
                TypeSymbol nonPrimitive = SymbolTable.getTypeSymbol( target.toNonPrimitiveString() );
                return this.canMatch(nonPrimitive, distance + 1);
            } catch (SalsaNotFoundException snfe) {
                System.err.println("Could not find TypeSymbol for non-primivite version of primitive type '" + target.toNonPrimitiveString() + "'.");
                System.err.println("This should never happen.");
                throw new RuntimeException();
            }
        }

        int temp_distance = -1;

        if (superType != null) temp_distance = superType.canMatch(target, distance + 1);
        if (temp_distance >= 0) return temp_distance;

        for (TypeSymbol implementsType : implementsTypes) {
            temp_distance = implementsType.canMatch(target, distance + 1);
            if (temp_distance >= 0) return temp_distance;
        }
        return -1;
    }

    public String toNonPrimitiveString() {
		if (name.equals("short")) return "Short";
		else if (name.equals("int")) return "Integer";
		else if (name.equals("long")) return "Long";
		else if (name.equals("float")) return "Float";
		else if (name.equals("double")) return "Double";
		else if (name.equals("boolean")) return "Boolean";
		else if (name.equals("char")) return "Character";
		else if (name.equals("byte")) return "Byte";
		else return getSignature();
    }
}
