package org.nalby.spring.bean;


import java.util.HashMap;
import java.util.Map;

import org.nalby.spring.util.Assert;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlBeanDefinition {
	public static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";
	public static final String PROPERTY_ELEMENT = "property";

	private String id;
	private Class<?> clazz;
	private Map<Integer, BeanArg> constructorArgs;
	private Map<String, BeanArg> properties;
	
	private Element element;
	
	public XmlBeanDefinition(Element element) {
		Assert.notNull(element, "bean element can not be null");
		this.element = element;
		constructorArgs = new HashMap<Integer, BeanArg>();
		properties = new HashMap<String, BeanArg>();
	}
	
	
	private void parseConstructorArg(Element element) {
		String index = element.getAttribute("index");
		Assert.notEmptyText(index, "index can not be null");
		String value = element.getAttribute("ref");
		BeanArgType type = BeanArgType.REFERENCE;
		if (value == null || "".equals(value)) {
			value = element.getAttribute("value");
			type = BeanArgType.VALUE;
		}
		Assert.notEmptyText(value, "netheir value nor ref was found");
		constructorArgs.put(Integer.parseInt(index), new BeanArg(value, type));
	}

	
	private void parsePropertyArg(Element element) {
		String name = element.getAttribute("name");
		Assert.notEmptyText(name, "name for property can not be null");
		String value = element.getAttribute("ref");
		BeanArgType type = BeanArgType.REFERENCE;
		if (value == null || "".equals(value)) {
			value = element.getAttribute("value");
			type = BeanArgType.VALUE;
		}
		Assert.notEmptyText(value, "netheir value nor ref was found");
		properties.put(name, new BeanArg(value, type));
	}
	
	/*
	 * Figure out all the beans this bean relies on.
	 */
	private void parseDependecies() {
		NodeList nodeList = element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && CONSTRUCTOR_ARG_ELEMENT.equalsIgnoreCase(node.getNodeName())) {
				parseConstructorArg(element);
			} else if (node.getNodeType() == Node.ELEMENT_NODE && PROPERTY_ELEMENT.equalsIgnoreCase(node.getNodeName())) {
				parsePropertyArg(element);
			}
		}
	}
	
	
	/**
	 * Parse the bean element in order to resolve dependent beans/values.
	 * @throws InvalidBeanConfigException if the bean element is mis-configured.
	 */
	public void parseBeanDefinition() {
		try {
			this.id = element.getAttribute("id");
			Assert.notEmptyText(this.id, "bean id can not be null");
			String className = element.getAttribute("class");
			this.clazz = Class.forName(className);
			parseDependecies();
		} catch (ClassNotFoundException e) {
			throw new InvalidBeanConfigException("Invalid bean class");
		} catch (Exception e) {
			throw new InvalidBeanConfigException(e);
		}
	}

}
