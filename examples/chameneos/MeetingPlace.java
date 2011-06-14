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


public class MeetingPlace extends salsa_lite.local_fcs.LocalActor {
	boolean did_evens = false;
	int max_meetings;
	int meetings;
	Chameneos[] chameneoses;
	Chameneos first = null;
	Colour first_colour;
	String[] numbers = new String[]{"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};


	public Object invokeMessage(int messageId, Object[] arguments) throws ContinuationPassException, TokenPassException, MessageHandlerNotFoundException {
		switch(messageId) {
			case 0: spell( (Integer)arguments[0] ); return null;
			case 1: printCompliments(); return null;
			case 2: printCompliment( (salsa_lite.examples.chameneos.Colour)arguments[0], (salsa_lite.examples.chameneos.Colour)arguments[1] ); return null;
			case 3: return doCompliment( (salsa_lite.examples.chameneos.Colour)arguments[0], (salsa_lite.examples.chameneos.Colour)arguments[1] );
			case 4: odds(); return null;
			case 5: evens(); return null;
			case 6: meet( (salsa_lite.examples.chameneos.Chameneos)arguments[0], (salsa_lite.examples.chameneos.Colour)arguments[1] ); return null;
			case 7: finished(); return null;
			case 8: end(); return null;
			default: throw new MessageHandlerNotFoundException(messageId, arguments);
		}
	}

	public void invokeConstructor(int messageId, Object[] arguments) throws ConstructorNotFoundException {
		switch(messageId) {
			case 0: construct( (java.lang.String[])arguments[0] ); return;
			default: throw new ConstructorNotFoundException(messageId, arguments);
		}
	}

	public void construct(String[] arguments) {
		max_meetings = Integer.parseInt(arguments[0]);
		meetings = max_meetings;
		ContinuationDirector continuation_token = StageService.sendContinuationMessage(this, 1 /*printCompliments*/, null);
		StageService.sendMessage(this, 4 /*odds*/, null, continuation_token);
	}



	public void spell(int value) {
		String s = Integer.toString(value);
		for (int i = 0; i < s.length(); i++) {
			System.out.print(" " + numbers[Character.getNumericValue(s.charAt(i))]);
		}

		System.out.println();
	}

	public void printCompliments() {
		this.printCompliment(Colour.blue, Colour.blue);
		this.printCompliment(Colour.blue, Colour.red);
		this.printCompliment(Colour.blue, Colour.yellow);
		this.printCompliment(Colour.red, Colour.blue);
		this.printCompliment(Colour.red, Colour.red);
		this.printCompliment(Colour.red, Colour.yellow);
		this.printCompliment(Colour.yellow, Colour.blue);
		this.printCompliment(Colour.yellow, Colour.red);
		this.printCompliment(Colour.yellow, Colour.yellow);
	}

	public void printCompliment(Colour c1, Colour c2) {
		System.out.println(c1 + " + " + c2 + " -> " + this.doCompliment(c1, c2));
	}

	public Colour doCompliment(Colour c1, Colour c2) {
		switch (c1) {
			case blue:
			switch (c2) {
				case blue:
				return Colour.blue;
				case red:
				return Colour.yellow;
				case yellow:
				return Colour.red;
			}

			case red:
			switch (c2) {
				case blue:
				return Colour.yellow;
				case red:
				return Colour.red;
				case yellow:
				return Colour.blue;
			}

			case yellow:
			switch (c2) {
				case blue:
				return Colour.red;
				case red:
				return Colour.blue;
				case yellow:
				return Colour.yellow;
			}

		}

		System.out.println("Cannot do compliment for unknown colours: " + c1 + ", " + c2);
		System.exit(0);
		return c1;
	}

	public void odds() {
		System.out.println("\nblue red yellow");
		chameneoses = new Chameneos[3];
		chameneoses[0] = Chameneos.construct(0, new Object[]{Colour.blue, this});
		chameneoses[1] = Chameneos.construct(0, new Object[]{Colour.red, this});
		chameneoses[2] = Chameneos.construct(0, new Object[]{Colour.yellow, this});
	}

	public void evens() {
		meetings = max_meetings;
		first = null;
		System.out.println("\nblue red yellow red yellow blue red yellow red blue");
		chameneoses = new Chameneos[10];
		chameneoses[0] = Chameneos.construct(0, new Object[]{Colour.blue, this});
		chameneoses[1] = Chameneos.construct(0, new Object[]{Colour.red, this});
		chameneoses[2] = Chameneos.construct(0, new Object[]{Colour.yellow, this});
		chameneoses[3] = Chameneos.construct(0, new Object[]{Colour.red, this});
		chameneoses[4] = Chameneos.construct(0, new Object[]{Colour.yellow, this});
		chameneoses[5] = Chameneos.construct(0, new Object[]{Colour.blue, this});
		chameneoses[6] = Chameneos.construct(0, new Object[]{Colour.red, this});
		chameneoses[7] = Chameneos.construct(0, new Object[]{Colour.yellow, this});
		chameneoses[8] = Chameneos.construct(0, new Object[]{Colour.red, this});
		chameneoses[9] = Chameneos.construct(0, new Object[]{Colour.blue, this});
	}

	public void meet(Chameneos chameneos, Colour colour) {
		if (meetings == 0) {
			return;
		} 
		if (first == null) {
			first = chameneos;
			first_colour = colour;
		}
		else {
			if (first == chameneos) {
				StageService.sendMessage(first, 1 /*left_self*/, new Object[]{this.doCompliment(first_colour, colour)});
				StageService.sendMessage(chameneos, 1 /*left_self*/, new Object[]{this.doCompliment(first_colour, colour)});
				first = null;
			}
			else {
				StageService.sendMessage(first, 0 /*left_other*/, new Object[]{this.doCompliment(first_colour, colour)});
				StageService.sendMessage(chameneos, 0 /*left_other*/, new Object[]{this.doCompliment(first_colour, colour)});
				first = null;
			}

			meetings--;
			if (meetings == 0) {
				StageService.sendMessage(this, 7 /*finished*/, null);
			} 
		}

	}

	public void finished() {
		if (!did_evens) {
			did_evens = true;
			TokenDirector ta = TokenDirector.construct(1, new Object[]{new Acknowledgement()});
			for (int i = 0; i < chameneoses.length; i++) {
				ta = StageService.sendTokenMessage(chameneoses[i], 2 /*output_meetings*/, null, new ContinuationDirector[]{ta});
			}

			ContinuationDirector continuation_token = StageService.sendContinuationMessage(this, 0 /*spell*/, new Object[]{max_meetings * 2}, new ContinuationDirector[]{ta});
			StageService.sendMessage(this, 5 /*evens*/, null, continuation_token);
		}
		else {
			TokenDirector ta = TokenDirector.construct(1, new Object[]{new Acknowledgement()});
			for (int i = 0; i < chameneoses.length; i++) {
				ta = StageService.sendTokenMessage(chameneoses[i], 2 /*output_meetings*/, null, new ContinuationDirector[]{ta});
			}

			ContinuationDirector continuation_token = StageService.sendContinuationMessage(this, 0 /*spell*/, new Object[]{max_meetings * 2}, new ContinuationDirector[]{ta});
			StageService.sendMessage(this, 8 /*end*/, null, continuation_token);
		}

	}

	public void end() {
		System.exit(0);
	}


	public MeetingPlace() { super(); }

	public MeetingPlace(SynchronousMailboxStage stage) { super(stage); }

	public static void main(String[] arguments) {
		construct(0, new Object[]{arguments}, StageService.getStage(0));
	}

	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions) {
		MeetingPlace actor = new MeetingPlace();
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

	public static MeetingPlace construct(int constructor_id, Object[] arguments) {
		MeetingPlace actor = new MeetingPlace();
		StageService.sendMessage(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
	public static TokenDirector construct(int constructor_id, Object[] arguments, int[] token_positions, SynchronousMailboxStage target_stage) {
		MeetingPlace actor = new MeetingPlace(target_stage);
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

	public static MeetingPlace construct(int constructor_id, Object[] arguments, SynchronousMailboxStage target_stage) {
		MeetingPlace actor = new MeetingPlace(target_stage);
		target_stage.putMessageInMailbox(new Message(Message.CONSTRUCT_MESSAGE, actor, constructor_id, arguments));
		return actor;
	}
}
