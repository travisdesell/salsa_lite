package salsa_lite.wwc;

import salsa_lite.wwc.language.exceptions.ContinuationPassException;
import salsa_lite.wwc.language.exceptions.TokenPassException;
import salsa_lite.wwc.language.exceptions.MessageHandlerNotFoundException;

public abstract class WWCActorReference {

	private String identifier;
	public int getHashcode() { return identifier.hashCode(); }
	public String getUniqueId() { return identifier; }


	public WWCActorReference(String identifier) {
		this.identifier = identifier;
	}

	public abstract Object invokeImmutableMessage(int message_id, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException;
}
