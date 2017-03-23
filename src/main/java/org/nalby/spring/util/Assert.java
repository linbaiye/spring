package org.nalby.spring.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A helper class that assists to validate arguments.
 * 
 */

public class Assert {
	
	private Assert() {
		/* Avoid being instantiated. */
	}
	
	/**
	 * Test if the object argument is null, throws a {@code NullPointerException} instantiated with the message argument if true.
	 * @param object the object to be validated.
	 * @param message the message for the exception.
	 * @throws NullPointerException if object is null.
	 */
	public static void notNull(Object object, String message) {
		if (object == null) {
			throw new NullPointerException(message == null ? "null pointer" : message);
		}
	}
	
	/**
	 * Test if a string is null or empty, throws a {@code IllegalArgumentException} constructed with the message if true.
	 * @param text the text to be validated.
	 * @throws IllegalArgumentException if text is null or empty.
	 */
	public static void notEmptyText(String text, String message) {
		if (text == null || "".equals(text)) {
			throw new IllegalArgumentException(message == null ? "text can not be empty" : message);
		}
	}
	
	/**
	 * Test if the given matches the given regex.
	 * @param text the text to be matched.
	 * @param regex the regex to match.
	 * @throws IllegalArgumentException if do not match.
	 */
	public static void textMatchsRegex(String text, String regex) {
		try {
			Pattern pattern = Pattern.compile(regex);
			Matcher matcher = pattern.matcher(text);
			if (matcher.matches()) {
				return;
			}
		} catch (Exception e) {
			//Ignore.
		}
		throw new IllegalArgumentException("text can not be matched.");
	}
	
	/**
	 * Test if a string is null or empty, throws a {@code IllegalArgumentException} if true.
	 * @param text the text to be validated.
	 * @throws IllegalArgumentException if text is null or empty.
	 */
	public static void notEmptyText(String text) {
		Assert.notEmptyText(text, "text can not be empty");
	}
	
	
}
