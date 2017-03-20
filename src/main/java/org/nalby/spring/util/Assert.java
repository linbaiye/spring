package org.nalby.spring.util;

/**
 * A helper class that assists to validate arguments.
 * 
 * @author Lin Tao
 *
 */

public class Assert {
	
	
	/**
	 * Tests if the object argument is null, throws a {@code NullPointerException} instantiated with the message argument if true.
	 * @param object the object to be validated.
	 * @param message the message for the exception.
	 * @throws NullPointerException if object is null.
	 */
	public static void notNull(Object object, String message) {
		if (object == null) {
			throw new NullPointerException(message == null ? "null pointer" : message);
		}
	}
	
	public static void notEmptyText(String text, String message) {
		if (text == null || "".equals(text)) {
			throw new IllegalArgumentException(message == null ? "text can not be empty" : message);
		}
	}
	
	/**
	 * Tests if a string is null or empty, throws a {@code IllegalArgumentException} if true.
	 * 
	 * @param text the text to be validated.
	 * @throws IllegalArgumentException if text is null or text is empty.
	 */
	public static void notEmptyText(String text) {
		Assert.notEmptyText(text, "text can not be empty");
	}
	
	
}
