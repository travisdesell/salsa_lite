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

    int hashCode;
    public int hashCode() {
        return hashCode;
    }

    protected Actor(int hashCode) {
        this.hashCode = hashCode;
		this.stage = StageService.stages[Math.abs(this.hashCode() % StageService.number_stages)];
    }
    
    protected Actor(int hashCode, SynchronousMailboxStage stage) {
        this.hashCode = hashCode;
        this.stage = stage;
    }

	public Actor() {
        hashCode = ActorRegistry.generateUniqueHashCode(super.hashCode());
		this.stage = StageService.stages[Math.abs(this.hashCode() % StageService.number_stages)];
	}

	public Actor(SynchronousMailboxStage stage) {
        hashCode = ActorRegistry.generateUniqueHashCode(super.hashCode());
		this.stage = stage;
	}

    public String toString() {
        return "Actor[stage: " + stage.getStageId() + ", type: " + getClass().getName() + ", hashCode: " + hashCode() + "]";
    }

	public abstract void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, ConstructorNotFoundException;

	public abstract Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException;
	
}
