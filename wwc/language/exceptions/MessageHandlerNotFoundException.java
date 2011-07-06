package salsa_lite.wwc.language.exceptions;

public class MessageHandlerNotFoundException extends Exception {

	String[] argTypes;
	String[] argValues;
	int messageId;

	public MessageHandlerNotFoundException(int messageId, Object[] arguments) {
		this.messageId = messageId;
		argTypes = new String[arguments.length];
		argValues = new String[arguments.length];
		for (int i = 0; i < argTypes.length; i++) {
			argTypes[i] = arguments[i].getClass().getName();
			argValues[i] = arguments[i].toString();
		}
	}

	public String toString() {
		String description = "MessageHandlerNotFoundException: could not find message: " + messageId + "\n";
		description += "\targument types: ";
		for (int i = 0; i < argTypes.length; i++) description += argTypes[i] + " ";
		description += "\n\targument values: ";
		for (int i = 0; i < argValues.length; i++) description += argValues[i] + " ";
		return description;
	}
}
