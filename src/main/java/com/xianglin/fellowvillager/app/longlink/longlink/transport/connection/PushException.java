package com.xianglin.fellowvillager.app.longlink.longlink.transport.connection;

import java.io.PrintStream;
import java.io.PrintWriter;

public class PushException extends Exception {
	
	private static final long serialVersionUID = 1L;
	
	public static final String PUSH_EXCEPTION_CONNFAIL = "50";
	public static final String PUSH_EXCEPTION_RESPTIMEOUT = "51";
	public static final String PUSH_EXCEPTION_WRITEERROR = "52";
	public static final String PUSH_EXCEPTION_STREAMEOS = "53";
	public static final String PUSH_EXCEPTION_CONNECEPTION = "54";

	private Throwable wrappedThrowable = null;
	
	private String type = "";

	/**
	 * Creates a new PushException.
	 */
	public PushException() {
		super();
	}

	/**
	 * Creates a new PushException with a description of the exception.
	 * 
	 * @param message
	 *            description of the exception.
	 */
	public PushException(String message) {
		super(message);
	}

	/**
	 * Creates a new PushException with the Throwable that was the root cause of
	 * the exception.
	 * 
	 * @param wrappedThrowable
	 *            the root cause of the exception.
	 */
	public PushException(Throwable wrappedThrowable) {
		super();
		this.wrappedThrowable = wrappedThrowable;
	}

	/**
	 * Creates a new PushException with a description of the exception and the
	 * Throwable that was the root cause of the exception.
	 * 
	 * @param message
	 *            a description of the exception.
	 * @param wrappedThrowable
	 *            the root cause of the exception.
	 */
	public PushException(String message, Throwable wrappedThrowable) {
		super(message);
		this.wrappedThrowable = wrappedThrowable;
	}

	/**
	 * Returns the Throwable asscociated with this exception, or <tt>null</tt>
	 * if there isn't one.
	 * 
	 * @return the Throwable asscociated with this exception.
	 */
	public Throwable getWrappedThrowable() {
		return wrappedThrowable;
	}

	public void printStackTrace() {
		printStackTrace(System.err);
	}

	public void printStackTrace(PrintStream out) {
		super.printStackTrace(out);

		if (wrappedThrowable != null) {
			out.println("Nested Exception: ");
			wrappedThrowable.printStackTrace(out);
		}
	}

	public void printStackTrace(PrintWriter out) {
		super.printStackTrace(out);

		if (wrappedThrowable != null) {
			out.println("Nested Exception: ");
			wrappedThrowable.printStackTrace(out);
		}
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String curType) {
		type = curType;
	}

	public String getMessage() {
		String msg = super.getMessage();
		return msg;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();

		String message = super.getMessage();
		if (message != null) {
			buf.append(message).append(": ");
		}

		if (wrappedThrowable != null) {
			buf.append("\n  -- caused by: ").append(wrappedThrowable);
		}

		return buf.toString();
	}
}