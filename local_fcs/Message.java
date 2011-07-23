/**
 * This is the Message class.
 */

package salsa_lite.local_fcs;

import salsa_lite.local_fcs.language.Director;

import java.util.Arrays;

public class Message {
	public final static int CONSTRUCT_MESSAGE = 0;
	public final static int SIMPLE_MESSAGE = 1;
	public final static int CONTINUATION_MESSAGE = 2;
	public final static int TOKEN_MESSAGE = 3;

	public final static int DESTROY_MESSAGE = 7;

	public int type;

	public LocalActor target;

	public int message_id;
	public Object[] arguments;

	public Director continuationDirector;

	public Message(int type, LocalActor target, int message_id, Object[] arguments) {
		this.type = type;
		this.target = target;
		this.message_id = message_id;
		this.arguments = arguments;
	}

	public Message(int type, LocalActor target, int message_id, Object[] arguments, Director continuationDirector) {
		this.type = type;
		this.target = target;
		this.message_id = message_id;
		this.arguments = arguments;
		this.continuationDirector = continuationDirector;
	}

    public String toString() {
        String argString = "null";
        if (arguments != null) argString = Arrays.toString(arguments);
        return String.format("message[target: %s, id: %d, " + argString + ", continuationDirector: %s]", target, message_id, continuationDirector);
    }
}
