package salsa_lite.local_fcs.language.exceptions;

import salsa_lite.local_fcs.language.ContinuationDirector;

public class ContinuationPassException extends Exception {

	public ContinuationDirector continuationDirector;

	public ContinuationPassException(ContinuationDirector continuationDirector) {
		super();
		this.continuationDirector = continuationDirector;
	}
}
