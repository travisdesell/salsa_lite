package salsa_lite.compiler.definitions;

import java.util.Vector;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.ActorType;
import salsa_lite.compiler.symbol_table.MessageSymbol;
import salsa_lite.compiler.symbol_table.TypeSymbol;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;
import salsa_lite.compiler.symbol_table.VariableDeclarationException;

public class CAllocation extends CVariableInit {

	public boolean remote_reference = false;

	public CName type;

	public int array_dimensions = 0;

	public Vector<CExpression> arguments;

	public Vector<CExpression> array_arguments;
	public CArrayInit array_init;

    public CExpression called_expression, nameserver_expression, host_expression, port_expression, stage_expression;

	public boolean isToken() {
		if (arguments != null) {
			for (int i = 0; i < arguments.size(); i++) {
				if (arguments.get(i).isToken()) return true;
			}
		} else if (array_arguments != null) {
			for (int i = 0; i < array_arguments.size(); i++) {
				if (array_arguments.get(i).isToken()) return true;
			}
		} else if (array_init != null) {
			return array_init.isToken();
		}

		if (nameserver_expression != null && nameserver_expression.isToken()) return true;
        if (remote_reference && nameserver_expression != null) return true;         //getting a remote reference to a MobileActor always returns a token
        if (!remote_reference && (host_expression != null || port_expression != null)) return true; //creating an actor remotely always returns a token
		if (host_expression != null && host_expression.isToken()) return true;
		if (port_expression != null && port_expression.isToken()) return true;

		return false;
	}

	public TypeSymbol getType() {
        try {
            String typeName = type.name;
            for (int i = 0; i < array_dimensions; i++) typeName += "[]";

            TypeSymbol t = SymbolTable.getTypeSymbol(typeName);
            return t;
        } catch (VariableDeclarationException vde) {
            CompilerErrors.printErrorMessage("[CAllocation.getType]: " + vde.toString(), type);
            throw new RuntimeException(vde);
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("[CAllocation.getType]: " + snfe.toString(), type);
            throw new RuntimeException(snfe);
        }
	}

	public String toJavaCode() {
		String code = "";


        try {
            if (arguments != null || remote_reference == true) {
                if (SymbolTable.isActor(type.name)) {

                    TypeSymbol actorType = SymbolTable.getTypeSymbol(type.name);

                    if (actorType.isSubclassOf(SymbolTable.getTypeSymbol("MobileActor"))) {
                        /*
                        if (called_expression == null) {
                            CompilerErrors.printErrorMessage("Cannot create a MobileActor without a name.  Use new " + type.name + "(...) called (<unique mobile actor name)", this);
                        }
                        */
                        /*
                        if (nameserver_expression == null) {
                            CompilerErrors.printErrorMessage("Cannot create a MobileActor without using a nameserver.  Use new " + type.name + "(...) called ('your_name') using (<nameserver or host, port>)", this);
                        }
                        */

                    } else if (actorType.isSubclassOf(SymbolTable.getTypeSymbol("RemoteActor"))) {
                        /*
                        if (called_expression == null) {
                            CompilerErrors.printErrorMessage("Cannot create a RemoteActor without a name.  Use new " + type.name + "(...) called (<unique mobile actor name)", this);
                        }
                        */

                    } else {
                        if (nameserver_expression != null) {
                            CompilerErrors.printErrorMessage("Cannot create a LocalActor using a nameserver.", this);
                        }
                        if (host_expression != null) {
                            CompilerErrors.printErrorMessage("Cannot create a LocalActor at a host.", host_expression);
                        }
                        if (port_expression != null) {
                            CompilerErrors.printErrorMessage("Cannot create a LocalActor at a port.", port_expression);
                        }
                    }

                    //cannot put a reference on a particular stage
                    if (remote_reference == true && stage_expression != null) {
                        CompilerErrors.printErrorMessage("Cannot specify the stage for a remote reference, that actor has already been created at a specific stage.", stage_expression);
                    }

                    if (remote_reference && actorType.isSubclassOf(SymbolTable.getTypeSymbol("MobileActor"))) {
                        //creating a reference to a mobile actor needs to do:
                        //StageService.sendTokenMessage((TokenDirector)(nameserver_expression), 5 /*get*/, new Object[]{(called_expression)})
                        //nameserver_expression must match NameServer and called_expression must match String

                        if (!nameserver_expression.getType().isSubclassOf(SymbolTable.getTypeSymbol("NameServer"))) {
                            CompilerErrors.printErrorMessage("Expression in the 'using' clause was not a NameServer. ", nameserver_expression);
                            throw new RuntimeException();
                        }
                        if (!called_expression.getType().isSubclassOf(SymbolTable.getTypeSymbol("String"))) {
                            CompilerErrors.printErrorMessage("Expression in the 'called' clause was not a String. ", called_expression);
                            throw new RuntimeException();
                        }

                        code += "StageService.sendTokenMessage((";
                        code += nameserver_expression.toJavaCode();
                        code += "), ";
                        
//                        code += "5 /*get*/";
                        ActorType at = (ActorType)nameserver_expression.getType();
                        MessageSymbol messageSymbol = null;
                        try {
                            Vector<CExpression> arguments = new Vector<CExpression>();
                            arguments.add( called_expression );

                            messageSymbol = at.getMessage("get", arguments, false);
                        } catch (VariableDeclarationException vde) {
                            CompilerErrors.printErrorMessage("Could not find matching message, message name: 'get(String)' for the nameserver in 'using' clause. (This should only happen if using a custom overloaded NameServer class). variable declaration exception: " + vde.toString(), nameserver_expression);
                            throw new RuntimeException(vde);
                        } catch (SalsaNotFoundException snfe) {
                            CompilerErrors.printErrorMessage("Could not find matching message, message name: 'get(String)' for the nameserver in 'using' clause. (This should only happen if using a custom overloaded NameServer class):\n\t" + snfe.toString(), nameserver_expression);
                            throw new RuntimeException(snfe);
                        }

                        code += messageSymbol.getId();
                        code += " /*get*/, ";


                        code += "new Object[]{(";      //need to get the method from the NameServer type
                        code += called_expression.toJavaCode();
                        code += ")})";

                    } else {
                        if (type.name.contains("<")) {
                            code += type.name.substring(0, type.name.indexOf("<"));
                        } else {
                            code += type.name;
                        }
                        if (remote_reference == true) {
                            code += ".getRemoteReference(";
                        } else {
                            code += ".construct(" + SymbolTable.getTypeSymbol(type.name).getConstructor(arguments).getId() + ", ";

                            String argument_code = "";
                            for (int i = 0; i < arguments.size(); i++) {
                                argument_code += arguments.get(i).toJavaCode();
                                if (i < arguments.size() - 1) argument_code += ", ";
                            }

                            if (arguments.size() == 0) {
                                code += "null";
                            } else {
                                code += "new Object[]{" + argument_code + "}";
                            }
                        }

                        if (called_expression != null) {
                            code += ", " + called_expression.toJavaCode();
                        } else {
                            if (actorType.isSubclassOf(SymbolTable.getTypeSymbol("MobileActor")) || actorType.isSubclassOf(SymbolTable.getTypeSymbol("RemoteActor"))) {
                                code += ", stage.getUniqueName()";
                            }
                        }

                        if (nameserver_expression != null) {
                            String ns_code = "(" + nameserver_expression.toJavaCode() + ")";
                            code += ", " + ns_code + ".getName(), " + ns_code + ".getHost(), " + ns_code + ".getPort()";
                        } else {
                            if (actorType.isSubclassOf(SymbolTable.getTypeSymbol("MobileActor"))) {
                                code += ", TransportService.getNameServer().getName()";
                                code += ", TransportService.getNameServer().getHost()";
                                code += ", TransportService.getNameServer().getPort()";
                            }
                        }

                        if (host_expression != null) {
                            code += ", " + host_expression.toJavaCode() + ", " + port_expression.toJavaCode();
                        }

                        if (stage_expression != null) {
                            if (stage_expression.getType().equals(SymbolTable.getTypeSymbol("Integer")) || stage_expression.getType().equals(SymbolTable.getTypeSymbol("int"))) {
                                code += ", " + stage_expression.toJavaCode();
                            } else if (stage_expression.getType().isSubclassOf(SymbolTable.getTypeSymbol("Stage"))) {
                                code += ", " + stage_expression.toJavaCode();
                            } else {
                                CompilerErrors.printErrorMessage("Cannot determine stage, expression must be an int or implement the Stage interface.", stage_expression);
                            }
                        }

                        code += ")";
                    }
                } else {
                    if (called_expression != null) {
                        CompilerErrors.printErrorMessage("Cannot specify a name for a non-actor", called_expression);
                    }
                    if (host_expression != null) {
                        CompilerErrors.printErrorMessage("Cannot specify a host for a non-actor", host_expression);
                    }
                    if (port_expression != null) {
                        CompilerErrors.printErrorMessage("Cannot specify a port for a non-actor", port_expression);
                    }
                    if (nameserver_expression != null) {
                        CompilerErrors.printErrorMessage("Cannot specify a nameserver for a non-actor", nameserver_expression);
                    }

                    code += "new " + type.name + "( ";
                    for (CExpression argument : arguments) {
                        code += argument.toJavaCode();

                        if (!argument.equals(arguments.lastElement())) code += ", ";
                    }
                    code += " )";
                }
            } else {
                //this is a new array
                code += "new " + type.name;

                for (int i = 0; i < array_dimensions; i++) {
                    code += "[";
                    if (array_arguments != null && array_arguments.get(i) != null) code += array_arguments.get(i).toJavaCode();
                    code += "]";
                }

                if (array_init != null) code += array_init.toJavaCode(type.name, isToken());
            }
        } catch (VariableDeclarationException vde) {
            CompilerErrors.printErrorMessage("[CAllocation.getType]: " + vde.toString(), type);
            throw new RuntimeException(vde);
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("[CAllocation.getType]: " + snfe.toString(), type);
            throw new RuntimeException(snfe);
        }

		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
