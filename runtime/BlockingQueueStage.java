package salsa_lite.runtime;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import java.util.concurrent.LinkedBlockingQueue;


import salsa_lite.runtime.language.exceptions.TokenPassException;
import salsa_lite.runtime.language.exceptions.ContinuationPassException;
import salsa_lite.runtime.language.exceptions.ConstructorNotFoundException;

//import salsa_lite.runtime.language.ContinuationDirector;
//import salsa_lite.runtime.language.ImplicitTokenDirector;
//import salsa_lite.runtime.language.TokenDirector;

public class BlockingQueueStage extends Thread {

	private LinkedBlockingQueue<Message> mailbox;
	private int id;

	public BlockingQueueStage(int id) {
		this.id = id;
		mailbox = new LinkedBlockingQueue<Message>();
	}

	public void putMessageInMailbox(Message message) {
		try {
			mailbox.put(message);
		} catch (InterruptedException e) {
			System.err.println("Interrupted!");
		}
	}

	Message message = null;
	public void run() {
		Object result;

		while (true) {
			try {
				message = mailbox.take();
			} catch (InterruptedException e) {
				System.err.println("Interrupted!");
			}

			try {
				switch (message.type) {
					case Message.CONSTRUCT_MESSAGE:
						message.target.invokeConstructor(message.message_id, message.arguments);
						break;

					case Message.SIMPLE_MESSAGE:
						message.target.invokeMessage(message.message_id, message.arguments);
						break;

					case Message.CONTINUATION_MESSAGE:
						message.target.invokeMessage(message.message_id, message.arguments);
						StageService.sendMessage(message.continuationDirector, 0 /*resolve*/, null);
						break;

					case Message.TOKEN_MESSAGE:
						result = message.target.invokeMessage(message.message_id, message.arguments);
						StageService.sendMessage(message.continuationDirector, 0 /*setValue*/, new Object[]{result});
						
						break;

					default:
						System.err.println("Unknown message type: " + message.type);
						return;
				}
			} catch (TokenPassException tpe) {
				//Don't need to do anything here.
				
			} catch (ContinuationPassException cpe) {
				System.err.println("ContinuationPassException!!!");

//				Actor cd = cpe.continuationDirector;
				switch(message.type) {
					case Message.CONTINUATION_MESSAGE:
						break;

					case Message.TOKEN_MESSAGE:
						break;

					default:
						System.err.println("Continuation passed message without a continuation.\n");
						return;
				}
	
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
