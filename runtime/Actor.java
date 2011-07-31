package salsa_lite.runtime;

import java.io.ObjectStreamException;
import java.io.Serializable;

import java.util.LinkedList;
import java.util.List;

import salsa_lite.runtime.language.exceptions.ConstructorNotFoundException;
import salsa_lite.runtime.language.exceptions.RemoteMessageException;
import salsa_lite.runtime.language.exceptions.MessageHandlerNotFoundException;
import salsa_lite.runtime.language.exceptions.TokenPassException;


public abstract class Actor {

	public SynchronousMailboxStage stage;

	public Actor() {
		this.stage = StageService.stages[Math.abs(this.hashCode() % StageService.number_stages)];
        hashCode = ActorRegistry.generateUniqueHashCode(super.hashCode());
	}

	public Actor(SynchronousMailboxStage stage) {
		this.stage = stage;
        hashCode = ActorRegistry.generateUniqueHashCode(super.hashCode());
	}

    public String toString() {
        return "actor[stage: " + stage.getStageId() + ", type: " + getClass().getName() + ", hashCode: " + hashCode() + "]";
    }

    int hashCode;
    public int hashCode() {
        return hashCode;
    }

	public abstract void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, ConstructorNotFoundException;

	public abstract Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException;
	
}
