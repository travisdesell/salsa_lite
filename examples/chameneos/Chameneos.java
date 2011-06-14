package salsa_lite.examples.chameneos;

/****** SALSA LANGUAGE IMPORTS ******/
import salsa_lite.local_fcs.Acknowledgement;
import salsa_lite.local_fcs.SynchronousMailboxStage;
import salsa_lite.local_fcs.LocalActor;
import salsa_lite.local_fcs.Message;
import salsa_lite.local_fcs.StageService;
import salsa_lite.local_fcs.language.JoinDirector;
import salsa_lite.local_fcs.language.MessageDirector;
import salsa_lite.local_fcs.language.ContinuationDirector;
import salsa_lite.local_fcs.language.TokenDirector;

import salsa_lite.local_fcs.language.exceptions.ContinuationPassException;
import salsa_lite.local_fcs.language.exceptions.TokenPassException;
import salsa_lite.local_fcs.language.exceptions.MessageHandlerNotFoundException;
import salsa_lite.local_fcs.language.exceptions.ConstructorNotFoundException;

/****** END SALSA LANGUAGE IMPORTS ******/


public class Chameneos extends salsa_lite.local_fcs.LocalActor {
	int self_meetings = 0;
	int other_meetings = 0;
	MeetingPlace meeting_place;


	public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			case 0: left_other( (salsa_lite.examples.chameneos.Colour)arguments[0] ); return null;
			case 1: left_self( (salsa_lite.examples.chameneos.Colour)arguments[0] ); return null;
			case 2: output_meetings(); return null;
			default: throw new MessageHandlerNotFoundException(messageId, arguments);
		}
	}

	public void invokeConstructor(int messageId, Object[] arguments) throws ConstructorNotFoundException {
		switch(messageId) {
			case 0: construct( (salsa_lite.examples.chameneos.Colour)arguments[0], (salsa_lite.examples.chameneos.MeetingPlace)arguments[1] ); return;
			default: throw new ConstructorNotFoundException(messageId, arguments);
		}
	}

	public void construct(Colour colour, MeetingPlace meeting_place) {
		this.meeting_place = meeting_place;
		StageService.sendMessage(meeting_place, 6 /*meet*/, new Object[]{this, colour});
	}



	public void left_other(Colour new_colour) {
		other_meetings++;
		StageService.sendMessage(meeting_place, 6 /*meet*/, new Object[]{this, new_colour});
	}

	public void left_self(Colour new_colour) {
		self_meetings++;
		StageService.sendMessage(meeting_place, 6 /*meet*/, new Object[]{this, new_colour});
	}

	public void output_meetings() throws TokenPassException {
		System.out.print(other_meetings);
		StageService.sendPassMessage(meeting_place, 0 /*spell*/, new Object[]{self_meetings}, this.stage.message.continuationDirector);
		throw new TokenPassException();
	}


	public Chameneos() { super(); }

	public Chameneos(SynchronousMailboxStage stage) { super(stage); }

	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
		Chameneos actor = new Chameneos();
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null);
		Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length});
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			StageService.sendMessage(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
		return output_continuation;
	}

	public static Chameneos construct(int constructor_id, Object[] arguments) {
		Chameneos actor = new Chameneos();
		StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {
		Chameneos actor = new Chameneos(target_stage);
		TokenDirector output_continuation = TokenDirector.construct(0 /*construct()*/, null, target_stage);
		Message input_message = new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments, output_continuation);
		MessageDirector md = MessageDirector.construct(0, new Object[]{input_message, token_positions.length}, target_stage);
		TokenDirector argument_token;
		for (int i = 0; i < token_positions.length; i++) {
			argument_token = (TokenDirector)arguments[token_positions[i]];
			argument_token.stage.putMessageInMailbox(new Message(Message.SIMPLE_MESSAGE, argument_token, 0 /*addMessageDirector*/, new Object[]{md, token_positions[i]}));
		}
		return output_continuation;
	}

	public static Chameneos construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
		Chameneos actor = new Chameneos(target_stage);
		target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
}
