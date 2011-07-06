package salsa_lite.wwc;

import salsa_lite.wwc.language.exceptions.ConstructorNotFoundException;
import salsa_lite.wwc.language.exceptions.MessageHandlerNotFoundException;
import salsa_lite.wwc.language.exceptions.ContinuationPassException;
import salsa_lite.wwc.language.exceptions.TokenPassException;

public abstract class WWCActorState {

	public WWCActorReference self;

	private String identifier;
	public int getHashcode() { return identifier.hashCode(); }
	public String getUniqueId() { return identifier; }

	public WWCActorState(String identifier) {
		this.identifier = identifier;
	}

	public abstract Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException;
	public abstract void invokeConstructor(int messageId, Object[] arguments) throws ConstructorNotFoundException;
}
