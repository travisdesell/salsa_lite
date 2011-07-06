package salsa_lite.wwc.language.exceptions;

import salsa_lite.wwc.language.ContinuationDirector;

public class ContinuationPassException extends Exception {

	public ContinuationDirector continuationDirector;

	public ContinuationPassException(ContinuationDirector continuationDirector) {
		super();
		this.continuationDirector = continuationDirector;
	}
}
