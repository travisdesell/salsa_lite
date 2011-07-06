package salsa_lite.wwc;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import salsa_lite.wwc.language.exceptions.TokenPassException;
import salsa_lite.wwc.language.exceptions.ContinuationPassException;
import salsa_lite.wwc.language.exceptions.ConstructorNotFoundException;

import salsa_lite.wwc.language.ContinuationDirector;
import salsa_lite.wwc.language.ImplicitTokenDirector;
import salsa_lite.wwc.language.TokenDirector;

public class StageActor extends Thread {

	private Hashtable<String, WWCActorState> actors;
	private LinkedList<Message> mailbox;
	private int id;

	public StageActor(int id) {
		this.id = id;
		actors = new Hashtable<String, WWCActorState>();
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

	public void putActorOnStage(WWCActorState actorState, int constructor_id, Object[] arguments) {
		synchronized (actors) {
//			System.out.println("putting actor: " + actorState.getUniqueId() + " -- " + actorState);
			actors.put(actorState.getUniqueId(), actorState);
		}
		StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actorState.self, constructor_id, arguments));
	}

	Message message;
	public void run() {
		WWCActorState currentTarget;
		Object result;

		while (true) {
			message = getMessage();

			try {
				switch (message.type) {
					case Message.CONSTRUCT_MESSAGE:
						synchronized (actors) {
							currentTarget = actors.get( message.target_reference.getUniqueId() );
						}
						if (currentTarget == null) {
							TransportService.sendMessage(message);
						} else {
							currentTarget.invokeConstructor(message.message_id, message.arguments);
						}
						break;

					case Message.SIMPLE_MESSAGE:
						synchronized (actors) {
//							System.out.println("getting actor: " + message.target_reference.getUniqueId());
							currentTarget = actors.get( message.target_reference.getUniqueId() );
						}
						if (currentTarget == null) {
							TransportService.sendMessage(message);
						} else {
							currentTarget.invokeMessage(message.message_id, message.arguments);
						}
						break;

					case Message.CONTINUATION_MESSAGE:
						synchronized (actors) {
//							System.out.println("getting actor: " + message.target_reference.getUniqueId());
							currentTarget = actors.get( message.target_reference.getUniqueId() );
						}
						if (currentTarget == null) {
							TransportService.sendMessage(message);
						} else {
							currentTarget.invokeMessage(message.message_id, message.arguments);
							StageService.sendMessage(message.continuationDirector, 0 /*resolve*/, new Object[]{});
						}
						break;

					case Message.TOKEN_MESSAGE:
						synchronized (actors) {
//							System.out.println("getting actor: " + message.target_reference.getUniqueId());
							currentTarget = actors.get( message.target_reference.getUniqueId() );
						}
						if (currentTarget == null) {
							TransportService.sendMessage(message);
						} else {
							result = currentTarget.invokeMessage(message.message_id, message.arguments);
							StageService.sendMessage(message.continuationDirector, 1 /*setValue*/, new Object[]{result});
						} 
						
						break;

					case Message.IMMUTABLE_MESSAGE:
						message.target_reference.invokeImmutableMessage(message.message_id, message.arguments);
						break;

					case Message.IMMUTABLE_CONTINUATION_MESSAGE:
						message.target_reference.invokeImmutableMessage(message.message_id, message.arguments);
						StageService.sendMessage(message.continuationDirector, 0 /*resolve*/, new Object[]{});
						break;

					case Message.IMMUTABLE_TOKEN_MESSAGE:
						result = message.target_reference.invokeImmutableMessage(message.message_id, message.arguments);
						StageService.sendMessage(message.continuationDirector, 1 /*setValue*/, new Object[]{result});
						break;

					case Message.MIGRATE_MESSAGE:
						synchronized (actors) {
							currentTarget = actors.remove( message.target_reference.getUniqueId() );
						}
						if (currentTarget == null) {
							TransportService.sendMessage(message);
						} else {
							TransportService.migrateActor(currentTarget, message.arguments);
						}

						break;

					case Message.DESTROY_MESSAGE:
//						System.out.println("destroying actor: " + message.target_reference.getUniqueId() + ", " + message.target_reference);
						synchronized (actors) {
							currentTarget = actors.remove( message.target_reference.getUniqueId() );
						}
						if (currentTarget == null) {
							TransportService.sendMessage(message);
						}
						break;

					default:
						System.err.println("Unknown message type: " + message.type);
						return;
				}
			} catch (TokenPassException tpe) {
				//Don't need to do anything here.

			} catch (ContinuationPassException cpe) {
				System.err.println("ContinuationPassException!!!");

				ContinuationDirector cd = cpe.continuationDirector;
				switch(message.type) {
					case Message.CONTINUATION_MESSAGE:
						break;

					case Message.TOKEN_MESSAGE:
						break;

					case Message.IMMUTABLE_CONTINUATION_MESSAGE:
						break;

					case Message.IMMUTABLE_TOKEN_MESSAGE:
						break;

					default:
						System.err.println("Continuation passed message without a continuation.\n");
						return;
				}
	
			} catch (Exception exception) {
				System.err.println("Message processing exception:");
				System.err.println("\tMessage: " + message);
				System.err.println("\ttarget: " + message.target_reference);
				if (message.target_reference != null) System.err.println("\ttarget_id: " + message.target_reference.getUniqueId());
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
