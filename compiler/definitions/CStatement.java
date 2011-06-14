package salsa_lite.compiler.definitions;

import java.util.Vector;

public abstract class CStatement extends CErrorInformation {
	public boolean continues = false;

	public CStatement next_statement = null;

	public void addContainedMessageHandlers(Vector<CMessageHandler> constrainedMessageHandlers) {
		return;
	}

	public abstract String toJavaCode();
}
