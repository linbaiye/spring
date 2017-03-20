package org.nalby.spring.bean;

import org.nalby.spring.util.Assert;


/**
 * An argument for a bean constructor or a bean setter method.
 * @author lintao
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
}
