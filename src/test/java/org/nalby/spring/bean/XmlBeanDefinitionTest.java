package org.nalby.spring.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlBeanDefinitionTest {
	
	public static class DefaultConstructorClass {
		boolean valueSet = false;
		private String str;
		public boolean isValueSet() {
			return this.valueSet;
		}

		public void setStr(String s) {
			str = s;
		}
		
		public String getStr() {
			return str;
		}
	}
	
	public static class SetterClass {
		private String string;
		private int number;
		public SetterClass() {}
		public SetterClass(String str) {
			this.string = str;
		}
		public String getString() {
			return string;
		}
		public void setString(String string) {
			this.string = string;
		}
		public int getNumber() {
			return number;
		}
		public void setNumber(int number) {
			this.number = number;
		}
	}
	
	public static abstract class AbsctractClass {
	}
	
	public static interface Interface {
	}
	
	public static @interface Annotation {
	}
	
	public static enum Enum {
	}


	public static class TargetClass extends DefaultConstructorClass{
		public TargetClass() {
			super();
		}
		public TargetClass(boolean b) {
			this.valueSet = b;
		}
	}
	
	public static class NoArgConstructorClass extends DefaultConstructorClass {
		public NoArgConstructorClass() {
			this.valueSet = true;
		}
	}

	public static class TwoArgClass extends DefaultConstructorClass{
		private String str;
		private DefaultConstructorClass clz;
		public TwoArgClass(boolean b, String arg) {
			this.valueSet = b;
			this.str = arg;
		}

		public TwoArgClass(boolean b, DefaultConstructorClass clz) {
			this.valueSet = b;
			this.clz = clz;
		}

		public String getStr() {
			return this.str;
		}

		public DefaultConstructorClass getClz() {
			return clz;
		}
	}

	
	public static class DependingClass {
		private DefaultConstructorClass dependency1;
		private DefaultConstructorClass dependency2;
		public DependingClass(DefaultConstructorClass class1, DefaultConstructorClass class2) {
			this.dependency1 = class1;
			this.dependency2 = class2;
		}
		public boolean hasObject(DefaultConstructorClass obj) {
			return (dependency1 == obj || dependency2 == obj) && obj != null;
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
	
	private Element newElementWithIdAndClass(String id, Class<?> clazz) throws ParserConfigurationException {
		Element element = newElement();
		element.setAttribute("id", id); 
		element.setAttribute("class", clazz.getName());
		return element;
	}
	
	private Map<String, String> newArg(String nameKey, String nameValue, String typeKey, String typeValue) {
		Map<String, String> arg = new HashMap<String, String>();
		arg.put(nameKey, nameValue);
		arg.put(typeKey, typeValue);
		return arg;
	}
	
	
	/* Build an argument for constructor. */
	private Map<String, String> newConstructorArg(String index, String type, String value) {
		return newArg("index", index, type, value);
	}
	
	private Map<String, String> newPropertyArg(String name, String type, String value) {
		return newArg("name", name, type, value);
	}
	
	
	private Element newBeanElementWithArgs(Document xmlDoc, Class<?> clazz, 
			List<Map<String, String>> ctorArgs, List<Map<String, String>> propertyArgs) throws ParserConfigurationException {
		Element beanElement = xmlDoc.createElement("bean");
		setIdAndClass(beanElement, clazz);
		for (Map<String, String> map: ctorArgs) {
			Element argElement = xmlDoc.createElement("constructor-arg");
			for (String key: map.keySet()) {
				argElement.setAttribute(key, map.get(key));
			}
			beanElement.appendChild(argElement);
		}
		for (Map<String, String> map: propertyArgs) {
			Element argElement = xmlDoc.createElement("property");
			for (String key: map.keySet()) {
				argElement.setAttribute(key, map.get(key));
			}
			beanElement.appendChild(argElement);
		}
		return beanElement;
	}
	
	
	private Element newElementWithSubElements(Class<?> clazz, List<Map<String, String>> subElementAttrs, String subElement) throws ParserConfigurationException {
		Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element beanElement = xmlDoc.createElement("bean");
		setIdAndClass(beanElement, clazz);
		for (Map<String, String> map: subElementAttrs) {
			Element argElement = xmlDoc.createElement(subElement);
			for (String key: map.keySet()) {
				argElement.setAttribute(key, map.get(key));
			}
			beanElement.appendChild(argElement);
		}
		return beanElement;
	}
	
	private Element newElementWithProperties(Class<?> clazz, List<Map<String, String>>propertieAgs) throws ParserConfigurationException {
		return newElementWithSubElements(clazz, propertieAgs, "property");
	}


	private Element newElmentWithConstructorArgs(Class<?> clazz, List<Map<String, String>>constructorArgs) throws ParserConfigurationException {
		return newElementWithSubElements(clazz, constructorArgs, "constructor-arg");
	}
	
	
	private void assertThrowInvalidConfigException(Element element) {
		try {
			XmlBeanDefinition.parseXmlBeanElement(element).getBean();
			fail("An InvalidBeanConfigException should be thrown.");
		} catch (InvalidBeanConfigException e) {
			/* Expected. */
		}
	}
	
	@Test(expected = NullPointerException.class)
	public void testWithNullElement() {
		XmlBeanDefinition.parseXmlBeanElement(null);
	}
	
	@Test
	public void testWithNullId() throws ParserConfigurationException {
		assertThrowInvalidConfigException(newElement());
	}
	
	@Test
	public void testWithNullClass() throws ParserConfigurationException {
		assertThrowInvalidConfigException(newElementWithId());
	}
	
	@Test
	public void testWithInvalidClass() throws ParserConfigurationException {
		Element item = newElementWithId();
		item.setAttribute("class", "NoClass");
		assertThrowInvalidConfigException(item);
	}
	
	@Test
	public void testWithValidClass() throws ParserConfigurationException {
		Element item = newElementWithId();
		item.setAttribute("class", getClass().getName());
		XmlBeanDefinition.parseXmlBeanElement(item);
	}
	
	@Test
	public void testWithAbstractClass() throws ParserConfigurationException {
		Element item = newElementWithId();
		item.setAttribute("class", AbsctractClass.class.getName());
		assertThrowInvalidConfigException(item);
		item.setAttribute("class", Interface.class.getName());
		assertThrowInvalidConfigException(item);
		item.setAttribute("class", Annotation.class.getName());
		assertThrowInvalidConfigException(item);
		item.setAttribute("class", Enum.class.getName());
		assertThrowInvalidConfigException(item);
	}
	
	/*
	 * Make sure a bean can be created if no explicit constructors defined.
	 */
	@Test
	public void testWithDefaultConstructor() throws ParserConfigurationException {
		XmlBeanDefinition xmlBeanDefinition = XmlBeanDefinition.parseXmlBeanElement(newElementWithIdAndClass(DefaultConstructorClass.class));
		Object object = xmlBeanDefinition.getBean();
		assertTrue(object instanceof DefaultConstructorClass);
		DefaultConstructorClass instance = (DefaultConstructorClass) object;
		assertFalse(instance.isValueSet());
	}
	
	/*
	 * Make sure a bean can be created using no-arg constructor.
	 */
	@Test
	public void testWithNoArgConstructor() throws ParserConfigurationException {
		XmlBeanDefinition xmlBeanDefinition = XmlBeanDefinition.parseXmlBeanElement(newElementWithIdAndClass(TargetClass.class));
		Object object = xmlBeanDefinition.getBean();
		assertTrue(object instanceof TargetClass);
		TargetClass instance = (TargetClass) object;
		assertFalse(instance.isValueSet());

		xmlBeanDefinition = XmlBeanDefinition.parseXmlBeanElement(newElementWithIdAndClass(NoArgConstructorClass.class));
		object = xmlBeanDefinition.getBean();
		NoArgConstructorClass bean = (NoArgConstructorClass) object;
		assertTrue(bean.isValueSet());
	}


	/*
	 * Test with multiple non-customized arguments.
	 */
	@Test
	public void testWithArgedConstructor() throws ParserConfigurationException {
		List<Map<String, String>> args = new LinkedList<Map<String,String>>();
		args.add(newConstructorArg("0", "value", "true"));
		BeanDefinition xmlBeanDefinition = XmlBeanDefinition.parseXmlBeanElement(newElmentWithConstructorArgs(TargetClass.class, args));
		Object object = xmlBeanDefinition.getBean();
		assertTrue(object instanceof TargetClass);
		TargetClass instance = (TargetClass) object;
		assertTrue(instance.isValueSet());
		
		args.clear();
		args.add(newConstructorArg("0", "value", "true"));
		args.add(newConstructorArg("1", "value", "Hello"));
		xmlBeanDefinition = XmlBeanDefinition.parseXmlBeanElement(newElmentWithConstructorArgs(TwoArgClass.class, args));
		TwoArgClass twoArgClass = (TwoArgClass) xmlBeanDefinition.getBean();
		assertTrue("Hello".equals(twoArgClass.getStr()));
		assertTrue(twoArgClass.isValueSet());
	}

	
	@Test
	public void testDuplicatedArgs() throws ParserConfigurationException {
		List<Map<String, String>> args = new LinkedList<Map<String,String>>();
		args.add(newConstructorArg("0", "ref", "refBean"));
		args.add(newConstructorArg("0", "ref", "refBean"));
		assertThrowInvalidConfigException(newElmentWithConstructorArgs(TargetClass.class, args));
	}
	
	@Test(expected = UnresolvedBeanDependencyException.class)
	public void testUnsolvedArg() throws ParserConfigurationException {
		List<Map<String, String>> args = new LinkedList<Map<String,String>>();
		args.add(newConstructorArg("0", "ref", "refBean"));
		XmlBeanDefinition xmlBeanDefinition = XmlBeanDefinition.parseXmlBeanElement(newElmentWithConstructorArgs(TargetClass.class, args));
		xmlBeanDefinition.getBean();
	}
	
	@Test
	public void testWithDependency() throws ParserConfigurationException {
		List<Map<String, String>> args = new LinkedList<Map<String,String>>();
		args.add(newConstructorArg("0", "value", "true"));
		args.add(newConstructorArg("1", "ref", "referredBean"));
		//The bean we want to create.
		XmlBeanDefinition targetBeanDefinition = XmlBeanDefinition.parseXmlBeanElement(newElmentWithConstructorArgs(TwoArgClass.class, args));
		assertTrue(targetBeanDefinition.hasUnresolvedDependency());

		XmlBeanDefinition dependingDefinition = XmlBeanDefinition.parseXmlBeanElement(newElementWithIdAndClass("referredBean", DefaultConstructorClass.class));
		targetBeanDefinition.onOtherBeanCreated(dependingDefinition);
		TwoArgClass targetBean = (TwoArgClass) targetBeanDefinition.getBean();
		assertTrue(targetBean.isValueSet());
		assertTrue(targetBean.getClz() == dependingDefinition.getBean());
	}
	
	@Test
	public void testWithProperties() throws ParserConfigurationException {
		List<Map<String, String>> args = new LinkedList<Map<String,String>>();
		args.add(newPropertyArg("number", "value", "error."));
		Element element = newElementWithProperties(SetterClass.class, args);
		assertThrowInvalidConfigException(element);
		args.clear();
		args.add(newPropertyArg("number", "value", "100"));
		args.add(newPropertyArg("string", "value", "100"));
		element = newElementWithProperties(SetterClass.class, args);
		XmlBeanDefinition definition = XmlBeanDefinition.parseXmlBeanElement(element);
		SetterClass bean = (SetterClass) definition.getBean();
		assertTrue(bean.getNumber() == 100);
		assertTrue("100".equals(bean.getString()));
	}
	
	@Test
	public void testConstructorWithProperties() throws ParserConfigurationException {
		Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		List<Map<String, String>> ctorArgs = new LinkedList<Map<String,String>>();
		ctorArgs.add(newConstructorArg("0", "value", "hello"));
		List<Map<String, String>> propertyArgs = new LinkedList<Map<String,String>>();
		propertyArgs.add(newPropertyArg("number", "value", "100"));
		Element element = newBeanElementWithArgs(xmlDoc, SetterClass.class, ctorArgs, propertyArgs);
		BeanDefinition beanDefinition = XmlBeanDefinition.parseXmlBeanElement(element);
		SetterClass bean = (SetterClass) beanDefinition.getBean();
		assertTrue(bean.getNumber() == 100);
		assertTrue("hello".equals(bean.getString()));
	}
	
	
}
