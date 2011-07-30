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
	}

	public Actor(SynchronousMailboxStage stage) {
		this.stage = stage;
	}

    public String toString() {
        return "actor[stage: " + stage.getId() + ", type: " + getClass().getName() + "]";
    }

	public abstract void invokeConstructor(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, ConstructorNotFoundException;

	public abstract Object invokeMessage(int messageId, Object[] arguments) throws RemoteMessageException, TokenPassException, MessageHandlerNotFoundException;
	
	
	private Message currentMessage = null;

	public Message getCurrentMessage() {
		return currentMessage;
	}

	public void setCurrentMessage(Message currentMessage) {
		this.currentMessage = currentMessage;
	}
	
	private boolean isActive = false;
	
	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	private List<Message> mailbox = new LinkedList<Message>();
	
	public synchronized void putMessageInMailbox(Message message) {
		mailbox.add(message);
	}
	
	public synchronized Message getNextMessage() {
		if (mailbox.size() == 0) {
			System.err.println("Should not happen!");
			return null;
		}
		else
			return mailbox.remove(0);
	}
}
