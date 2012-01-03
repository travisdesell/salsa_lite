package salsa_lite.compiler.definitions;

import java.util.LinkedList;
import java.util.Vector;

import salsa_lite.compiler.symbol_table.SymbolTable;
import salsa_lite.compiler.symbol_table.ActorType;
import salsa_lite.compiler.symbol_table.ArrayType;
import salsa_lite.compiler.symbol_table.ObjectType;
import salsa_lite.compiler.symbol_table.PrimitiveType;
import salsa_lite.compiler.symbol_table.MethodSymbol;
import salsa_lite.compiler.symbol_table.MessageSymbol;
import salsa_lite.compiler.symbol_table.TypeSymbol;
import salsa_lite.compiler.symbol_table.SalsaNotFoundException;
import salsa_lite.compiler.symbol_table.VariableDeclarationException;

public class CValue extends CErrorInformation {

	public CLiteral literal;

	public CType cast_type = null;
	public CVariableName variable_name;

	public CExpression expression;

	public CAllocation allocation;

    public LinkedList<CModification> modifications = new LinkedList<CModification>();

	public boolean isLiteral() { return literal != null; }
	public boolean isSelf() { return (literal != null && literal.value.equals("self")); }
	public boolean isParent() { return (literal != null && literal.value.equals("parent")); }
	public boolean isAllocation() { return allocation != null; }
	public boolean isExpression() { return expression != null; }

	public TypeSymbol getValueType() {
		TypeSymbol value_type;

        try {
            if (isSelf()) {
                value_type = SymbolTable.getVariableType(literal.value);
            } else if (isParent()) {
                if (modifications.size() > 0 && modifications.get(0) instanceof CMessageSend) {
                    value_type = SymbolTable.getVariableType("self");
                } else {
                    value_type = SymbolTable.getVariableType("self").getSuperType();
                }
            } else if (literal != null) {
                value_type = SymbolTable.getTypeSymbol(literal.type);
            } else if (variable_name != null) {
                value_type = SymbolTable.getVariableType(variable_name.name);
            } else if (expression != null) {
                value_type = expression.getType();
            } else if (allocation != null) {
                value_type = allocation.getType();
            } else {
                System.err.println("Could not get type for value.  Was not self, parent, literal, variable, expression or allocation (which are the only possible choices).");
                System.err.println("This should never happen.");
                throw new RuntimeException();
            }
        } catch (VariableDeclarationException vde) {
            CompilerErrors.printErrorMessage("[CValue.getValueType]: Could not declare variable. " + vde.toString(), this);
            throw new RuntimeException(vde);
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("[CValue.getValueType]: Could not determine type for variable. " + snfe.toString(), this);
            throw new RuntimeException(snfe);
        }

		return value_type;
	}

	public TypeSymbol getType() {
        try {
    		if (cast_type != null)  return SymbolTable.getTypeSymbol(cast_type.name);
        } catch (VariableDeclarationException vde) {
            CompilerErrors.printErrorMessage("[CValue.getType]: Could not declare variable. " + vde.toString(), this);
            throw new RuntimeException(vde);
        } catch (SalsaNotFoundException snfe) {
            CompilerErrors.printErrorMessage("[CValue.getType]: Could not determine type for cast. " + snfe.toString(), this);
            throw new RuntimeException(snfe);
        }

		TypeSymbol value_type = getValueType();

        boolean isParentMessageSend = false;
        if (isParent()) {
            isParentMessageSend = true;
        }

        for (CModification modification : modifications) {

            if (modification instanceof CArrayAccess) {
                CArrayAccess array_access = (CArrayAccess)modification;

                if (value_type instanceof ArrayType) {
                    value_type = ((ArrayType)value_type).getSubtype();
                } else {
                    CompilerErrors.printErrorMessage("Cannot perform array access on non-array", array_access);
                    throw new RuntimeException();
                }

                if (value_type == null) {
                    CompilerErrors.printErrorMessage("[CValue.getType]: Could not determine type for array access. ", array_access);
                    throw new RuntimeException();
                }

            } else if (modification instanceof CFieldAccess) {
                CFieldAccess field_access = (CFieldAccess)modification;

                if (value_type instanceof ObjectType) {
                    value_type = ((ObjectType)value_type).getFieldType(field_access.name);
                } else if (value_type instanceof ActorType) {
                    value_type = ((ActorType)value_type).getFieldType(field_access.name);
                } else if (value_type instanceof ArrayType) {
                    value_type = ((ArrayType)value_type).getFieldType(field_access.name);
                } else if (value_type instanceof PrimitiveType) {
                    CompilerErrors.printErrorMessage("Cannot access field of primitive type", field_access);
                    throw new RuntimeException();
                } else {
                    CompilerErrors.printErrorMessage("Cannot access field of variable with unknown type.", field_access);
                    throw new RuntimeException();
                }

                if (value_type == null) {
                    CompilerErrors.printErrorMessage("[CValue.getType]: Could not determine type for field access. ", field_access);
                    throw new RuntimeException();
                }

            } else if (modification instanceof CMethodInvocation) {
                CMethodInvocation method_invocation = (CMethodInvocation)modification;

                try {
                    if (value_type instanceof ObjectType) {
                        value_type = ((ObjectType)value_type).getMethod(method_invocation.method_name, method_invocation.arguments).getReturnType();

                    } else if (value_type instanceof ActorType) {
                        if (modifications.getFirst().equals(modification) && (isSelf() || isParent() || SymbolTable.isReferenceMethod((CMethodInvocation)modification))) {
                            value_type = ((ActorType)value_type).getMessage(method_invocation.method_name, method_invocation.arguments, isParentMessageSend).getPassType();
                        } else {
                            CompilerErrors.printErrorMessage("Cannot invoke a message on an actor that is not self or parent", method_invocation);
                            throw new RuntimeException();
                        }
                    } else if (value_type instanceof ArrayType) { 
                        value_type = ((ObjectType)(value_type.getSuperType())).getMethod(method_invocation.method_name, method_invocation.arguments).getReturnType();
                    } else if (value_type instanceof PrimitiveType) {
                        CompilerErrors.printErrorMessage("Cannot invoke a message on a primitive.", method_invocation);
                        throw new RuntimeException();
                    } else {
                        CompilerErrors.printErrorMessage("Cannot invoke a message on an an unknown type.", method_invocation);
                        throw new RuntimeException();
                    }
                } catch (VariableDeclarationException vde) {
                    CompilerErrors.printErrorMessage("[CValue.getType]: Could not declare variable for method invocation. " + vde.toString(), this);
                    throw new RuntimeException(vde);
                } catch (SalsaNotFoundException snfe) {
                    CompilerErrors.printErrorMessage("[CValue.getType]: Could not determine type for method invocation. " + snfe.toString(), this);
                    throw new RuntimeException(snfe);
                }

                for (CExpression expression : method_invocation.arguments) {
                    if (expression.isToken()) {
                        CompilerErrors.printErrorMessage("[CValue.getType]: Cannot send a token as the argument to a method invocation.", expression);
                    }
                }

                if (value_type == null) {
                    CompilerErrors.printErrorMessage("[CValue.getType]: Could not determine type for method invocation. ", method_invocation);
                    throw new RuntimeException();
                }

            } else if (modification instanceof CMessageSend) {
                CMessageSend message_send = (CMessageSend)modification;

                try {
                    if (!(value_type instanceof ActorType)) {
                        CompilerErrors.printErrorMessage("Cannot send a message to a non-actor. Type is '" + value_type.getLongSignature() + "'", message_send);
                        throw new RuntimeException();
                    }
                    value_type = ((ActorType)value_type).getMessage(message_send.message_name, message_send.arguments, isParentMessageSend).getPassType();
                } catch (VariableDeclarationException vde) {
                    CompilerErrors.printErrorMessage("[CValue.getType]: Could not declare variable for message send. " + vde.toString(), this);
                    throw new RuntimeException(vde);
                } catch (SalsaNotFoundException snfe) {
                    CompilerErrors.printErrorMessage("[CValue.getType]: Could not determine type for message send. " + snfe.toString(), this);
                    throw new RuntimeException(snfe);
                }

                if (value_type == null) {
                    CompilerErrors.printErrorMessage("[CValue.getType]: Could not determine type for message send. ", message_send);
                    throw new RuntimeException();
                }
            }

            isParentMessageSend = false;
        }

        return value_type;
	}

	public boolean isToken() {
        for (CModification modification : modifications) {
            if (modification instanceof CArrayAccess) {
                CArrayAccess array_access = (CArrayAccess)modification;
                if (array_access.expression.isToken()) return true;
            }
            if (modification instanceof CMessageSend) {
                return true;
            }
        }

        try {
            if (expression != null) return expression.isToken();
            else if (allocation != null) return allocation.isToken();
            else if (variable_name != null) return SymbolTable.isToken(variable_name.name);
        } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("Error determining if variable was a token.  Could not determine variable type.", variable_name);
                throw new RuntimeException(snfe);
        }

        return false;
	}

	public boolean containsTokens(Vector<CExpression> expressions) {
		for (int i = 0; i < expressions.size(); i++) {
			if (expressions.get(i).isToken()) return true;
		}
		return false;
	}

    public boolean containsMessageSends() {
        for (CModification modification : modifications) {
            if (modification instanceof CMessageSend) return true;
        }
        return false;
    }

    public String toJavaCode() {
        return toJavaCode(null);
    }

	public String toJavaCode(TypeSymbol currentType) {
		String code = "";
//        System.err.println("CValue.toJavaCode, currentType: " + currentType);

        boolean isParentMessageSend = false;

        boolean currentValueIsToken = false;

		if (literal != null) {
            if (literal.value.equals("self")) {
                code += "this";
                if (modifications.size() == 0 || (modifications.size() > 0 && modifications.get(0) instanceof CMessageSend)) {
                    if (SymbolTable.is_mobile_actor) {
                        code += ".stage.message.target";        //kind of a hack, so that a mobile actor state doesn't need to have a self reference
                    }
                }

            } else if (literal.value.equals("parent")) {
                if (modifications.size() > 0 && modifications.get(0) instanceof CMessageSend) {
                    code += "this";
                    if (SymbolTable.is_mobile_actor) {
                        code += ".stage.message.target";        //kind of a hack, so that a mobile actor state doesn't need to have a self reference
                    }
                    isParentMessageSend = true;
                } else {
                    code += "super";
                }
            } else if (literal.value.equals("ack")) {
                code += "new Acknowledgement()";
            } else {
    			code += literal.toJavaCode();
            }

		} else if (variable_name != null) {
            code += variable_name;
            try {
                currentValueIsToken = SymbolTable.isToken(variable_name.name);
            } catch (SalsaNotFoundException snfe) {
                CompilerErrors.printErrorMessage("Warning: Error determining if variable was a token.  Could not determine variable type. (This needs to be fixed)", variable_name);
//                throw new RuntimeException(snfe);
 
                //HUGE HACK!
                //I think this only breaks with ENUMs and I'm not sure why
                currentValueIsToken = false;
            }

		} else if (expression != null) {
			code += "(" + expression.toJavaCode() + ")";
            currentValueIsToken = expression.isToken();

		} else if (allocation != null) {
			code += allocation.toJavaCode();
            currentValueIsToken = allocation.isToken();

		}

        /**
         *  Need to check if casting a token to non-token, or non-token to token.
         */
        if (cast_type != null) {
            if (isToken()) {
                if (cast_type.is_token) {
                    code = "(TokenDirector)" + code;
                } else {
                    CompilerErrors.printErrorMessage("Cannot cast a token to a non-token type.", cast_type);
                }
            } else {
                if (cast_type.is_token) {
                    CompilerErrors.printErrorMessage("Cannot cast a non-token to a token type.", cast_type);
                } else {
                    code = "(" + cast_type.name + ")" + code;
                }
            }
        }

        if (currentType == null) currentType = getValueType();

        int position = 0;
//        System.err.println("\nNew pass through toJavaCode for CValue():");
        for (CModification modification : modifications) {
//            System.err.println("    currentType = " + currentType.getLongSignature());

            if (modification instanceof CArrayAccess) {
                CArrayAccess aa = (CArrayAccess)modification;

                if (aa.expression.isToken()) {
                    System.err.println("COMPILER PROBLEM: -- haven't implemented using a token for an array access.");
                    CompilerErrors.printErrorMessage("Haven't implemented using a token for array access.", aa);
                    currentType = null;

                } else {
                    code += "[" + aa.expression.toJavaCode() + "]";

                    if (currentType == null) {
                        System.err.println("something weird went down, current type == null.");
                        throw new RuntimeException();
                    }
                    currentType = ((ArrayType)currentType).getSubtype();
                }

                if (aa.expression.isToken()) currentValueIsToken = true;

            } else if (modification instanceof CFieldAccess) {
                CFieldAccess fa = (CFieldAccess)modification;

                if (currentType instanceof ObjectType) {
                    code += "." + fa.name;
                    currentType = ((ObjectType)currentType).getFieldType(fa.name);
                } else if (currentType instanceof ActorType) {
                    code += "." + fa.name;
                    currentType = ((ActorType)currentType).getFieldType(fa.name);
                } else if (currentType instanceof ArrayType) {
                    code += "." + fa.name;
                    currentType = ((ArrayType)currentType).getFieldType(fa.name);
                } else if (currentType instanceof PrimitiveType) {
                    CompilerErrors.printErrorMessage("Cannot access field of primitive type", fa);
                    currentType = null;
                } else {
                    CompilerErrors.printErrorMessage("Cannot access field of variable with unknown type.", fa);
                    currentType = null;
                }

            }  else if (modification instanceof CMethodInvocation) {
                CMethodInvocation mi = (CMethodInvocation)modification;

                try {
                    if (currentType instanceof ObjectType) {
                        MethodSymbol ms = ((ObjectType)currentType).getMethod(mi.method_name, mi.arguments);
                        if (ms == null) {
                            CompilerErrors.printErrorMessage("Could not find matching method.", mi);
                            currentType = null;
                        }
                        currentType = ms.getReturnType();
                    } else if (currentType instanceof ActorType) {
                        if (modifications.getFirst().equals(modification) && (isSelf() || isParent() || SymbolTable.isReferenceMethod((CMethodInvocation)modification))) {
                            currentType = ((ActorType)currentType).getMessage(mi.method_name, mi.arguments, isParentMessageSend).getPassType();
                        } else {
                            CompilerErrors.printErrorMessage("Cannot invoke a method on an actor that is not self or parent", mi);
                            currentType = null;
                        }
                    } else if (currentType instanceof ArrayType) { 
                        CompilerErrors.printErrorMessage("Cannot invoke a method on an array", mi);
                        currentType = null;
                    } else if (currentType instanceof PrimitiveType) {
                        CompilerErrors.printErrorMessage("Cannot invoke a method on a primitive", mi);
                        currentType = null;
                    } else {
                        CompilerErrors.printErrorMessage("Cannot invoke a method on an unknown type.", mi);
                        currentType = null;
                    }

                    if (currentType != null) {
                        code += "." + mi.method_name + "(";

                        for (CExpression argument : mi.arguments) {
                            if (!argument.isToken()) {
                                code += argument.toJavaCode();

                                if (!argument.equals(mi.arguments.lastElement())) code += ", ";
                            } else {
                                CompilerErrors.printErrorMessage("[CValue.toJavaCode]: Cannot pass a token to a method invocation", argument);
                            }
                        }
                        code += ")";
                    }
                } catch (VariableDeclarationException vde) {
                    CompilerErrors.printErrorMessage("[CValue.toJavaCode]: Error declaring variable for method invocation. " + vde.toString(), mi);
                    throw new RuntimeException(vde);
                } catch (SalsaNotFoundException snfe) {
                    CompilerErrors.printErrorMessage("[CValue.toJavaCode]: Error looking up type for method invocation. " + snfe.toString(), mi);
                    throw new RuntimeException(snfe);
                }

            } else if (modification instanceof CMessageSend) {
                CMessageSend ms = (CMessageSend)modification;

                try {
                    String joinDirector = SymbolTable.getJoinDirector();

                    if (ms.message_property != null && !ms.message_property.name.equals("waitfor")) {
                        CompilerErrors.printErrorMessage("COMPILER PROBLEM: Unknown message property '" + ms.message_property.name + "'.", ms);
                    }

                    boolean moreMessages = false;

                    for (int new_pos = position + 1; new_pos < modifications.size(); new_pos++) {
                        if (modifications.get(new_pos) instanceof CMessageSend) {
                            moreMessages = true;
                            break;
                        }
                    }

                    String pre_code = "";
                    if (SymbolTable.continuesToPass && !SymbolTable.withinArguments) {
                        pre_code += "StageService.sendPass";
                    } else if (SymbolTable.withinArguments) {
                        pre_code += "StageService.sendImplicitToken";
                    } else if (SymbolTable.continuationTokenMessage) {
                        pre_code += "StageService.sendContinuation";
                    } else if (SymbolTable.isExpressionContinuation) {
                        pre_code += "StageService.sendToken";
                    } else if (SymbolTable.messageContinues || joinDirector != null) {
                        if (moreMessages) {
                            pre_code += "StageService.sendToken";   //this message returns a token for an actor which has a message being sent to it
                        } else {
                            if (SymbolTable.firstContinuation()) {
                                pre_code += "ContinuationDirector ";
                                SymbolTable.initializedFirstContinuation();
                            }
                            pre_code += "continuation_token = StageService.sendContinuation";
                        } 
                    } else if (moreMessages) {
                            pre_code += "StageService.sendToken";   //this message returns a token for an actor which has a message being sent to it
                    } else {
                        pre_code += "StageService.send";
                    }

                    String expression_director_code = "";
                    pre_code += "Message(";

                    String target_code = code;
                    if (currentType.isInterface) target_code = "(Actor)" + target_code;

                    if (currentValueIsToken) {
                        code = pre_code + "null, ";
                    } else {
                        code = pre_code + target_code + ", ";
                    }

                    if (!(currentType instanceof ActorType)) {
                        CompilerErrors.printErrorMessage("Cannot send a message to an object '" + currentType.getLongSignature() + "'.", ms);
                        throw new RuntimeException();
                    }

                    ActorType at = (ActorType)currentType;
                    MessageSymbol messageSymbol = null;
                    try {
                        messageSymbol = at.getMessage(ms.message_name, ms.arguments, isParentMessageSend);
                    } catch (VariableDeclarationException vde) {
                        CompilerErrors.printErrorMessage("Could not find matching message, message name: '" + ms.message_name + "', variable declaration exception: " + vde.toString(), ms);
                        throw new RuntimeException(vde);
                    } catch (SalsaNotFoundException snfe) {
                        CompilerErrors.printErrorMessage("Could not find matching message, message name: '" + ms.message_name + "'", ms);
                        throw new RuntimeException(snfe);
                    }

                    code += messageSymbol.getId();
                    code += " /*" + ms.message_name + "*/, ";

                    String argument_code = "new Object[]{";
                    String argument_tokens = "new int[]{";
                    boolean hasContinuationToken = false;
                    boolean hasToken = false;
                    int j = 0;
                    for (CExpression argument : ms.arguments) {
                        if (argument.isToken()) {
                            if (argument.requiresExpressionDirector()) {
                                expression_director_code += argument.getExpressionDirectorCode();
                            }

                            if (argument_tokens.length() > 10) argument_tokens += ", ";
                            argument_tokens += j;
                            hasToken = true;

                        } else if (SymbolTable.isMutableObject( argument.getType() )) {
                            argument_code += "(" + argument.getType().getName() + ")DeepCopy.deepCopy( ";
                        }

                        SymbolTable.withinArguments = true;
                        argument_code += argument.toJavaCode();
                        SymbolTable.withinArguments = false;

                        if (SymbolTable.isMutableObject( argument.getType() )) argument_code += " )";

                        if (!argument.equals(ms.arguments.lastElement())) argument_code += ", ";
                        j++;
                    }
                    code = expression_director_code + code;

                    if (hasToken) {
                        argument_code += "}, " + argument_tokens + "}";
                    } else {
                        argument_code += "}";
                    }
                    if (ms.arguments.size() == 0) argument_code = "null";

                    code += argument_code;
                    if (ms.message_property != null && ms.message_property.name.equals("waitfor")) {
                        code += ", new Director[]{";

                        for (int i = 0; i < ms.message_property.arguments.size(); i++) {
                            CExpression arg = ms.message_property.arguments.get(i);
                            if (!arg.isToken()) {
                                CompilerErrors.printErrorMessage("Cannot waitfor non-token.", arg);
                                throw new RuntimeException();
                            }
                            code += arg.toJavaCode();

                            if (i != ms.message_property.arguments.size() - 1) code += ", ";
                        }

                        if (SymbolTable.messageRequiresContinuation) {
                            code += ", continuation_token";
                            SymbolTable.messageRequiresContinuation = false;    //so we only use the continuation once
                        }
                        code += "}";

                    } else if (SymbolTable.messageRequiresContinuation) {
                        code += ", continuation_token";
                        SymbolTable.messageRequiresContinuation = false;    //so we only use the continuation once
                    }

                    if (SymbolTable.continuesToPass && !SymbolTable.withinArguments) {
                        code += ", this.stage.message.continuationDirector";
                    }

                    if (currentValueIsToken) {
//                        System.err.println("CURRENT VALUE IS TOKEN FOR CODE: " + code);
                        code += ", " + target_code;
                    }

                    code += ")";

                    if (joinDirector != null && !SymbolTable.messageContinues) {
                        code += ";\n";
                        code += CIndent.getIndent() + "StageService.sendMessage(" + joinDirector + ", 0 /*setValue*/, new Object[]{++" + joinDirector + "_message_count}, continuation_token)";
                    }

                    currentType = ((ActorType)currentType).getMessage(ms.message_name, ms.arguments, isParentMessageSend).getPassType();
                    SymbolTable.setContinuationType(currentType);
                } catch (VariableDeclarationException vde) {
                    CompilerErrors.printErrorMessage("[CValue.toJavaCode]: Error declaring variable for message send. " + vde.toString(), ms);
                    throw new RuntimeException(vde);
                } catch (SalsaNotFoundException snfe) {
                    CompilerErrors.printErrorMessage("[CValue.toJavaCode]: Error looking up type for message send. " + snfe.toString(), ms);
                    throw new RuntimeException(snfe);
                }

                currentValueIsToken = true;
            }
            isParentMessageSend = false;
            position++;
        }

		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
