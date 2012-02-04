package salsa_lite.compiler.definitions;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.SimpleNode;

import java.util.Vector;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.ActorType;
import salsa_lite.compiler.symbol_table.TypeSymbol;
import salsa_lite.compiler.symbol_table.ConstructorSymbol;
import salsa_lite.compiler.symbol_table.MessageSymbol;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;
import salsa_lite.compiler.symbol_table.VariableDeclarationException;

import salsa_lite.compiler.symbol_table.ObjectType;

public class CCompilationUnit {
	public String module_string = null;
    public String getModule() {
        return module_string;
    }

	public Vector<CImportDeclaration> import_declarations = new Vector<CImportDeclaration>();

	public CBehaviorDeclaration behavior_declaration;
	public CInterfaceDeclaration interface_declaration;

	public CJavaStatement java_statement;

	public CName getName() {
		if (behavior_declaration != null) return behavior_declaration.behavior_name;
		else return interface_declaration.interface_name;
	}

	public CName getExtendsName() {
		if (behavior_declaration != null) return behavior_declaration.getExtendsName();
		else return null;
	}

    public Vector<CName> getImplementsNames() {
        if (behavior_declaration == null) {
            return interface_declaration.extends_names;
        } else {
            return behavior_declaration.implements_names;
        }
    }

    public boolean isRemote() {
        return getExtendsName() != null && getExtendsName().name.equals("RemoteActor");
    }

    public boolean isMobile() {
        return getExtendsName() != null && getExtendsName().name.equals("MobileActor");
    }
    
    public boolean isStaged() {
        Vector<CName> implementsNames = getImplementsNames();

        if (implementsNames != null) {
            for (CName name : implementsNames) {
                if (name.name.equals("StagedActor")) return true;
            }

        }
        return false;
    }

	public Vector<CEnumeration> getEnumerations() {
		if (behavior_declaration != null) return behavior_declaration.enumerations;
		else return null;
	}

	public Vector<CLocalVariableDeclaration> getFields() {
		if (behavior_declaration != null) return behavior_declaration.variable_declarations;
		else return interface_declaration.variable_declarations;
	}

	public Vector<CMessageHandler> getMessageHandlers() {
		if (behavior_declaration != null) return behavior_declaration.message_handlers;
		else return interface_declaration.message_handlers;
	}

	public Vector<CConstructor> getConstructors() {
		if (behavior_declaration != null) return behavior_declaration.constructors;
		else return null;
	}

	public String getImportDeclarationCode() {
		String code = "";
		String package_name = SymbolTable.getRuntimeModule();

		for (int i = 0; i < import_declarations.size(); i++) {
			CImportDeclaration import_declaration = import_declarations.get(i);
			String import_string = import_declaration.import_string;
			import_string = import_string.replace("salsa_lite.language.", package_name + ".language.");
			import_string = import_string.replace("salsa_lite.io.", package_name + ".io.");
			import_string = import_string.replace("salsa_lite.util.", package_name + ".util.");

			code += "import " + import_string + ";\n";

//            System.err.println("LOADING NEW: '" + import_string + "' FROM " + module_string + " " + getName());

            try {
                if (import_declaration.is_object) {
                    SymbolTable.importObject(import_string);
                    SymbolTable.addVariableType(import_declaration.import_string, import_declaration.import_string, false, true);

                } else if (import_declaration.is_enum) {
                    SymbolTable.importObject(import_string);
                    SymbolTable.addVariableType(import_declaration.import_string, import_declaration.import_string, false, true);

                } else SymbolTable.importActor(import_string);
            } catch (VariableDeclarationException vde) {
                CompilerErrors.printErrorMessage("[CCompilationUnit.getImportDeclarationCode] Could not declare variable. " + vde.toString(), import_declaration);
                throw new RuntimeException(vde);
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("[CCompilationUnit.getImportDeclarationCode] Could not find object, enum or actor to import. " + snfe.toString(), import_declaration);
                throw new RuntimeException(snfe);
            }
		}
		return code;
	}

	public String getFieldCode() {
		String code = "";
		for (int i = 0; i < getFields().size(); i++) {
            CLocalVariableDeclaration v = getFields().get(i);

            if (v.is_token) {
                CompilerErrors.printErrorMessage("Error declaring field(s). Currently, fields cannot be tokens.", v);
                continue;
            }

			for (int j = 0; j < getFields().get(i).variables.size(); j++) {
				CVariableInit vi = v.variables.get(j);

                try {
    				code += CIndent.getIndent() + v.type.name + " " + vi.toJavaCode(v.type.name, v.is_token) + ";\n";
				    SymbolTable.addVariableType(vi.name, v.type.name, v.is_token, false);
                } catch (VariableDeclarationException vde) {
                    CompilerErrors.printErrorMessage("[CCompilationUnit.getImportDeclarationCode] Could not declare variable. " + vde.toString(), vi);
                    throw new RuntimeException(vde);
                } catch (SalsaNotFoundException snfe) {
                    CompilerErrors.printErrorMessage("[CCompilationUnit.getFieldCode] Could not find type for field. " + snfe.toString(), vi);
                    throw new RuntimeException(snfe);
                }
			}
		}
		return code;
	}

	public String getEnumerationCode() {
		String code = "";
        if (getEnumerations() != null) {
            for (int i = 0; i < getEnumerations().size(); i++) {
                code += CIndent.getIndent() + getEnumerations().get(i).toJavaCode() + "\n";
            }
        }
		return code;
	}

	public String getCaseInvocation(TypeSymbol[] parameterTypes) throws SalsaNotFoundException {
		String code = "(";
		for (int i = 0; i < parameterTypes.length; i++) {
            String typeString = parameterTypes[i].toNonPrimitiveString();
            typeString = TypeSymbol.removeBounds(typeString);

			code += " (" + typeString +")arguments[" + i +"]";
			if (i < parameterTypes.length - 1) code += ",";
			else code += " ";
		}

		return code + ")";
	}

	public String getInvokeMessageCode() {
		String code = "";

		code += CIndent.getIndent() + "public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {\n";
		CIndent.increaseIndent();
		code += CIndent.getIndent() + "switch(messageId) {\n";
		CIndent.increaseIndent();

		TypeSymbol typeSymbol = null;
        try {
            CName name = getName();
            typeSymbol = SymbolTable.getTypeSymbol(getName().name);
            if (!(typeSymbol instanceof ActorType)) {
                CompilerErrors.printErrorMessage("VERY BAD COMPILER ERROR [CCompilationUnit.getInvokeMessageCode]: Compiler thinks current actor is not an actor, thinks it is: " + typeSymbol.getLongSignature(), name);
            }
        } catch (VariableDeclarationException vde) {
            CompilerErrors.printErrorMessage("VERY BAD ERROR. Could not declare actor being compiled. This should never happen. " + vde.toString(), behavior_declaration);
            throw new RuntimeException(vde);
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("VERY BAD ERROR. Could not get type for actor being compiled. This should never happen.", behavior_declaration);
            throw new RuntimeException(snfe);
        }

        ActorType at = (ActorType)typeSymbol;
        int number_message_handlers = at.message_handlers.size();

        boolean overloads = false;
		for (int i = 0; i < at.message_handlers.size(); i++) {
			MessageSymbol sm = at.getMessageHandler(i);

            try {
                if (sm.isOverloadedByParent) {
                    if (overloads == false) {
                        overloads = true;
                        code += CIndent.getIndent() + "/* Superclasses Overloaded Message Handlers */\n";
                    }
                    code += CIndent.getIndent() + "case " + sm.getId() + ": return super.invokeMessage(messageId, arguments);\n";
                } else if (sm.getPassType().getName().equals("ack")) {
                    code += CIndent.getIndent() + "case " + sm.getId() + ": " + sm.getName() + getCaseInvocation(sm.parameterTypes) + "; return null;\n";
                } else {
                    code += CIndent.getIndent() + "case " + sm.getId() + ": return " + sm.getName() + getCaseInvocation(sm.parameterTypes) + ";\n";
                }
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("[CCompilationUnit.getInvokeMessageCode]: Error getting parameter types for message handler. " + snfe.toString(), getMessageHandlers().get(i));
                throw new RuntimeException(snfe);
            }
		}

        if (at.overloaded_message_handlers.size() > 0) {
            code += CIndent.getIndent() + "/* Overloaded Message handlers */\n";
            for (int j = 0; j < at.overloaded_message_handlers.size(); j++) {
                MessageSymbol sm = at.getOverloadedMessageHandler(j);

                try {
                    if (sm.getPassType().getName().equals("ack")) {
                        code += CIndent.getIndent() + "case " + sm.getId() + ": super." + sm.getName() + getCaseInvocation(sm.parameterTypes) + "; return null;\n";
                    } else {
                        code += CIndent.getIndent() + "case " + sm.getId() + ": return super." + sm.getName() + getCaseInvocation(sm.parameterTypes) + ";\n";
                    }
                } catch (SalsaNotFoundException snfe) {
                    CompilerErrors.printErrorMessage("[CCompilationUnit.getInvokeMessageCode]: Error getting parameter types for message handler. " + snfe.toString(), getMessageHandlers().get(j));
                    throw new RuntimeException(snfe);
                }
            }
        }

		code += CIndent.getIndent() + "default: throw new MessageHandlerNotFoundException(messageId, arguments);\n";
		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";

		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";
		return code;
	}

	public String getMessageCode() {
		String code = "";
		for (int i = 0; i < getMessageHandlers().size(); i++) {
			CMessageHandler mh = getMessageHandlers().get(i);
			code += mh.toJavaCode();
		}
		return code;
	}

	public String getInvokeConstructorCode() {
		String code = "";

		code += CIndent.getIndent() + "public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {\n";
		CIndent.increaseIndent();
		code += CIndent.getIndent() + "switch(messageId) {\n";
		CIndent.increaseIndent();

		TypeSymbol typeSymbol = null;
        try {
            CName name = getName();
            typeSymbol = SymbolTable.getTypeSymbol(name.name);
            if (!(typeSymbol instanceof ActorType)) {
                CompilerErrors.printErrorMessage("VERY BAD COMPILER ERROR [CCompilationUnit.getInvokeMessageCode]: Compiler thinks current actor is not an actor, thinks it is: " + typeSymbol.getLongSignature(), name);
            }
        } catch (VariableDeclarationException vde) {
            CompilerErrors.printErrorMessage("VERY BAD ERROR. Could not get declare actor being compiled. This should never happen. " + vde.toString(), behavior_declaration);
            throw new RuntimeException(vde);
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("VERY BAD ERROR. Could not get type for actor being compiled. This should never happen.", behavior_declaration);
            throw new RuntimeException(snfe);
        }

        ActorType at = (ActorType)typeSymbol;

        boolean overloads = false;
		for (int i = 0; i < at.constructors.size(); i++) {
			ConstructorSymbol cm = at.getConstructor(i);

            try {
                if (cm.isOverloadedByParent) {
                    if (overloads == false) {
                        overloads = true;
                        code += CIndent.getIndent() + "/* Superclasses Overloaded Message Handlers */\n";
                    }
                    code += CIndent.getIndent() + "case " + cm.getId() + ": return super.invokeMessage(messageId, arguments);\n";
                } else {
    			    code += CIndent.getIndent() + "case " + cm.getId() + ": construct" + getCaseInvocation(cm.parameterTypes) + "; return;\n";
                }
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("[CCompilationUnit.getInvokeConstructorCode]: Error getting parameter types for constructor. " + snfe.toString(), getConstructors().get(i));
                throw new RuntimeException(snfe);
            }
		}

		code += CIndent.getIndent() + "default: throw new ConstructorNotFoundException(messageId, arguments);\n";
		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";

		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";
		return code;
	}

	public String getConstructorCode() {
		Vector<CConstructor> constructors = getConstructors();
		String code = "";
        if (interface_declaration == null && (constructors == null || constructors.size() == 0 || (constructors.size() == 1 && constructors.get(0).getArgumentTypes().length == 1 && constructors.get(0).getArgumentTypes()[0].equals("String[]")))) {
            //in this case we need to make a default constructor
            code += CIndent.getIndent() + "public void construct() {}\n\n";
        }
        if (constructors != null) {
            for (int i = 0; i < constructors.size(); i++) {
                code += constructors.get(i).toJavaCode(); 
            }
            code += "\n";
        } 
		return code;
	}

	public String getStateCode() {
		String code = "";

		if (module_string != null) {
			code += "package " + module_string + ";\n\n";
			SymbolTable.setCurrentModule(module_string + ".");
		}

		code += "/****** SALSA LANGUAGE IMPORTS ******/\n";
        code += "import salsa_lite.common.DeepCopy;\n";
        if (isMobile()) {
            code += "import salsa_lite.runtime.MobileActorRegistry;\n";
            code += "import salsa_lite.runtime.wwc.NameServer;\n";
        } else if (isRemote()) {
            code += "import salsa_lite.runtime.RemoteActorRegistry;\n";
        } else {
            code += "import salsa_lite.runtime.LocalActorRegistry;\n";
        }
        code += "import salsa_lite.runtime.Hashing;\n";
        code += "import salsa_lite.runtime.Acknowledgement;\n";
        code += "import salsa_lite.runtime.SynchronousMailboxStage;\n";
        code += "import salsa_lite.runtime.Actor;\n";
        code += "import salsa_lite.runtime.Message;\n";
        code += "import salsa_lite.runtime.RemoteActor;\n";
        code += "import salsa_lite.runtime.MobileActor;\n";
        code += "import salsa_lite.runtime.StageService;\n";
        code += "import salsa_lite.runtime.TransportService;\n";

        if (module_string == null || !module_string.equals("salsa_lite.runtime.language.")) {
            code += "import salsa_lite.runtime.language.Director;\n";
            code += "import salsa_lite.runtime.language.JoinDirector;\n";
            code += "import salsa_lite.runtime.language.MessageDirector;\n";
            code += "import salsa_lite.runtime.language.ContinuationDirector;\n";
            code += "import salsa_lite.runtime.language.TokenDirector;\n";
        }
        code += "\n";
        code += "import salsa_lite.runtime.language.exceptions.RemoteMessageException;\n";
        code += "import salsa_lite.runtime.language.exceptions.TokenPassException;\n";
        code += "import salsa_lite.runtime.language.exceptions.MessageHandlerNotFoundException;\n";
        code += "import salsa_lite.runtime.language.exceptions.ConstructorNotFoundException;\n";
        code += "\n";

        if (isMobile()) {
            code += "import salsa_lite.runtime.wwc.OutgoingTheaterConnection;\n";
            code += "\n";
        }

		code += "/****** END SALSA LANGUAGE IMPORTS ******/\n";
		code += "\n";

		if (java_statement != null) code += java_statement.toJavaCode() + "\n";

		code += getImportDeclarationCode() + "\n";

        String actor_name = getName().name;
        try {
//            if (module_string == null) {
//                SymbolTable.importActor(actor_name);
//            } else {
//                SymbolTable.importActor(module_string + "." + actor_name);
//            }
            SymbolTable.addVariableType("self", actor_name, false, false);
        } catch (VariableDeclarationException vde) {
            CompilerErrors.printErrorMessage("[CCompilationUnit.getImportDeclarationCode] Could not declare variable. " + vde.toString(), behavior_declaration);
            throw new RuntimeException(vde);
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("VERY BAD ERROR. Could not get type for actor being compiled. This should never happen.", behavior_declaration);
            throw new RuntimeException(snfe);
        }
        if (behavior_declaration != null) {
            if (behavior_declaration.is_abstract) {
                code += "abstract public class " + actor_name;
            } else {
                code += "public class " + actor_name;
            }
        } else {
            code += "public interface " + actor_name;
        }

        if (getExtendsName() != null) {
            String extendsName = getExtendsName().name;
            code += " extends " + extendsName;
        }

		String implementsNames = null;
        if (behavior_declaration != null) implementsNames = behavior_declaration.getImplementsNames();
        else if (interface_declaration.extends_names.size() > 0) implementsNames = interface_declaration.getImplementsNames();

		if (implementsNames != null && !implementsNames.equals("")) {
            code += " implements " + implementsNames;
            
            if (behavior_declaration != null && !behavior_declaration.is_abstract) {
                code += ", java.io.Serializable";
            }
        }
        else {
            code += " implements java.io.Serializable";
        }

		code += " {\n";
		CIndent.increaseIndent();

        if (behavior_declaration != null && !behavior_declaration.is_abstract) {
            String tmp_name = actor_name;
            if (tmp_name.contains("<")) tmp_name = tmp_name.substring(0, tmp_name.indexOf('<'));

            code += "\n";

            if (isMobile()) {
                /**
                 *  Remote and Mobile actors should already be in the ActorRegistry
                 */
                code += CIndent.getIndent() + "public Object writeReplace() throws java.io.ObjectStreamException {\n";
                code += CIndent.getIndent() + "    return new Serialized" + tmp_name + "( this.getName(), this.getNameServer(), this.getLastKnownHost(), this.getLastKnownPort());\n";
                code += CIndent.getIndent() + "}\n\n";

                code += CIndent.getIndent() + "public static class Serialized" + tmp_name + " implements java.io.Serializable {\n";
                code += CIndent.getIndent() + "    String name;\n";
                code += CIndent.getIndent() + "    String lastKnownHost;\n";
                code += CIndent.getIndent() + "    int lastKnownPort;\n\n";
                code += CIndent.getIndent() + "    NameServer nameserver;\n\n";

                code += CIndent.getIndent() + "    Serialized" + tmp_name + "(String name, NameServer nameserver, String lastKnownHost, int lastKnownPort) { this.name = name; this.nameserver = nameserver; this.lastKnownHost = lastKnownHost; this.lastKnownPort = lastKnownPort; }\n\n";

                code += CIndent.getIndent() + "    public Object readResolve() throws java.io.ObjectStreamException {\n";
                code += CIndent.getIndent() + "        int hashCode = Hashing.getHashCodeFor(name, nameserver.getName(), nameserver.getHost(), nameserver.getPort());\n\n";

                 code += CIndent.getIndent() + "            synchronized (MobileActorRegistry.getStateLock(hashCode)) {\n";
                 code += CIndent.getIndent() + "                Actor entry = MobileActorRegistry.getStateEntry(hashCode);\n";
                 code += CIndent.getIndent() + "                if (entry == null) {\n";
                 code += CIndent.getIndent() + "                    MobileActorRegistry.addStateEntry(hashCode, TransportService.getSocket(lastKnownHost, lastKnownPort));\n";
                 code += CIndent.getIndent() + "                }\n";
                 code += CIndent.getIndent() + "            }\n\n";


                code += CIndent.getIndent() + "        synchronized (MobileActorRegistry.getReferenceLock(hashCode)) {\n";
                code += CIndent.getIndent() + "            " + tmp_name + " actor = (" + tmp_name +")MobileActorRegistry.getReferenceEntry(hashCode);\n";
//                code += CIndent.getIndent() + "            System.err.println(\"DESERIALIZING A REFERENCE TO A MOBILE ACTOR: \" + hashCode + \" -- \" + lastKnownHost + \":\" + lastKnownPort + \"/\" + name + \" -- got: \" + actor + \" -- type: " + tmp_name + "\");\n";
                code += CIndent.getIndent() + "            if (actor == null) {\n";
                code += CIndent.getIndent() + "                " + tmp_name + " remoteReference = new " + tmp_name + "(name, nameserver, lastKnownHost, lastKnownPort);\n";
                code += CIndent.getIndent() + "                MobileActorRegistry.addReferenceEntry(hashCode, remoteReference);\n";
                code += CIndent.getIndent() + "                return remoteReference;\n";
                code += CIndent.getIndent() + "            } else {\n";
                code += CIndent.getIndent() + "                return actor;\n";
                code += CIndent.getIndent() + "            }\n";
                code += CIndent.getIndent() + "        }\n";
                code += CIndent.getIndent() + "    }\n";
                code += CIndent.getIndent() + "}\n";
                code += "\n";


            } else if (isRemote()) {
                code += CIndent.getIndent() + "public static " + tmp_name + " getRemoteReference(String name, String host, int port) {\n";
                code += CIndent.getIndent() + "    if (host.equals(\"127.0.0.1\") || host.equals(\"localhost\")) host = TransportService.getHost();\n";
                code += CIndent.getIndent() + "    int hashCode = Hashing.getHashCodeFor(name, host, port);\n";
                code += CIndent.getIndent() + "    synchronized (RemoteActorRegistry.getLock(hashCode)) {\n";
                code += CIndent.getIndent() + "        " + tmp_name + " entry = (" + tmp_name + ")RemoteActorRegistry.getEntry(hashCode);\n";
                code += CIndent.getIndent() + "        if (entry == null) {\n";
                code += CIndent.getIndent() + "            RemoteReference reference = new RemoteReference(name, host, port);\n";
                code += CIndent.getIndent() + "            RemoteActorRegistry.addEntry(hashCode, reference);\n";
                code += CIndent.getIndent() + "            return reference;\n";
                code += CIndent.getIndent() + "        } else {\n";
                code += CIndent.getIndent() + "            return entry;\n";
                code += CIndent.getIndent() + "        }\n";
                code += CIndent.getIndent() + "    }\n";
                code += CIndent.getIndent() + "}\n";
                code += "\n";

                /**
                 *  Remote and Mobile actors should already be in the ActorRegistry
                 */
                code += CIndent.getIndent() + "public Object writeReplace() throws java.io.ObjectStreamException {\n";
                code += CIndent.getIndent() + "    return new Serialized" + tmp_name + "( this.getName(), this.getHost(), this.getPort() );\n";
                code += CIndent.getIndent() + "}\n\n";

                code += CIndent.getIndent() + "public static class RemoteCreationStub implements java.io.Serializable {\n";
                code += CIndent.getIndent() + "    String name;\n";
                code += CIndent.getIndent() + "    String host;\n";
                code += CIndent.getIndent() + "    int port;\n";
                code += CIndent.getIndent() + "    int target_stage_id;\n";
                code += "\n";
                code += CIndent.getIndent() + "    RemoteCreationStub(String name, String host, int port) { this.name = name; this.host = host; this.port = port; }\n";
                code += CIndent.getIndent() + "    RemoteCreationStub(String name, String host, int port, int target_stage_id) { this.name = name; this.host = host; this.port = port; this.target_stage_id = target_stage_id; }\n";
                code += "\n";
                code += CIndent.getIndent() + "    public Object readResolve() throws java.io.ObjectStreamException {\n";
                code += CIndent.getIndent() + "        if (target_stage_id < 0) return new " + tmp_name + "(name, -1 /*-1 means a new stage*/);\n";
                code += CIndent.getIndent() + "        else return new " + tmp_name + "(name, target_stage_id);\n";
                code += CIndent.getIndent() + "    }\n";
                code += CIndent.getIndent() + "}\n\n";

                code += CIndent.getIndent() + "public static class RemoteReference extends " + tmp_name + " {\n";

                code += CIndent.getIndent() + "    RemoteReference(String name, String host, int port) { super(name, host, port); }\n";
                code += "\n";

                code += CIndent.getIndent() + "    public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {\n";
                code += CIndent.getIndent() + "        TransportService.sendMessageToRemote(getHost(), getPort(), this.getStage().message);\n";
                code += CIndent.getIndent() + "        throw new RemoteMessageException();\n";
                code += CIndent.getIndent() + "    }\n";
                code += "\n";

                code += CIndent.getIndent() + "    public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {\n";
                code += CIndent.getIndent() + "        TransportService.sendMessageToRemote(getHost(), getPort(), this.getStage().message);\n";
                code += CIndent.getIndent() + "        throw new RemoteMessageException();\n";
                code += CIndent.getIndent() + "    }\n";
                code += "\n";

                code += CIndent.getIndent() + "    public Object writeReplace() throws java.io.ObjectStreamException {\n";
                code += CIndent.getIndent() + "        return new Serialized" + tmp_name + "( getName(), getHost(), getPort() );\n";
                code += CIndent.getIndent() + "    }\n";
                code += CIndent.getIndent() + "}\n\n";

                code += CIndent.getIndent() + "public static class Serialized" + tmp_name + " implements java.io.Serializable {\n";
                code += CIndent.getIndent() + "    String name;\n";
                code += CIndent.getIndent() + "    String host;\n";
                code += CIndent.getIndent() + "    int port;\n";
                code += "\n";

                code += CIndent.getIndent() + "    Serialized" + tmp_name + "(String name, String host, int port) { this.name = name; this.host = host; this.port = port; }\n";
                code += "\n";

                code += CIndent.getIndent() + "    public Object readResolve() throws java.io.ObjectStreamException {\n";
                code += CIndent.getIndent() + "        int hashCode = Hashing.getHashCodeFor(name, host, port);\n";
                code += CIndent.getIndent() + "        synchronized (RemoteActorRegistry.getLock(hashCode)) {\n";
                code += CIndent.getIndent() + "            " + tmp_name + " actor = (" + tmp_name +")RemoteActorRegistry.getEntry(hashCode);\n";
//                code += CIndent.getIndent() + "            System.err.println(\"DESERIALIZING A REFERENCE TO A REMOTE ACTOR: \" + hashCode + \" -- \" + host + \":\" + port + \"/\" + name + \" -- got: \" + actor + \" -- type: " + tmp_name + "\");\n";
                code += CIndent.getIndent() + "            if (actor == null) {\n";
                code += CIndent.getIndent() + "                RemoteReference remoteReference = new RemoteReference(name, host, port);\n";
                code += CIndent.getIndent() + "                RemoteActorRegistry.addEntry(hashCode, remoteReference);\n";
                code += CIndent.getIndent() + "                return remoteReference;\n";
                code += CIndent.getIndent() + "            } else {\n";
                code += CIndent.getIndent() + "                return actor;\n";
                code += CIndent.getIndent() + "            }\n";
                code += CIndent.getIndent() + "        }\n";
                code += CIndent.getIndent() + "    }\n";
                code += CIndent.getIndent() + "}\n";
                code += "\n";

            } else {        //this is a local actor
                code += "\n";
                //theres got to be a better way to do this so we don't need to know the local host and port when serializing a local actor locally
                //Maybe a way to find out the stream the actor is being written over.  If it is the local fast deepcopy stream
                //use a different registry.
                code += CIndent.getIndent() + "public Object writeReplace() throws java.io.ObjectStreamException {\n";
                code += CIndent.getIndent() + "    int hashCode = Hashing.getHashCodeFor(this.hashCode(), TransportService.getHost(), TransportService.getPort());\n";
                code += CIndent.getIndent() + "    synchronized (LocalActorRegistry.getLock(hashCode)) {\n";
                code += CIndent.getIndent() + "        LocalActorRegistry.addEntry(hashCode, this);\n";
                code += CIndent.getIndent() + "    }\n";
                code += CIndent.getIndent() + "    return new Serialized" + tmp_name + "( this.hashCode(), TransportService.getHost(), TransportService.getPort() );\n";
                code += CIndent.getIndent() + "}\n\n";

                code += CIndent.getIndent() + "public static class RemoteReference extends " + tmp_name + " {\n";
                code += CIndent.getIndent() + "    private int hashCode;\n";
                code += CIndent.getIndent() + "    private String host;\n";
                code += CIndent.getIndent() + "    private int port;\n";
                code += CIndent.getIndent() + "    RemoteReference(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }\n";
                code += "\n";

                code += CIndent.getIndent() + "    public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {\n";
                code += CIndent.getIndent() + "        TransportService.sendMessageToRemote(host, port, this.getStage().message);\n";
                code += CIndent.getIndent() + "        throw new RemoteMessageException();\n";
                code += CIndent.getIndent() + "    }\n";
                code += "\n";

                code += CIndent.getIndent() + "    public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {\n";
                code += CIndent.getIndent() + "        TransportService.sendMessageToRemote(host, port, this.getStage().message);\n";
                code += CIndent.getIndent() + "        throw new RemoteMessageException();\n";
                code += CIndent.getIndent() + "    }\n";
                code += "\n";

                code += CIndent.getIndent() + "    public Object writeReplace() throws java.io.ObjectStreamException {\n";
                code += CIndent.getIndent() + "        return new Serialized" + tmp_name + "( hashCode, host, port);\n";
                code += CIndent.getIndent() + "    }\n";
                code += CIndent.getIndent() + "}\n\n";

                code += CIndent.getIndent() + "public static class Serialized" + tmp_name + " implements java.io.Serializable {\n";
                code += CIndent.getIndent() + "    int hashCode;\n";
                code += CIndent.getIndent() + "    String host;\n";
                code += CIndent.getIndent() + "    int port;\n";
                code += "\n";

                code += CIndent.getIndent() + "    Serialized" + tmp_name + "(int hashCode, String host, int port) { this.hashCode = hashCode; this.host = host; this.port = port; }\n";
                code += "\n";

                code += CIndent.getIndent() + "    public Object readResolve() throws java.io.ObjectStreamException {\n";
                code += CIndent.getIndent() + "        int hashCode = Hashing.getHashCodeFor(this.hashCode, this.host, this.port);\n";
                code += CIndent.getIndent() + "        synchronized (LocalActorRegistry.getLock(hashCode)) {\n";
                code += CIndent.getIndent() + "            " + tmp_name + " actor = (" + tmp_name +")LocalActorRegistry.getEntry(hashCode);\n";
//                code += CIndent.getIndent() + "            System.err.println(\"DESERIALIZING A REFERENCE TO A LOCAL ACTOR: \" + hashCode + \" -- \" + host + \":\" + port + \" -- got: \" + actor + \" -- type: " + tmp_name + "\");\n";
                code += CIndent.getIndent() + "            if (actor == null) {\n";
                code += CIndent.getIndent() + "                RemoteReference remoteReference = new RemoteReference(this.hashCode, this.host, this.port);\n";
                code += CIndent.getIndent() + "                LocalActorRegistry.addEntry(hashCode, remoteReference);\n";
                code += CIndent.getIndent() + "                return remoteReference;\n";
                code += CIndent.getIndent() + "            } else {\n";
                code += CIndent.getIndent() + "                return actor;\n";
                code += CIndent.getIndent() + "            }\n";
                code += CIndent.getIndent() + "        }\n";
                code += CIndent.getIndent() + "    }\n";
                code += CIndent.getIndent() + "}\n";
                code += "\n";
            }
        }

//		System.err.println("ATTEMPTING TO ADD CONTAINED MESSAGE HANDLERS");
		Vector<CMessageHandler> containedMessageHandlers = new Vector<CMessageHandler>();
		int i = 0;
        if (getConstructors() != null) {
            for (CConstructor constructor : getConstructors()) {
    //			System.err.println("C : " + (i++));
                constructor.addContainedMessageHandlers(containedMessageHandlers);
            }
        }

		i = 0;
		for (CMessageHandler message_handler : getMessageHandlers()) {
//			System.err.println("MH : " + (i++));
			message_handler.addContainedMessageHandlers(containedMessageHandlers);
		}
//		System.err.println("FOUND " + containedMessageHandlers.size() + " CONTAINED MESSAGE HANDLERS");

		TypeSymbol self_type = null;
        try {
            self_type = SymbolTable.getVariableType("self");
            if (!(self_type instanceof ActorType)) {
                System.err.println("VERY BAD COMPILER ERROR [CCompilationUnit.getStateCode]: Compiler thinks current actor is not an actor, thinks it is: " + self_type.getLongSignature());
            }
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("VERY BAD ERROR. Could not get type for actor being compiled. This should never happen.", behavior_declaration);
            throw new RuntimeException(snfe);
        }

        ActorType at = (ActorType)self_type;

		int number_original_messages = getMessageHandlers().size();
		for (i = 0; i < containedMessageHandlers.size(); i++) {
            MessageSymbol ms = new MessageSymbol(i + number_original_messages, self_type, containedMessageHandlers.get(i));
            at.message_handlers.add( ms );
		}

        if (isMobile()) {
            SymbolTable.is_mobile_actor = true;
            String tmp_name = actor_name;
            if (tmp_name.contains("<")) tmp_name = tmp_name.substring(0, tmp_name.indexOf('<'));

            code += CIndent.getIndent() + "public Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException {\n";
            code += CIndent.getIndent() + "    Object entry;\n";
            code += CIndent.getIndent() + "    int hashCode = hashCode();\n";
            code += CIndent.getIndent() + "    synchronized (MobileActorRegistry.getStateLock(hashCode)) {\n";
            code += CIndent.getIndent() + "        entry = MobileActorRegistry.getStateEntry(hashCode);\n";
            code += CIndent.getIndent() + "    }\n";
            code += CIndent.getIndent() + "    if (entry instanceof State) {\n";
            code += CIndent.getIndent() + "        return ((State)entry).invokeMessage(messageId, arguments);\n";
            code += CIndent.getIndent() + "    } else {\n";
            code += CIndent.getIndent() + "        StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, ((OutgoingTheaterConnection)entry), 2 /*send*/, new Object[]{this.getStage().message}));\n";
            code += CIndent.getIndent() + "        throw new RemoteMessageException();\n";
            code += CIndent.getIndent() + "    }\n";
            code += CIndent.getIndent() + "}\n";
            code += "\n";
            code += CIndent.getIndent() + "public void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, ConstructorNotFoundException {\n";
            code += CIndent.getIndent() + "    Object entry;\n";
            code += CIndent.getIndent() + "    int hashCode = hashCode();\n";
            code += CIndent.getIndent() + "    synchronized (MobileActorRegistry.getStateLock(hashCode)) {\n";
            code += CIndent.getIndent() + "        entry = MobileActorRegistry.getStateEntry(hashCode);\n";
            code += CIndent.getIndent() + "    }\n";
            code += CIndent.getIndent() + "    if (entry instanceof State) {\n";
            code += CIndent.getIndent() + "        ((State)entry).invokeConstructor(messageId, arguments);\n";
            code += CIndent.getIndent() + "    } else {\n";
            code += CIndent.getIndent() + "        StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, ((OutgoingTheaterConnection)entry), 2 /*send*/, new Object[]{this.getStage().message}));\n";
            code += CIndent.getIndent() + "        throw new RemoteMessageException();\n";
            code += CIndent.getIndent() + "    }\n";
            code += CIndent.getIndent() + "}\n\n\n";

            if (!isStaged()) {
                code += CIndent.getIndent() + "public " + tmp_name + "(String name, NameServer nameserver) { super(name, nameserver); }\n";
            } else {
                code += CIndent.getIndent() + "public " + tmp_name + "() { super(-1 /*-1 means new stage*/); }\n";
            }
            code += CIndent.getIndent() + "public " + tmp_name + "(String name, NameServer nameserver, int stage_id) { super(name, nameserver, stage_id); }\n";
            code += CIndent.getIndent() + "public " + tmp_name + "(String name, NameServer nameserver, String lastKnownHost, int lastKnownPort) { super(name, nameserver, lastKnownHost, lastKnownPort); }\n\n";
            code += CIndent.getIndent() + "public " + tmp_name + "(String name, NameServer nameserver, String lastKnownHost, int lastKnownPort, int stage_id) { super(name, nameserver, lastKnownHost, lastKnownPort, stage_id); }\n\n";

            int act_constructor = behavior_declaration.getActConstructor();
            if (act_constructor >= 0) {
                code += CIndent.getIndent() + "public static void main(String[] arguments) {\n";
                code += CIndent.getIndent() + "    TransportService.initialize();\n";
                code += CIndent.getIndent() + "    String name = System.getProperty(\"called\");\n";
                code += CIndent.getIndent() + "    String nameserver_info = System.getProperty(\"using\");\n";
                code += CIndent.getIndent() + "    if (name == null || nameserver_info == null) {\n";
                code += CIndent.getIndent() + "        System.err.println(\"Error starting " + tmp_name + ": to run a mobile actor you must specify a name with the '-Dcalled=<name>' system property and a namesever with the '-Dusing=\\\"<nameserver_host>:<nameserver_port>/<nameserver_name>\\\"' system property.\");\n";
                code += CIndent.getIndent() + "        System.err.println(\"usage: (port is optional and 4040 by default)\");\n";
                if (getModule() == null) {
                    code += CIndent.getIndent() + "        System.err.println(\"\tjava -Dcalled=\\\"<name>\\\" -Dusing=\\\"nameserver_host:nameserver_port/nameserver_name\\\" [-Dport=4040] " + tmp_name + "\");\n";
                } else {
                    code += CIndent.getIndent() + "        System.err.println(\"\tjava -Dcalled=\\\"<name>\\\" [-Dport=4040] " + getModule() + "." + tmp_name + "\");\n";
                }
                code += CIndent.getIndent() + "        System.exit(0);\n";
                code += CIndent.getIndent() + "    }\n";
                code += CIndent.getIndent() + "    try {\n";
                code += CIndent.getIndent() + "        int colon_index = nameserver_info.indexOf(':');\n";
                code += CIndent.getIndent() + "        int slash_index = nameserver_info.indexOf('/');\n";
                code += CIndent.getIndent() + "        String nameserver_host = nameserver_info.substring(0,colon_index);\n";
                code += CIndent.getIndent() + "        int nameserver_port = Integer.parseInt(nameserver_info.substring(colon_index + 1, slash_index));\n";
                code += CIndent.getIndent() + "        String nameserver_name = nameserver_info.substring(slash_index + 1, nameserver_info.length());\n";
                code += CIndent.getIndent() + "        " + tmp_name + ".construct(" + act_constructor + ", new Object[]{arguments}, name, NameServer.getRemoteReference(nameserver_name, nameserver_host, nameserver_port));\n";
                code += CIndent.getIndent() + "    } catch (Exception e) {\n";
                code += CIndent.getIndent() + "        System.err.println(\"Error in format of -Dusing system property, needs to be 'nameserver_host:nameserver_port/nameserver_name'.\");\n";
                code += CIndent.getIndent() + "        e.printStackTrace();\n";
                code += CIndent.getIndent() + "        System.exit(0);\n";
                code += CIndent.getIndent() + "    }\n";
                code += CIndent.getIndent() + "}\n\n";
            }

            code += CIndent.getIndent() + "public static " + tmp_name + " construct(int constructor_id, Object[] arguments, String name, NameServer nameserver) {\n";
            code += CIndent.getIndent() + "    " + tmp_name + " actor = new " + tmp_name + "(name, nameserver);\n";
            code += CIndent.getIndent() + "    State state = new State(name, nameserver, actor.getStageId());\n\n";
            code += CIndent.getIndent() + "    StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, nameserver, 4 /*put*/, new Object[]{actor})); //register the actor with the name server. \n\n";
            code += CIndent.getIndent() + "    StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));\n";
            code += CIndent.getIndent() + "    return actor;\n";
            code += CIndent.getIndent() + "}\n\n";

            code += CIndent.getIndent() + "public static " + tmp_name + " construct(int constructor_id, Object[] arguments, String name, NameServer nameserver, int target_stage_id) {\n";
            code += CIndent.getIndent() + "    " + tmp_name + " actor = new " + tmp_name + "(name, nameserver, target_stage_id);\n";
            code += CIndent.getIndent() + "    State state = new State(name, nameserver, target_stage_id);\n\n";
            code += CIndent.getIndent() + "    StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, nameserver, 4 /*put*/, new Object[]{actor})); //register the actor with the name server. \n\n";
            code += CIndent.getIndent() + "    actor.getStage().putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));\n";
            code += CIndent.getIndent() + "    return actor;\n";
            code += CIndent.getIndent() + "}\n\n\n";

            code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, String name, NameServer nameserver) {\n";
            code += CIndent.getIndent() + "    " + tmp_name + " actor = new " + tmp_name + "(name, nameserver);\n";
            code += CIndent.getIndent() + "    State state = new State(name, nameserver, actor.getStageId());\n\n";
            code += CIndent.getIndent() + "    StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, nameserver, 4 /*put*/, new Object[]{actor})); //register the actor with the name server. \n\n";
            code += CIndent.getIndent() + "    TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);\n";
            code += CIndent.getIndent() + "    Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);\n";
            code += CIndent.getIndent() + "    MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});\n";
            code += CIndent.getIndent() + "    return output_continuation;\n";
            code += CIndent.getIndent() + "}\n\n";

            code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, String name, NameServer nameserver, int target_stage_id) {\n";
            code += CIndent.getIndent() + "    " + tmp_name + " actor = new " + tmp_name + "(name, nameserver, target_stage_id);\n";
            code += CIndent.getIndent() + "    State state = new State(name, nameserver, target_stage_id);\n\n";
            code += CIndent.getIndent() + "    StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, nameserver, 4 /*put*/, new Object[]{actor})); //register the actor with the name server. \n\n";
            code += CIndent.getIndent() + "    TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage_id);\n";
            code += CIndent.getIndent() + "    Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);\n";
            code += CIndent.getIndent() + "    MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage_id);\n";
            code += CIndent.getIndent() + "    return output_continuation;\n";
            code += CIndent.getIndent() + "}\n\n";

            //remote creation construct methods
            //need one for host port and no tokens, one for host port stage and no tokens, one for host port and tokens and one for host port stage and tokens
            code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, String name, NameServer nameserver, String host, int port) {\n";
            code += CIndent.getIndent() + "    " + tmp_name + " actor = new " + tmp_name + "(name, nameserver, host, port);\n";
            code += CIndent.getIndent() + "    State state = new State(name, nameserver, host, port, actor.getStageId());\n\n";
            code += CIndent.getIndent() + "    if (! (host.equals(TransportService.getHost()) && port == TransportService.getPort()) ) {\n";
            code += CIndent.getIndent() + "        synchronized (MobileActorRegistry.getStateLock(actor.hashCode())) {\n";
            code += CIndent.getIndent() + "            MobileActorRegistry.updateStateEntry(actor.hashCode(), TransportService.getSocket(host, port));\n";
            code += CIndent.getIndent() + "        }\n";
            code += CIndent.getIndent() + "        TransportService.migrateActor(host, port, state);\n";
            code += CIndent.getIndent() + "    }\n\n";
            code += CIndent.getIndent() + "    StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, nameserver, 4 /*put*/, new Object[]{actor})); //register the actor with the name server. \n\n";
            code += CIndent.getIndent() + "    TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);\n";
            code += CIndent.getIndent() + "    StageService.sendMessage(new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation));\n";
            code += CIndent.getIndent() + "    return output_continuation;\n";
            code += CIndent.getIndent() + "}\n\n";

            code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, String name, NameServer nameserver, String host, int port, int target_stage_id) {\n";
            code += CIndent.getIndent() + "    " + tmp_name + " actor = new " + tmp_name + "(name, nameserver, host, port, target_stage_id);\n";
            code += CIndent.getIndent() + "    State state = new State(name, nameserver, host, port, target_stage_id);\n\n";
            code += CIndent.getIndent() + "    if (! (host.equals(TransportService.getHost()) && port == TransportService.getPort()) ) {\n";
            code += CIndent.getIndent() + "        synchronized (MobileActorRegistry.getStateLock(actor.hashCode())) {\n";
            code += CIndent.getIndent() + "            MobileActorRegistry.updateStateEntry(actor.hashCode(), TransportService.getSocket(host, port));\n";
            code += CIndent.getIndent() + "        }\n";
            code += CIndent.getIndent() + "        TransportService.migrateActor(host, port, state);\n";
            code += CIndent.getIndent() + "    }\n\n";
            code += CIndent.getIndent() + "    StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, nameserver, 4 /*put*/, new Object[]{actor})); //register the actor with the name server. \n\n";
            code += CIndent.getIndent() + "    TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);\n";
            code += CIndent.getIndent() + "    StageService.sendMessage(new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation));\n";
            code += CIndent.getIndent() + "    return output_continuation;\n";
            code += CIndent.getIndent() + "}\n\n";

            code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, String name, NameServer nameserver, String host, int port) {\n";
            code += CIndent.getIndent() + "    " + tmp_name + " actor = new " + tmp_name + "(name, nameserver, host, port);\n";
            code += CIndent.getIndent() + "    State state = new State(name, nameserver, host, port, actor.getStageId());\n\n";
            code += CIndent.getIndent() + "    if (! (host.equals(TransportService.getHost()) && port == TransportService.getPort()) ) {\n";
            code += CIndent.getIndent() + "        synchronized (MobileActorRegistry.getStateLock(actor.hashCode())) {\n";
            code += CIndent.getIndent() + "            MobileActorRegistry.updateStateEntry(actor.hashCode(), TransportService.getSocket(host, port));\n";
            code += CIndent.getIndent() + "        }\n";
            code += CIndent.getIndent() + "        TransportService.migrateActor(host, port, state);\n";
            code += CIndent.getIndent() + "    }\n\n";
            code += CIndent.getIndent() + "    StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, nameserver, 4 /*put*/, new Object[]{actor})); //register the actor with the name server. \n\n";
            code += CIndent.getIndent() + "    TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);\n";
            code += CIndent.getIndent() + "    Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);\n";
            code += CIndent.getIndent() + "    MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});\n";
            code += CIndent.getIndent() + "    return output_continuation;\n";
            code += CIndent.getIndent() + "}\n\n";

            code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, String name, NameServer nameserver, String host, int port, int target_stage_id) {\n";
            code += CIndent.getIndent() + "    " + tmp_name + " actor = new " + tmp_name + "(name, nameserver, host, port, target_stage_id);\n";
            code += CIndent.getIndent() + "    State state = new State(name, nameserver, host, port, target_stage_id);\n\n";
            code += CIndent.getIndent() + "    if (! (host.equals(TransportService.getHost()) && port == TransportService.getPort()) ) {\n";
            code += CIndent.getIndent() + "        synchronized (MobileActorRegistry.getStateLock(actor.hashCode())) {\n";
            code += CIndent.getIndent() + "            MobileActorRegistry.updateStateEntry(actor.hashCode(), TransportService.getSocket(host, port));\n";
            code += CIndent.getIndent() + "        }\n";
            code += CIndent.getIndent() + "        TransportService.migrateActor(host, port, state);\n";
            code += CIndent.getIndent() + "    }\n\n";
            code += CIndent.getIndent() + "    StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, nameserver, 4 /*put*/, new Object[]{actor})); //register the actor with the name server. \n\n";
            code += CIndent.getIndent() + "    TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage_id);\n";
            code += CIndent.getIndent() + "    Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);\n";
            code += CIndent.getIndent() + "    MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage_id);\n";
            code += CIndent.getIndent() + "    return output_continuation;\n";
            code += CIndent.getIndent() + "}\n\n\n";


            code += CIndent.getIndent() + "public static class State ";
            if (getExtendsName() != null) code += "extends " + getExtendsName().name + ".State ";
            code += "{\n";

            CIndent.increaseIndent();
        }

        if (actor_name.contains("<")) actor_name = actor_name.substring(0, actor_name.indexOf('<'));

        if (isMobile()) {
            if (!isStaged()) {
                code += CIndent.getIndent() + "public State(String name, NameServer nameserver) { super(name, nameserver); }\n";
            } else {
                code += CIndent.getIndent() + "public State(String name, NameServer nameserver) { super(name, nameserver, -1 /*-1 means new stage*/); }\n";
            }
            code += CIndent.getIndent() + "public State(String name, NameServer nameserver, int stage_id) { super(name, nameserver, stage_id); }\n\n";
            code += CIndent.getIndent() + "public State(String name, NameServer nameserver, String host, int port) { super(name, nameserver, host, port); }\n\n";
            code += CIndent.getIndent() + "public State(String name, NameServer nameserver, String host, int port, int stage_id) { super(name, nameserver, host, port, stage_id); }\n\n";
        } else if (isRemote()) {
            if (!isStaged()) {
                code += CIndent.getIndent() + "public " + actor_name + "(String name) { super(name); }\n";
            } else {
                code += CIndent.getIndent() + "public " + actor_name + "(String name) { super(name, -1 /*-1 means new stage*/); }\n";
            }
            code += CIndent.getIndent() + "public " + actor_name + "(String name, int stage_id) { super(name, stage_id); }\n";
            code += CIndent.getIndent() + "private " + actor_name + "(String name, String host, int port) { super(name, host, port); }\n\n";
        } else {
            if (!isStaged()) {
                code += CIndent.getIndent() + "public " + actor_name + "() { super(); }\n";
            } else {
                code += CIndent.getIndent() + "public " + actor_name + "() { super(-1 /*-1 means new stage*/); }\n";
            }
            code += CIndent.getIndent() + "public " + actor_name + "(int stage_id) { super(stage_id); }\n\n";
        }

        if (isMobile()) {
            code += CIndent.getIndent() + "public void migrate(String host, int port) {\n";
            code += CIndent.getIndent() + "    if (! (host.equals(TransportService.getHost()) && port == TransportService.getPort()) ) {\n";
            code += CIndent.getIndent() + "        synchronized (MobileActorRegistry.getStateLock(this.hashCode())) {\n";
            code += CIndent.getIndent() + "            MobileActorRegistry.updateStateEntry(this.hashCode(), TransportService.getSocket(host, port));\n";
            code += CIndent.getIndent() + "        }\n";
            code += CIndent.getIndent() + "        TransportService.migrateActor(host, port, this);\n";
            code += CIndent.getIndent() + "    }\n";
            code += CIndent.getIndent() + "}\n\n";
        }

		code += getFieldCode() + "\n";
		code += getEnumerationCode() + "\n";

        if (behavior_declaration != null) {
            code += getInvokeMessageCode() + "\n";
            if (!behavior_declaration.is_abstract) {
                code += getInvokeConstructorCode() + "\n";
            }
        }

        if (behavior_declaration != null && !behavior_declaration.is_abstract) {
    		code += getConstructorCode() + "\n";
        }
		code += getMessageCode() + "\n";

        if (behavior_declaration != null && !behavior_declaration.is_abstract) {
            if (!isMobile()) {
                int act_constructor = behavior_declaration.getActConstructor();
                if (act_constructor >= 0) {
                    code += CIndent.getIndent() + "public static void main(String[] arguments) {\n";
                    if (isRemote()) {
                        code += CIndent.getIndent() + "    TransportService.initialize();\n";
                        code += CIndent.getIndent() + "    String name = System.getProperty(\"called\");\n";
                        code += CIndent.getIndent() + "    if (name == null) {\n";
                        code += CIndent.getIndent() + "        System.err.println(\"Error starting " + actor_name + ": to run a remote actor you must specify a name with the '-Dcalled=<name>' system property.\");\n";
                        code += CIndent.getIndent() + "        System.err.println(\"usage: (port is optional and 4040 by default)\");\n";
                        if (getModule() != null) {
                            code += CIndent.getIndent() + "        System.err.println(\"\tjava -Dcalled=\\\"<name>\\\" [-Dport=4040] " + getModule() + "." + actor_name + "\");\n";
                        } else {
                            code += CIndent.getIndent() + "        System.err.println(\"\tjava -Dcalled=\\\"<name>\\\" [-Dport=4040] " + actor_name + "\");\n";
                        }
                        code += CIndent.getIndent() + "        System.exit(0);\n";
                        code += CIndent.getIndent() + "    }\n";
                        code += CIndent.getIndent() + "    " + actor_name + ".construct(" + act_constructor + ", new Object[]{arguments}, name);\n";
                    } else {
                        code += CIndent.getIndent() + "    " + actor_name + ".construct(" + act_constructor + ", new Object[]{arguments});\n";
                    }
                    code += CIndent.getIndent() + "}\n\n";
                }


                if (isRemote()) {
                    code += CIndent.getIndent() + "public static " + actor_name + " construct(int constructor_id, Object[] arguments, String name) {\n";
                    code += CIndent.getIndent() + "    " + actor_name + " actor = new " + actor_name + "(name);\n";
                } else {
                    code += CIndent.getIndent() + "public static " + actor_name + " construct(int constructor_id, Object[] arguments) {\n";
                    code += CIndent.getIndent() + "    " + actor_name + " actor = new " + actor_name + "();\n";
                }
                code += CIndent.getIndent() + "    StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));\n";
                code += CIndent.getIndent() + "    return actor;\n";
                code += CIndent.getIndent() + "}\n\n";

                if (isRemote()) {
                    code += CIndent.getIndent() + "public static " + actor_name + " construct(int constructor_id, Object[] arguments, String name, int target_stage_id) {\n";
                    code += CIndent.getIndent() + "    " + actor_name + " actor = new " + actor_name + "(name, target_stage_id);\n";
                } else {
                    code += CIndent.getIndent() + "public static " + actor_name + " construct(int constructor_id, Object[] arguments, int target_stage_id) {\n";
                    code += CIndent.getIndent() + "    " + actor_name + " actor = new " + actor_name + "(target_stage_id);\n";
                }
                code += CIndent.getIndent() + "    actor.getStage().putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));\n";
                code += CIndent.getIndent() + "    return actor;\n";
                code += CIndent.getIndent() + "}\n\n";
 
                if (isRemote()) {
                    code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, String name) {\n";
                    code += CIndent.getIndent() + "    " + actor_name + " actor = new " + actor_name + "(name);\n";
                } else { //is local
                    code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {\n";
                    code += CIndent.getIndent() + "    " + actor_name + " actor = new " + actor_name + "();\n";
                }
                code += CIndent.getIndent() + "    TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);\n";
                code += CIndent.getIndent() + "    Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);\n";
                code += CIndent.getIndent() + "    MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});\n";
                code += CIndent.getIndent() + "    return output_continuation;\n";
                code += CIndent.getIndent() + "}\n\n";

                if (isRemote()) {
                    code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, String name, int target_stage_id) {\n";
                    code += CIndent.getIndent() + "    " + actor_name + " actor = new " + actor_name + "(name, target_stage_id);\n";
                } else {
                    code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, int target_stage_id) {\n";
                    code += CIndent.getIndent() + "    " + actor_name + " actor = new " + actor_name + "(target_stage_id);\n";
                }
                code += CIndent.getIndent() + "    TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage_id);\n";
                code += CIndent.getIndent() + "    Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);\n";
                code += CIndent.getIndent() + "    MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage_id);\n";
                code += CIndent.getIndent() + "    return output_continuation;\n";
                code += CIndent.getIndent() + "}\n\n";

                if (isRemote()) {
                    //remote creation construct methods
                    //need one for host port and no tokens, one for host port stage and no tokens, one for host port and tokens and one for host port stage and tokens
                    code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, String name, String host, int port) {\n";
                    code += CIndent.getIndent() + "    " + actor_name + " actor = " + actor_name + ".getRemoteReference(name, host, port);\n";
                    code += CIndent.getIndent() + "    TransportService.createRemotely(host, port, new " + actor_name + ".RemoteCreationStub(name, host, port));\n";  //may need to have this continue to the next message
                    code += CIndent.getIndent() + "    TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);\n";
                    code += CIndent.getIndent() + "    StageService.sendMessage(new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation));\n";
                    code += CIndent.getIndent() + "    return output_continuation;\n";
                    code += CIndent.getIndent() + "}\n\n";

                    code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, String name, String host, int port, int target_stage_id) {\n";
                    code += CIndent.getIndent() + "    " + actor_name + " actor = " + actor_name + ".getRemoteReference(name, host, port);\n";
                    code += CIndent.getIndent() + "    TransportService.createRemotely(host, port, new " + actor_name + ".RemoteCreationStub(name, host, port, target_stage_id));\n";  //may need to have this continue to the next message
                    code += CIndent.getIndent() + "    TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);\n";
                    code += CIndent.getIndent() + "    StageService.sendMessage(new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation));\n";
                    code += CIndent.getIndent() + "    return output_continuation;\n";
                    code += CIndent.getIndent() + "}\n\n";

                    code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, String name, String host, int port) {\n";
                    code += CIndent.getIndent() + "    " + actor_name + " actor = " + actor_name + ".getRemoteReference(name, host, port);\n";
                    code += CIndent.getIndent() + "    TransportService.createRemotely(host, port, new " + actor_name + ".RemoteCreationStub(name, host, port));\n";  //may need to have this continue to the next message
                    code += CIndent.getIndent() + "    TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);\n";
                    code += CIndent.getIndent() + "    Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);\n";
                    code += CIndent.getIndent() + "    MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions});\n";
                    code += CIndent.getIndent() + "    return output_continuation;\n";
                    code += CIndent.getIndent() + "}\n\n";

                    code += CIndent.getIndent() + "public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, String name, String host, int port, int target_stage_id) {\n";
                    code += CIndent.getIndent() + "    " + actor_name + " actor = " + actor_name + ".getRemoteReference(name, host, port);\n";
                    code += CIndent.getIndent() + "    TransportService.createRemotely(host, port, new " + actor_name + ".RemoteCreationStub(name, host, port, target_stage_id));\n";  //may need to have this continue to the next message
                    code += CIndent.getIndent() + "    TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage_id);\n";
                    code += CIndent.getIndent() + "    Message input_message = new Message(Message.CONSTRUCT_CONTINUATION_MESSAGE, actor, constructor_id, arguments, output_continuation);\n";
                    code += CIndent.getIndent() + "    MessageDirector md = MessageDirector.construct(3, new Object[]{input_message, arguments, token_positions}, target_stage_id);\n";
                    code += CIndent.getIndent() + "    return output_continuation;\n";
                    code += CIndent.getIndent() + "}\n\n\n";
                }
            }
       }

        if (isMobile()) {
            CIndent.decreaseIndent();
            code += CIndent.getIndent() + "}\n";
        }

		CIndent.decreaseIndent();
		code += CIndent.getIndent() + "}\n";

		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
