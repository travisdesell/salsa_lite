package salsa_lite.compiler.definitions;

import java.util.StringTokenizer;

import salsa_lite.compiler.Token;

public class CJavaStatement extends CStatement {
	public Token t1, t2;

	public CJavaStatement() {
	}

	public String toJavaCode() {
		String comment = t2.specialToken.image;
		StringTokenizer st = new StringTokenizer(comment, "\n\r");
		String code = "";

		while (st.hasMoreTokens()) {
			String current = st.nextToken().trim();
			if (current.equals("/*")) {
				code += st.nextToken().trim() + "\n";
			} else if (!current.equals("*/")) {
				if (current.length() > 2 && current.charAt(0) == '/' && current.charAt(1) == '*') {
					current = current.substring(2, current.length());
					current = current.trim();
				} else code += CIndent.getIndent();

				boolean was_single_line = false;
				if (current.length() > 2 && current.charAt(current.length() - 1) == '/' && current.charAt(current.length() - 2) == '*') {
					current = current.substring(0, current.length() - 2);
					current = current.trim();
					was_single_line = true;
				}

				code += current;
				if (!was_single_line) {
					code += "\n";
				}
			}
		}

		return code;
	}

	public String toSalsaCode() {
		return "";
	}
}
