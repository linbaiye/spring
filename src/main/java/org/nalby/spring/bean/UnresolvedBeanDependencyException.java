package org.nalby.spring.bean;

/**
 * To be thrown when trying to create a bean which has unresolved dependencies.
 */
public class UnresolvedBeanDependencyException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	public UnresolvedBeanDependencyException(String message) {
		super(message);
	}
}
