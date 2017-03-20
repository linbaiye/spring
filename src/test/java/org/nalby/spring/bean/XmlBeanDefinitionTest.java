package org.nalby.spring.bean;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlBeanDefinitionTest {
	
	@Test(expected = NullPointerException.class)
	public void testWithNullElement() {
		new XmlBeanDefinition(null);
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

}
