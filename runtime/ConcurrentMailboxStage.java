package salsa_lite.runtime;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import salsa_lite.runtime.language.exceptions.TokenPassException;
import salsa_lite.runtime.language.exceptions.RemoteMessageException;
import salsa_lite.runtime.language.exceptions.ConstructorNotFoundException;

//import salsa_lite.runtime.language.ContinuationDirector;
//import salsa_lite.runtime.language.ImplicitTokenDirector;
//import salsa_lite.runtime.language.TokenDirector;

public class ConcurrentMailboxStage extends Thread {

	private ConcurrentLinkedQueue<Message> mailbox;
	private int id;

    public final int getStageId() {
        return id;
    }

	public ConcurrentMailboxStage(int id) {
		this.id = id;
		mailbox = new ConcurrentLinkedQueue<Message>();
	}

	public final void putMessageInMailbox(Message message) {
		mailbox.add(message);
		notify();
	}

	private final Message getMessage() {
		if (mailbox.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println("Stage Error:");
				System.err.println("\tInterruptedException in getMessage(): " + e);
				System.exit(0);
			}
		}
		return mailbox.poll();
	}

	public Message message;
	public final void run() {
		Object result;

		while (true) {
			message = getMessage();

			try {
//                System.err.println("invoking message: " + message);
				switch (message.type) {
					case Message.CONSTRUCT_MESSAGE:
						message.target.invokeConstructor(message.message_id, message.arguments);
						break;

					case Message.SIMPLE_MESSAGE:
						message.target.invokeMessage(message.message_id, message.arguments);
						break;

					case Message.CONTINUATION_MESSAGE:
						message.target.invokeMessage(message.message_id, message.arguments);
						StageService.sendMessage(message.continuationDirector, 2 /*resolve*/, null); 
						break;

					case Message.TOKEN_MESSAGE:
						result = message.target.invokeMessage(message.message_id, message.arguments);
						StageService.sendMessage(message.continuationDirector, 2 /*setValue*/, new Object[]{result});
						break;

                    case Message.CONSTRUCT_CONTINUATION_MESSAGE:
						message.target.invokeConstructor(message.message_id, message.arguments);
						StageService.sendMessage(message.continuationDirector, 2 /*setValue*/, new Object[]{message.target});
						break;

					default:
						System.err.println("Unknown message type: " + message.type);
						return;
				}
			} catch (TokenPassException tpe) {
				//Don't need to do anything here.
				
			} catch (RemoteMessageException exception) {
                //Don't need to do anything here, the message was sent to another theater.

			} catch (Exception exception) {
                StringBuilder errorMessage = new StringBuilder("Exception occurred while processing message:\n");
                errorMessage.append("\tmessage signature: '" + message.target.getMessageInformation(message.message_id) + "'\n");
				errorMessage.append("\tmessage contents: " + message + "\n");
				errorMessage.append("\ttarget: " + message.target + "\n");
				errorMessage.append("\tmessage_id: " + message.message_id + "\n");
				errorMessage.append("\tmessage_type: " + message.type + "\n");
				errorMessage.append("\targuments: ");
				if (message.arguments == null) errorMessage.append("null");
				else {
					for (int i = 0; i < message.arguments.length; i++) {
						errorMessage.append(" " + message.arguments[i]);
					}
				}
                errorMessage.append("\n");

				errorMessage.append("\tThrew exception: " + exception + "\n");
                for (StackTraceElement ste : exception.getStackTrace()) {
                    errorMessage.append("\t\t" + ste.toString() + "\n");
                }
                System.err.println(errorMessage.toString());
				return;
			}
		}
	}
}
