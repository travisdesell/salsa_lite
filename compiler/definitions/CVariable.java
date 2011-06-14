package salsa_lite.compiler.definitions;

import salsa_lite.compiler.SalsaParser;
import salsa_lite.compiler.SimpleNode;

import java.util.Vector;


public class CVariable {

	public boolean is_token = false;
	public String modifier = "none";

	public String type = "unknown";
	public String name = "unnamed";

	public String literal_value;
	
	public CVariableInit init;	//Expression or ArrayInit or Allocation

	public String toJavaCode() {
		String code = "";
		if (literal_value != null) {
			code = literal_value;
		} else {
			if (!name.equals("unnamed")) code += name;

			if (init != null) {
				if (init instanceof CExpression) {
					code += " = " + ((CExpression)init).toJavaCode();
				} else if (init instanceof CArrayInit) {
					code += " = " + ((CArrayInit)init).toJavaCode();
				} else if (init instanceof CAllocation) {
					//I DONT THINK THIS IS USED
					System.out.println("ALLOCATION IN CVARIABLE");
					code += ((CAllocation)init).toJavaCode();
				}
			}
		}

		return code;
	}

	public String toSalsaCode() {
		return ""; 
	}   
}
