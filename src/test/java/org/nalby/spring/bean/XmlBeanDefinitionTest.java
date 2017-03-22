package org.nalby.spring.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlBeanDefinitionTest {
	
	public static class DefaultConstructorClass {
		boolean valueSet = false;
		public boolean isValueSet() {
			return this.valueSet;
		}
	}

	public static class TargetClass extends DefaultConstructorClass{
		public TargetClass() {
			super();
		}
		public TargetClass(boolean b) {
			this.valueSet = b;
		}
	}
	
	private Element newElement() throws ParserConfigurationException {
		Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		return xmlDoc.createElement("bean");
	}
	
	private Element newElementWithId() throws ParserConfigurationException {
		Element element = newElement();
		element.setAttribute("id", "test");
		return element;
	}

	private Element setIdAndClass(Element element, Class<?> clazz) throws ParserConfigurationException {
		element.setAttribute("id", "test");
		element.setAttribute("class", clazz.getName());
		return element;
	}
	
	private Element newElementWithIdAndClass(Class<?> clazz) throws ParserConfigurationException {
		Element element = newElement();
		return setIdAndClass(element, clazz);
	}


	private Element newElmentWithConstructorValue(Class<?> clazz, String value) throws ParserConfigurationException {
		Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element beanElement = xmlDoc.createElement("bean");
		setIdAndClass(beanElement, clazz);
		Element constructorArgElement = xmlDoc.createElement("constructor-arg");
		constructorArgElement.setAttribute("index", "0");
		constructorArgElement.setAttribute("value", value);
		beanElement.appendChild(constructorArgElement);
		return beanElement;
	}
	
	@Test(expected = NullPointerException.class)
	public void testWithNullElement() {
		new XmlBeanDefinition(null);
	}
	
	@Test(expected = InvalidBeanConfigException.class)
	public void testWithNullId() throws ParserConfigurationException {
		Element item = newElement();
		new XmlBeanDefinition(item).parseBeanDefinition();
	}
	
	@Test(expected = InvalidBeanConfigException.class)
	public void testWithNullClass() throws ParserConfigurationException {
		Element item = newElementWithId();
		new XmlBeanDefinition(item).parseBeanDefinition();
	}
	
	@Test(expected = InvalidBeanConfigException.class)
	public void testWithInvalidClass() throws ParserConfigurationException {
		Element item = newElementWithId();
		item.setAttribute("class", "NoClass");
		new XmlBeanDefinition(item).parseBeanDefinition();
	}
	
	@Test
	public void testWithValidClass() throws ParserConfigurationException {
		Element item = newElementWithId();
		item.setAttribute("class", getClass().getName());
		new XmlBeanDefinition(item).parseBeanDefinition();
	}
	
	/*
	 * Make sure a bean can be created if no explicit constructors defined.
	 */
	@Test
	public void testWithDefaultConstructor() throws ParserConfigurationException {
		XmlBeanDefinition xmlBeanDefinition = new XmlBeanDefinition(newElementWithIdAndClass(DefaultConstructorClass.class));
		xmlBeanDefinition.parseBeanDefinition();
		Object object = xmlBeanDefinition.getBean();
		assertTrue(object instanceof DefaultConstructorClass);
		DefaultConstructorClass instance = (DefaultConstructorClass) object;
		assertFalse(instance.isValueSet());
	}
	
	/*
	 * Make sure a bean can be created using no arg constructor.
	 */
	@Test
	public void testWithNoArgConstructor() throws ParserConfigurationException {
		XmlBeanDefinition xmlBeanDefinition = new XmlBeanDefinition(newElementWithIdAndClass(TargetClass.class));
		xmlBeanDefinition.parseBeanDefinition();
		Object object = xmlBeanDefinition.getBean();
		assertTrue(object instanceof TargetClass);
		TargetClass instance = (TargetClass) object;
		assertFalse(instance.isValueSet());
	}
	

	@Test
	public void testWithArgedConstructor() throws ParserConfigurationException {
		XmlBeanDefinition xmlBeanDefinition = new XmlBeanDefinition(newElmentWithConstructorValue(TargetClass.class, "true"));
		xmlBeanDefinition.parseBeanDefinition();
		Object object = xmlBeanDefinition.getBean();
		assertTrue(object instanceof TargetClass);
		TargetClass instance = (TargetClass) object;
		assertTrue(instance.isValueSet());
	}
	
	

}
