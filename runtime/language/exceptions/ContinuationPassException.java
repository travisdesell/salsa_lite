package salsa_lite.runtime.language.exceptions;

import salsa_lite.runtime.language.ContinuationDirector;

public class ContinuationPassException extends Exception {

	public ContinuationDirector continuationDirector;

	public ContinuationPassException(ContinuationDirector continuationDirector) {
		super();
		this.continuationDirector = continuationDirector;
	}
}
