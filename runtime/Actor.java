package salsa_lite.runtime;

import java.io.ObjectStreamException;
import java.io.Serializable;

import java.util.LinkedList;
import java.util.List;

import salsa_lite.runtime.language.exceptions.ConstructorNotFoundException;
import salsa_lite.runtime.language.exceptions.RemoteMessageException;
import salsa_lite.runtime.language.exceptions.MessageHandlerNotFoundException;
import salsa_lite.runtime.language.exceptions.TokenPassException;

public abstract class Actor implements java.io.Serializable {

	public transient SynchronousMailboxStage stage;

    protected int hashCode;
    public int hashCode() {
        return hashCode;
    }

    protected Actor(boolean dont_use_default_constructor) {}

    public Actor() {
        hashCode = Hashing.generateUniqueHashCode(super.hashCode());
        this.stage = StageService.stages[Math.abs(hashCode % StageService.number_stages)];
	}

	public Actor(SynchronousMailboxStage stage) {
        hashCode = Hashing.generateUniqueHashCode(super.hashCode());
		this.stage = stage;
	}

    public String toString() {
        return "Actor[stage: " + stage.getStageId() + ", type: " + getClass().getName() + ", hashCode: " + hashCode() + "]";
    }

	public abstract void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, ConstructorNotFoundException;

	public abstract Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException;
	
}
