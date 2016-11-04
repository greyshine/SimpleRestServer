package de.greyshine.restservices;

public class ApplicationException extends RuntimeException {
	
	public enum EReason {
		
		UNKNOWN,
		INPUT_IS_NO_JSON;

	}

	private static final long serialVersionUID = -8170123775249546421L;
	public final EReason reason;
	
	public boolean isTechnicalError = true;
	
	
	public ApplicationException(EReason inReason, String inMessage) {
	
		this(inReason, inMessage, null);
	}
	
	public ApplicationException(EReason inReason, String inMessage, Exception inRootCause) {
		
		super( inMessage, inRootCause );
		
		reason = inReason == null ? EReason.UNKNOWN : inReason;
	}
	
	public ApplicationException technicalError(boolean b) {
		isTechnicalError = b;
		return this;
	}

	@Override
	public String toString() {
		return "ApplicationException [reason=" + reason + ", cause="+ getCause() +"]";
	}
	
	
	
	
	
}
