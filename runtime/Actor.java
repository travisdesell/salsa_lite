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

	protected transient SynchronousMailboxStage stage;
    public SynchronousMailboxStage getStage() {
        return stage;
    }

    protected int stage_id;
    public final int getStageId() {
        return stage_id;
    }

//    protected int hashCode;
//    public int hashCode() {
//        return hashCode;
//    }

    protected Actor(boolean dont_use_default_constructor) {}

    public Actor() {
//        hashCode = Hashing.generateUniqueHashCode(super.hashCode());
//        this.stage = StageService.stages[Math.abs(hashCode % StageService.number_stages)];
        this.stage = StageService.stages[Math.abs(hashCode() % StageService.number_stages)];
        this.stage_id = this.stage.getStageId();
	}

	public Actor(int stage_id) {
//        hashCode = Hashing.generateUniqueHashCode(super.hashCode());
		this.stage_id = stage_id;
        this.stage = StageService.getStage(stage_id);
	}

    public String toString() {
        return "Actor[stage: " + stage.getStageId() + ", type: " + getClass().getName() + ", hashCode: " + hashCode() + "]";
    }

	public abstract void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, ConstructorNotFoundException;

	public abstract Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException;
	
}
