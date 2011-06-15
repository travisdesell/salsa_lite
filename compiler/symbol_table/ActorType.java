package salsa_lite.compiler.symbol_table;

import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.LinkedHashMap;
import java.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.ParseException;

import salsa_lite.compiler.definitions.CompilerErrors;

import salsa_lite.compiler.definitions.CCompilationUnit;
import salsa_lite.compiler.definitions.CConstructor;
import salsa_lite.compiler.definitions.CExpression;
import salsa_lite.compiler.definitions.CName;
import salsa_lite.compiler.definitions.CGenericType;
import salsa_lite.compiler.definitions.CMessageHandler;
import salsa_lite.compiler.definitions.CLocalVariableDeclaration;
import salsa_lite.compiler.definitions.CVariableInit;

public class ActorType extends TypeSymbol {
	public ActorType(String name) {
        if (!name.contains(".")) {
            System.err.println("ERROR: creating new ActorType without a package: " + name);
        }

        this.module = name.substring(0, name.lastIndexOf('.') + 1);
        this.name = name.substring(name.lastIndexOf('.') + 1, name.length());

//        System.out.println("New ActorType -- [" + this.module + "] " + this.name);
	}

	public ActorType(String name, TypeSymbol superType) {
        this(name);
		this.superType = superType;
	}


	public LinkedHashMap<String,MessageSymbol> message_handlers    = new LinkedHashMap<String,MessageSymbol>();

	public MessageSymbol        getMessageHandler(int i)    { return new ArrayList<MessageSymbol>(message_handlers.values()).get(i); }

    public MessageSymbol        getMessage(String s)        { return message_handlers.get(s); }

    public MessageSymbol        getMessage(String name, Vector<CExpression> parameters) throws SalsaNotFoundException {
        return (MessageSymbol)Invokable.matchInvokable(new Invokable(name, this, parameters), message_handlers);
    }

	static String[] classpaths = null;
	static {
                String classpath = System.getProperty("java.class.path");

                StringTokenizer st = new StringTokenizer(classpath, ":");
                classpaths = new String[st.countTokens()];
                for (int i = 0; i < classpaths.length; i++) {
                        classpaths[i] = st.nextToken();
                        if (classpaths[i].charAt(classpaths[i].length() - 1) != File.separatorChar) classpaths[i] += File.separatorChar;
                }
	}

    public static String findSalsaFile(String name) {
        for (int i = 0; i < classpaths.length; i++) {
            String filename = classpaths[i] + name.replace('.', File.separatorChar) + ".salsa";

//            System.out.println("looking for actor: " + filename);

            if (new File(filename).exists()) return filename;
        }

        return null;
    }

	public void load() throws SalsaNotFoundException {
		String file = new String(module + name).replace('.', File.separatorChar);

        String oldModule;
        CCompilationUnit cu;
        try {
            String longSignature = getLongSignature();
            if (longSignature.contains("<")) longSignature = longSignature.substring(0, longSignature.indexOf("<"));
            String filename = findSalsaFile(longSignature);

//            System.err.println("Getting new compilation unit to read new Actor: "+ filename);

            FileInputStream fis = new FileInputStream( filename );
            SalsaParser.ReInit(fis);
            cu = SalsaParser.CompilationUnit();

            oldModule = SymbolTable.getCurrentModule();
//            System.err.println("setting current module to: " + module);
            SymbolTable.setCurrentModule(module);
            cu.getImportDeclarationCode();  //to import the appropriate objects/actors


        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            System.out.println("Compiler Error: Could not find actor file: " + file + ", imported SALSA actors must exist");
            throw new SalsaNotFoundException(module, name, "actor");
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            System.out.println("Compiler Error: Could not parse actor: " + file + ", imported SALSA actors must be parseable");
            throw new SalsaNotFoundException(module, name, "actor");
        }

        load(cu, oldModule);
    }

    public void load(CCompilationUnit cu, String oldModule) throws SalsaNotFoundException {
        int i;
        if (cu.getExtendsName() != null) {
            this.superType = SymbolTable.getTypeSymbol(cu.getExtendsName().name);
            if (this.superType.isInterface) this.isInterface = true;
        } else {
            this.superType = SymbolTable.getTypeSymbol("LocalActor");
            this.isInterface = true;
        }

        if (cu.behavior_declaration != null && cu.behavior_declaration.implements_names != null) {
            for (CName implementsName : cu.behavior_declaration.implements_names) {
                implementsTypes.add( SymbolTable.getTypeSymbol(implementsName.name) );
            }
        }

        for (CGenericType generic_type : cu.getName().generic_types) {
            ObjectType ot;
            if (generic_type.name.equals("?")) {
                ot = new ObjectType(SymbolTable.getCurrentModule() + generic_type.extends_type);
            } else if (generic_type.extends_type != null) {
                ot = new ObjectType(SymbolTable.getCurrentModule() + generic_type.name, SymbolTable.getTypeSymbol(generic_type.extends_type));
            } else {
                ot = new ObjectType(SymbolTable.getCurrentModule() + generic_type.name, SymbolTable.getTypeSymbol("Object"));
            }
            SymbolTable.knownTypes.put(ot.getLongSignature(), ot);
            SymbolTable.namespace.put(ot.getName(), ot.getLongSignature());
        }


		Vector<CConstructor> cv = cu.getConstructors();
        if (cv != null) {
            for (i = 0; i < cv.size(); i++) {
                ConstructorSymbol cs = new ConstructorSymbol(i, this, cv.get(i));
                constructors.put( cs.getLongSignature(), cs );
            }
        }

        Vector<CMessageHandler> mv = cu.getMessageHandlers();
		for (i = 0; i < mv.size(); i++) {
            MessageSymbol ms = new MessageSymbol(i, this, mv.get(i));
            message_handlers.put( ms.getLongSignature(), ms );
        }

		Vector<CLocalVariableDeclaration> fv = cu.getFields();
		for (i = 0; i < fv.size(); i++) {
            Vector<CVariableInit> vv = fv.get(i).variables;
            for (int j = 0; j < vv.size(); j++) {
                FieldSymbol fs = new FieldSymbol(this, fv.get(i), vv.get(j));
                fields.put( fs.getLongSignature(), fs );
            }
		}

//        System.err.println("resetting current module to: " + oldModule + ", done loading '" + getLongSignature() + "'.");
        SymbolTable.setCurrentModule(oldModule);
	}
}
