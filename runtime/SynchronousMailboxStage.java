package salsa_lite.runtime;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
//import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

import salsa_lite.runtime.language.exceptions.TokenPassException;
import salsa_lite.runtime.language.exceptions.RemoteMessageException;
import salsa_lite.runtime.language.exceptions.ConstructorNotFoundException;

//import salsa_lite.runtime.language.ContinuationDirector;
//import salsa_lite.runtime.language.ImplicitTokenDirector;
//import salsa_lite.runtime.language.TokenDirector;

public class SynchronousMailboxStage extends Thread {

	private LinkedList<Message> mailbox;
//  private final LinkedBlockingQueue<Message> mailbox;
//	private ConcurrentLinkedQueue<Message> mailbox;
	private int id;

    private long uniqueNamesGenerated = 0;
    public String getUniqueName() {
        return TransportService.getHost() + ":" + TransportService.getPort() + "/" + id + "/" + (++uniqueNamesGenerated);
    }



    public final int getStageId() {
        return id;
    }

	public SynchronousMailboxStage(int id) {
		this.id = id;
		mailbox = new LinkedList<Message>();
//      mailbox = new LinkedBlockingQueue<Message>();
//		mailbox = new ConcurrentLinkedQueue<Message>();
	}

    /*
    public final void putMessageInMailbox(Message message) {
        try {
            mailbox.put(message);
        } catch (InterruptedException e) {
            System.err.println("Stage Error:");
            System.err.println("\tInterruptedException in putMessageInMailbox(): " + e);
            System.exit(0);
        }
    }

    private final Message getMessage() {
        Message result = null;
        try {
            result = mailbox.take();
        } catch (InterruptedException e) {
            System.err.println("Stage Error:");
            System.err.println("\tInterruptedException in getMessage(): " + e);
            System.exit(0);
        }
        return result;
    }
    */

    public final synchronized void putMessageInMailbox(Message message) {
        mailbox.add(message);
        notify();
    }

    private final synchronized Message getMessage() {
        Message message = null;

        while (mailbox.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException e) {
                System.err.println("Stage Error:");
                System.err.println("\tInterruptedException in getMessage(): " + e);
                System.exit(0);
            }
        }
        message = mailbox.removeFirst();

        return message;
	}


	public Message message;
	public final void run() {
        //System.out.println("starting stage with id: " + id);

		Object result;

		while (true) {
			message = getMessage();

			try {
                //System.err.println("invoking message: " + message);
				switch (message.type) {
					case Message.CONSTRUCT_MESSAGE:
						message.target.invokeConstructor(message.message_id, message.arguments);
						break;

					case Message.SIMPLE_MESSAGE:
						message.target.invokeMessage(message.message_id, message.arguments);
						break;

					case Message.CONTINUATION_MESSAGE:
						message.target.invokeMessage(message.message_id, message.arguments);
						StageService.sendMessage(message.continuationDirector, 3 /*resolve*/, null); 
						break;

					case Message.TOKEN_MESSAGE:
						result = message.target.invokeMessage(message.message_id, message.arguments);
						StageService.sendMessage(message.continuationDirector, 3 /*setValue*/, new Object[]{result});
						break;

                    case Message.CONSTRUCT_CONTINUATION_MESSAGE:
						message.target.invokeConstructor(message.message_id, message.arguments);
						StageService.sendMessage(message.continuationDirector, 3 /*setValue*/, new Object[]{message.target});
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
