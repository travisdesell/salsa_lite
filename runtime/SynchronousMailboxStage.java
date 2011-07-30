package salsa_lite.runtime;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import salsa_lite.runtime.language.exceptions.TokenPassException;
import salsa_lite.runtime.language.exceptions.RemoteMessageException;
import salsa_lite.runtime.language.exceptions.ConstructorNotFoundException;

//import salsa_lite.runtime.language.ContinuationDirector;
//import salsa_lite.runtime.language.ImplicitTokenDirector;
//import salsa_lite.runtime.language.TokenDirector;

public class SynchronousMailboxStage extends Thread {

	private LinkedList<Message> mailbox;
	private int id;

	public SynchronousMailboxStage(int id) {
		this.id = id;
		mailbox = new LinkedList<Message>();
	}

	public synchronized void putMessageInMailbox(Message message) {
		mailbox.add(message);
		notify();
	}

	private synchronized Message getMessage() {
		if (mailbox.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				System.err.println("Stage Error:");
				System.err.println("\tInterruptedException in getMessage(): " + e);
				System.exit(0);
			}
		}
		return mailbox.removeFirst();
	}

	public Message message;
	public void run() {
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
//						System.err.println("sending resolve to continuation from continuation message!");
						StageService.sendMessage(message.continuationDirector, 0 /*resolve*/, null); 
						break;

					case Message.TOKEN_MESSAGE:
						result = message.target.invokeMessage(message.message_id, message.arguments);
//						System.err.println("sending resolve to continuation from token message!");
						StageService.sendMessage(message.continuationDirector, 0 /*setValue*/, new Object[]{result});
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
				System.err.println("Message processing exception:");
				System.err.println("\tMessage: " + message);
				System.err.println("\ttarget: " + message.target);
				System.err.println("\tmessage_id: " + message.message_id);
				System.err.println("\tmessage_type: " + message.type);
				System.err.print("\targuments: ");
				if (message.arguments == null) System.err.println("null");
				else {
					for (int i = 0; i < message.arguments.length; i++) {
						System.err.print(" " + message.arguments[i]);
					}
					System.err.println("\n");
				}

				System.err.println("\tThrew exception: " + exception);
				exception.printStackTrace();
				return;
			}
		}
	}
}
