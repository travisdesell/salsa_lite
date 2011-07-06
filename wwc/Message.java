/**
 * This is the Message class.
 */

package salsa_lite.wwc;

import salsa_lite.wwc.language.ContinuationDirector;

public class Message implements java.io.Serializable {
	public final static int CONSTRUCT_MESSAGE = 0;

	public final static int SIMPLE_MESSAGE = 1;
	public final static int CONTINUATION_MESSAGE = 2;
	public final static int TOKEN_MESSAGE = 3;

	public final static int IMMUTABLE_MESSAGE = 4;
	public final static int IMMUTABLE_TOKEN_MESSAGE = 5;
	public final static int IMMUTABLE_CONTINUATION_MESSAGE = 6;

	public final static int DESTROY_MESSAGE = 7;

	public final static int MIGRATE_MESSAGE = 8;
	public final static int MIGRATE_CONTINUATION_MESSAGE = 9;
	public final static int MIGRATE_TOKEN_MESSAGE = 10;

	public int type;

	public int message_id;
	public Object[] arguments;

	public WWCActorReference target_reference;

	ContinuationDirector continuationDirector;

	public Message(int type, WWCActorReference target, int message_id, Object[] arguments) {
		this.type = type;
		this.target_reference = target;
		this.message_id = message_id;
		this.arguments = arguments;
	}

	public Message(int type, WWCActorReference target, int message_id, Object[] arguments, ContinuationDirector continuationDirector) {
		this.type = type;
		this.target_reference = target;
		this.message_id = message_id;
		this.arguments = arguments;
		this.continuationDirector = continuationDirector;
	}
}
