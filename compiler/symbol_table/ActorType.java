package salsa_lite.compiler.symbol_table;

import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ArrayList;
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
//        if (!name.contains(".")) System.err.println("ERROR: creating new ActorType without a package: " + name);

        this.module = name.substring(0, name.lastIndexOf('.') + 1);
        this.name = name.substring(name.lastIndexOf('.') + 1, name.length());
	}

	public ActorType(String name, TypeSymbol superType) {
        this(name);
		this.superType = superType;
	}

    private ActorType(String module, String name, TypeSymbol superType, ArrayList<TypeSymbol> implementsTypes, ArrayList<String> declaredGenericTypes, ArrayList<FieldSymbol> fields, ArrayList<ConstructorSymbol> constructors, ArrayList<MessageSymbol> message_handlers, ArrayList<MessageSymbol> overloaded_message_handlers) {
        this.module = module;
        this.name = name;
        this.superType = superType;
        this.implementsTypes = implementsTypes;
        this.declaredGenericTypes = declaredGenericTypes;
        this.fields = fields;
        this.constructors = constructors;
        this.message_handlers = message_handlers;
        this.overloaded_message_handlers = overloaded_message_handlers;
    }

    public TypeSymbol copy() {
        ActorType copy = new ActorType(module, name, superType, new ArrayList<TypeSymbol>(implementsTypes), new ArrayList<String>(declaredGenericTypes), new ArrayList<FieldSymbol>(fields), new ArrayList<ConstructorSymbol>(constructors), new ArrayList<MessageSymbol>(message_handlers), new ArrayList<MessageSymbol>(overloaded_message_handlers));

        for (ConstructorSymbol cs : copy.constructors)   cs.enclosingType = copy;
        for (MessageSymbol ms : copy.message_handlers)  ms.enclosingType = copy;
        for (MessageSymbol ms : copy.overloaded_message_handlers)  ms.enclosingType = copy;
        for (FieldSymbol fs : copy.fields)              fs.enclosingType = copy;

        return copy;
    } 

    public TypeSymbol replaceGenerics(String genericTypesString) throws SalsaNotFoundException {
        ActorType copy = (ActorType)this.copy();

        if (!isGeneric()) throw new SalsaNotFoundException(module, name, "Tried to replace generics on non generic-class: " + this.getLongSignature());

        ArrayList<TypeSymbol> instantiatedGenericTypes = parseGenerics(genericTypesString, declaredGenericTypes, false);

        if (instantiatedGenericTypes.size() != declaredGenericTypes.size()) {
            throw new SalsaNotFoundException(module, name, "Wrong number of generic parameters. instantiated [" + instantiatedGenericTypes.toString() + "], declared [" + declaredGenericTypes.toString() + "]. generic object");
        }

        if (copy.superType != null && copy.superType.isGeneric()) {
            copy.superType = copy.superType.replaceGenerics(getGenericsString(superType, declaredGenericTypes, instantiatedGenericTypes));
        } else {
            copy.superType = this.superType;
        }

        for (int i = 0; i < copy.implementsTypes.size(); i++) {
            if (copy.implementsTypes.get(i).isGeneric()) {
                copy.implementsTypes.set(i, copy.implementsTypes.get(i).replaceGenerics(getGenericsString(copy.implementsTypes.get(i), declaredGenericTypes, instantiatedGenericTypes)));
            }
        }

        for (int i = 0; i < copy.constructors.size(); i++)       copy.constructors.set(i, copy.constructors.get(i).replaceGenerics(declaredGenericTypes, instantiatedGenericTypes));
        for (int i = 0; i < copy.message_handlers.size(); i++)   copy.message_handlers.set(i, copy.message_handlers.get(i).replaceGenerics(declaredGenericTypes, instantiatedGenericTypes));
        for (int i = 0; i < copy.overloaded_message_handlers.size(); i++)   copy.overloaded_message_handlers.set(i, copy.overloaded_message_handlers.get(i).replaceGenerics(declaredGenericTypes, instantiatedGenericTypes));
        for (int i = 0; i < copy.fields.size(); i++)             copy.fields.set(i, copy.fields.get(i).replaceGenerics(declaredGenericTypes, instantiatedGenericTypes));

//        System.err.println("replaced generics for " + copy.getLongSignature());
//        System.err.println("\tfields: " + copy.fields.toString());
//        System.err.println("\tconstructors: " + copy.constructors.toString());
//        System.err.println("\tmessage_handlers: " + copy.message_handlers.toString());
        return copy;
    }

	public ArrayList<MessageSymbol> message_handlers = new ArrayList<MessageSymbol>();
	public ArrayList<MessageSymbol> overloaded_message_handlers = new ArrayList<MessageSymbol>();

	public MessageSymbol        getMessageHandler(int i)                { return message_handlers.get(i); }
	public MessageSymbol        getOverloadedMessageHandler(int i)      { return overloaded_message_handlers.get(i); }

    public MessageSymbol        getMessage(String name, Vector<CExpression> parameters, boolean isParentMessage) throws SalsaNotFoundException {
//        System.err.println("getting message for: " + name + ", isParentMessage: " + isParentMessage);
//        System.err.println("overloaded: " + overloaded_message_handlers.toString());
//        System.err.println("message handlers: " + message_handlers.toString());

        if (isParentMessage) {
            return (MessageSymbol)Invokable.matchInvokable(new Invokable(name, this, parameters), overloaded_message_handlers);
        } else {
            return (MessageSymbol)Invokable.matchInvokable(new Invokable(name, this, parameters), message_handlers);
        }
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

        SymbolTable.openScope();

        String oldModule;
        CCompilationUnit cu;
        try {
            String longSignature = getLongSignature();
            if (longSignature.contains("<")) {
                longSignature = longSignature.substring(0, longSignature.indexOf("<"));
            }
            String filename = findSalsaFile(longSignature);

//            System.err.println("Getting new compilation unit to read new Actor '" + longSignature + "', filename: "+ filename);

            if (filename == null) {
                throw new SalsaNotFoundException(module, name, "actor");
            } 

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
        SymbolTable.closeScope();
    }

    public void load(CCompilationUnit cu, String oldModule) throws SalsaNotFoundException {
        int i;
        if (cu.getExtendsName() != null) {
            this.superType = SymbolTable.getTypeSymbol(cu.getExtendsName().name);
            if (this.superType.isInterface) this.isInterface = true;
        } else {
            this.superType = SymbolTable.getTypeSymbol("Actor");
            this.isInterface = true;
        }

        for (CName implementsName : cu.getImplementsNames()) {
            this.implementsTypes.add( SymbolTable.getTypeSymbol(implementsName.name) );
        }

        if (declaredGenericTypes.size() == 0) {
            for (CGenericType gt : cu.getName().generic_types) {
                declaredGenericTypes.add(gt.toString());
                addGenericType( gt.toString(), "Actor" );

                TypeSymbol ts = SymbolTable.getTypeSymbol(gt.toString());
//                System.err.println("adding generic actor type: " + ts + ", supertype: " + ts.superType);
           }
        }

        if (cu.behavior_declaration != null && cu.behavior_declaration.implements_names != null) {
            for (CName implementsName : cu.behavior_declaration.implements_names) {
                implementsTypes.add( SymbolTable.getTypeSymbol(implementsName.name) );
            }
        }

        for (CGenericType generic_type : cu.getName().generic_types) {
            ObjectType ot;

            if (generic_type.bound != null) {
                if (generic_type.name.name.equals("?")) {
                    CompilerErrors.printErrorMessage("Compiler problem, '?' in generic type not currently supported.", generic_type);
                }

                if (generic_type.modifier.equals("extends")) {
                    ot = new ObjectType(SymbolTable.getCurrentModule() + generic_type.name.name, SymbolTable.getTypeSymbol(generic_type.bound.name));
                } else {
                    CompilerErrors.printErrorMessage("Compiler problem, 'super' in generic type not currently supported.", generic_type);
                    ot = null;
                }

            } else {
                if (generic_type.name.name.equals("?")) {
                    CompilerErrors.printErrorMessage("Compiler problem, '?' in generic type not currently supported.", generic_type);
                    ot = new ObjectType(SymbolTable.getCurrentModule() + generic_type.name.name);
                } else {
                    ot = new ObjectType(SymbolTable.getCurrentModule() + generic_type.name.name, SymbolTable.getTypeSymbol("Actor"));
                }
            }

            SymbolTable.knownTypes.put(ot.getLongSignature(), ot);
            SymbolTable.namespace.put(ot.getName(), ot.getLongSignature());
        }

        ArrayList<MessageSymbol> superclassMessageHandlers = null;
        if (getSuperType() instanceof ActorType) {
            ActorType superType = (ActorType)getSuperType();

            superclassMessageHandlers = new ArrayList<MessageSymbol>();

            for (MessageSymbol ms : superType.message_handlers) superclassMessageHandlers.add(ms.copy());
            for (MessageSymbol ms : superType.overloaded_message_handlers) {
                MessageSymbol copy = ms.copy();
                copy.isOverloadedByParent = true;
                superclassMessageHandlers.add(copy);
            }

        } else {
            superclassMessageHandlers = new ArrayList<MessageSymbol>();
        }

		Vector<CConstructor> cv = cu.getConstructors();
        if (cv != null) {
            for (i = 0; i < cv.size(); i++) {
                ConstructorSymbol cs = new ConstructorSymbol(i, this, cv.get(i));
                constructors.add( cs );
            }
            if (cv.size() == 0) {
//                ConstructorSymbol cs = new ConstructorSymbol(0, this, new TypeSymbol[]{});
//                constructors.add( cs );
            } else if (cv.size() == 1 && cv.get(0).getArgumentTypes().length == 1 && cv.get(0).getArgumentTypes()[0].equals("String[]")) {
//                ConstructorSymbol cs = new ConstructorSymbol(0, this, new TypeSymbol[]{});
//                constructors.add( cs );
            }
        }

        ArrayList<MessageSymbol> childMessageHandlers = new ArrayList<MessageSymbol>();

        Vector<CMessageHandler> mv = cu.getMessageHandlers();
		for (i = 0; i < mv.size(); i++) {
            MessageSymbol ms = new MessageSymbol(i, this, mv.get(i));
            childMessageHandlers.add( ms );
        }

        for (i = 0; i < superclassMessageHandlers.size(); i++) {
            MessageSymbol sms = superclassMessageHandlers.get(i);

            MessageSymbol overloadedMessageHandler = null;
            for (MessageSymbol cms : childMessageHandlers) {
                if (sms.matches(cms) == 0) {
                    if (overloadedMessageHandler != null) {
                        System.err.println("ERROR, multiple methods override parent method. Need to throw a good compiler error.");
                        throw new RuntimeException();
                    } else {
                        overloadedMessageHandler = cms;
                    }
                }
            }

            if (overloadedMessageHandler == null) {
                message_handlers.add(sms);
            } else {
                message_handlers.add(overloadedMessageHandler);
//                System.err.println("child message handlers: " + childMessageHandlers.toString());
                childMessageHandlers.remove(overloadedMessageHandler);
//                System.err.println("child message handlers: " + childMessageHandlers.toString() + " (after remove)");

                if (!sms.is_abstract) {
                    overloaded_message_handlers.add(sms.copy());
                }
//                System.err.println("adding to overloaded message handlers for: " + toString() + " -- " + sms.toString());
            }
        }

        for (MessageSymbol ms : childMessageHandlers) message_handlers.add(ms);
        int current = 0;
        for (MessageSymbol ms : message_handlers) ms.id = current++;
        for (MessageSymbol ms : overloaded_message_handlers) ms.id = current++; 

//        System.err.println("overloaded: " + overloaded_message_handlers.toString());
//        System.err.println("message handlers: " + message_handlers.toString());

		Vector<CLocalVariableDeclaration> fv = cu.getFields();
		for (i = 0; i < fv.size(); i++) {
            Vector<CVariableInit> vv = fv.get(i).variables;
            for (int j = 0; j < vv.size(); j++) {
                FieldSymbol fs = new FieldSymbol(this, fv.get(i), vv.get(j));
                fields.add( fs );
            }
		}

        SymbolTable.setCurrentModule(oldModule);
	}
}
