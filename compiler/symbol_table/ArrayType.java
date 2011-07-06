package salsa_lite.compiler.symbol_table;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.ParseException;

public class ArrayType extends TypeSymbol {

    public TypeSymbol subtype;

    public TypeSymbol getSubtype() { return subtype; }

    public TypeSymbol copy() {
        ArrayType copy = new ArrayType(module, name, subtype, superType);

        return copy;
    }

    public TypeSymbol replaceGenerics(String genericTypesString) throws SalsaNotFoundException {
        ArrayType copy = (ArrayType)this.copy();
        copy.subtype = copy.subtype.replaceGenerics(genericTypesString);
        return copy;
    }

    public static String getBaseType(String name) {
        if (name.charAt(name.length() - 1) == ']') {
            return getBaseType(name.substring(0, name.length() - 2));
        } else if (name.charAt(0) == '[') {
            switch (name.charAt(1)) {
                case 'Z': return "boolean";
                case 'B': return "byte";
                case 'C': return "char";
                case 'S': return "short";
                case 'I': return "int";
                case 'J': return "long";
                case 'F': return "float";
                case 'D': return "double";
                case 'L': return name.substring(2, name.length() - 1);                      //an object
                case '[': return getBaseType(name.substring(1, name.length()));             //another array
                default:
                          System.err.println("ERROR: initializing ArrayType.  Unknown subtype for [" + name + "]");
                          return name;
            }
        } else {
            return name;
        }
    }

    public static String getArrayDims(String name) {
        if (name.charAt(name.length() - 1) == ']') {
            return "[]" + getArrayDims(name.substring(0, name.length() - 2));
        } else if (name.charAt(0) == '[') {
            if (name.charAt(1) == '[') {
                return "[]" + getArrayDims(name.substring(1, name.length()));
            } else {
                return "[]";
            }
        } else {
            return "";
        }
    }

    public ArrayType(String module, String name, TypeSymbol subtype, TypeSymbol superType) {
        this.module = module;
        this.name = name;
        this.subtype = subtype;
        this.superType = superType;

        try {
            FieldSymbol fs = new FieldSymbol(this, "length", SymbolTable.getTypeSymbol("int"));
            fields.add( fs );
        } catch (SalsaNotFoundException snfe) {
            System.err.println("Very bad compiler error, could not find the type for 'int'.");
            throw new RuntimeException(snfe);
        }
    }

    public ArrayType(TypeSymbol subtype, String dimensions) throws SalsaNotFoundException {
        if (dimensions.length() > 2) this.subtype = new ArrayType(subtype, dimensions.substring(2, dimensions.length()));
        else this.subtype = subtype;

        this.superType = SymbolTable.getTypeSymbol("java.lang.Object");
        this.module = subtype.getModule();
        this.name = subtype.getName() + "[]";

        FieldSymbol fs = new FieldSymbol(this, "length", SymbolTable.getTypeSymbol("int"));
        fields.add( fs );
    }

    public String getLongSignature() {
        return name;
    }
}
