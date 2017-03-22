package org.nalby.spring.bean;

import org.nalby.spring.util.Assert;


/**
 * An argument for a bean constructor or a bean setter method.
 */

public class BeanArg {

	private String value;
	
	private BeanArgType type;
	
	public BeanArg(String value, BeanArgType type) {
		Assert.notNull(value, "Value can not be null");
		Assert.notNull(value, "Type can not be null");
		this.type = type;
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public BeanArgType getType() {
		return type;
	}
	
	/**
	 * Test if this argument is a reference to another bean.
	 * @return true if so, false if not.
	 */
	public boolean isReference() {
		return this.type == BeanArgType.REFERENCE;
	}
}
