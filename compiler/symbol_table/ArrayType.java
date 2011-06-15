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

	public ArrayType(String name) throws SalsaNotFoundException {
        if (name.charAt(0) == '[') {
            if (name.charAt(1) == 'L') {
                subtype = SymbolTable.getTypeSymbol(name.substring(2, name.length() - 1));
            } else {
                switch (name.charAt(1)) {
                    case 'Z': subtype = SymbolTable.getTypeSymbol("boolean"); break;
                    case 'B': subtype = SymbolTable.getTypeSymbol("byte"); break;
                    case 'C': subtype = SymbolTable.getTypeSymbol("char"); break;
                    case 'S': subtype = SymbolTable.getTypeSymbol("short"); break;
                    case 'I': subtype = SymbolTable.getTypeSymbol("int"); break;
                    case 'J': subtype = SymbolTable.getTypeSymbol("long"); break;
                    case 'F': subtype = SymbolTable.getTypeSymbol("float"); break;
                    case 'D': subtype = SymbolTable.getTypeSymbol("double"); break;
                    case '[': subtype = SymbolTable.getTypeSymbol(name.substring(1, name.length())); break;
                    default:
                              System.err.println("ERROR: initializing ArrayType.  Unknown subtype for [" + name + "]");
                }
            }
        } else {
            subtype = SymbolTable.getTypeSymbol(name.substring(0, name.length() - 2));
        }

//        System.out.println("New ArrayType -- [" + this.module + "] " + this.name + " -- subtype: " + subtype.getLongSignature());

        this.superType = SymbolTable.getTypeSymbol("java.lang.Object");
        this.module = subtype.getModule();
        this.name = subtype.getName() + "[]";

        FieldSymbol fs = new FieldSymbol(this, "length", SymbolTable.getTypeSymbol("int"));
        fields.put( fs.getLongSignature(), fs );
	}

    public String getLongSignature() {
        return name;
    }
}
