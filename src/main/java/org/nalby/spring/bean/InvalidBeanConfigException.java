package org.nalby.spring.bean;

/**
 * An exception to throw when parsing invalid xml bean configuration.
 * @author lintao
 */
public class InvalidBeanConfigException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	public InvalidBeanConfigException(String message) {
		super(message);
	}
	
	public InvalidBeanConfigException(Throwable throwable) {
		super(throwable);
	}
}
