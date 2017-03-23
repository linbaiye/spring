package org.nalby.spring.bean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.security.spec.ECField;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
	
	
	/* Build an argument for constructor. */
	private Map<String, String> newConstructorArg(String index, String type, String value) {
		Map<String, String> arg = new HashMap<String, String>();
		arg.put("index", index);
		arg.put(type, value);
		return arg;
	}


	private Element newElmentWithConstructorArgs(Class<?> clazz, List<Map<String, String>>constructorArgs) throws ParserConfigurationException {
		Document xmlDoc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element beanElement = xmlDoc.createElement("bean");
		setIdAndClass(beanElement, clazz);
		for (Map<String, String> map: constructorArgs) {
			Element constructorArgElement = xmlDoc.createElement("constructor-arg");
			for (String key: map.keySet()) {
				constructorArgElement.setAttribute(key, map.get(key));
			}
			beanElement.appendChild(constructorArgElement);
		}
		return beanElement;
	}
	
	
	private void assertThrowInvalidConfigException(Element element) {
		try {
			XmlBeanDefinition xmlBeanDefinition = new XmlBeanDefinition(element);
			xmlBeanDefinition.parseBeanDefinition();
			fail("An InvalidBeanConfigException should be thrown.");
		} catch (InvalidBeanConfigException e) {
			/* Expected. */
			return;
		}
	}
	
	@Test(expected = NullPointerException.class)
	public void testWithNullElement() {
		new XmlBeanDefinition(null);
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
		new XmlBeanDefinition(item).parseBeanDefinition();
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
		XmlBeanDefinition xmlBeanDefinition = new XmlBeanDefinition(newElementWithIdAndClass(DefaultConstructorClass.class));
		xmlBeanDefinition.parseBeanDefinition();
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
		XmlBeanDefinition xmlBeanDefinition = new XmlBeanDefinition(newElementWithIdAndClass(TargetClass.class));
		xmlBeanDefinition.parseBeanDefinition();
		Object object = xmlBeanDefinition.getBean();
		assertTrue(object instanceof TargetClass);
		TargetClass instance = (TargetClass) object;
		assertFalse(instance.isValueSet());

		xmlBeanDefinition = new XmlBeanDefinition(newElementWithIdAndClass(NoArgConstructorClass.class));
		xmlBeanDefinition.parseBeanDefinition();
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
		XmlBeanDefinition xmlBeanDefinition = new XmlBeanDefinition(newElmentWithConstructorArgs(TargetClass.class, args));
		xmlBeanDefinition.parseBeanDefinition();
		Object object = xmlBeanDefinition.getBean();
		assertTrue(object instanceof TargetClass);
		TargetClass instance = (TargetClass) object;
		assertTrue(instance.isValueSet());
		
		args.clear();
		args.add(newConstructorArg("0", "value", "true"));
		args.add(newConstructorArg("1", "value", "Hello"));
		xmlBeanDefinition = new XmlBeanDefinition(newElmentWithConstructorArgs(TwoArgClass.class, args));
		xmlBeanDefinition.parseBeanDefinition();
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
		XmlBeanDefinition xmlBeanDefinition = new XmlBeanDefinition(newElmentWithConstructorArgs(TargetClass.class, args));
		xmlBeanDefinition.parseBeanDefinition();
		xmlBeanDefinition.getBean();
	}
	
	@Test
	public void testWithDependency() throws ParserConfigurationException {
		List<Map<String, String>> args = new LinkedList<Map<String,String>>();
		args.add(newConstructorArg("0", "value", "true"));
		args.add(newConstructorArg("1", "ref", "referredBean"));
		//The bean we want to create.
		XmlBeanDefinition targetBeanDefinition = new XmlBeanDefinition(newElmentWithConstructorArgs(TwoArgClass.class, args));
		targetBeanDefinition.parseBeanDefinition();
		assertTrue(targetBeanDefinition.hasDependency());

		XmlBeanDefinition dependingDefinition = new XmlBeanDefinition(newElementWithIdAndClass("referredBean", DefaultConstructorClass.class));
		dependingDefinition.parseBeanDefinition();
		targetBeanDefinition.onExternalBeanCreated(dependingDefinition);
		TwoArgClass targetBean = (TwoArgClass) targetBeanDefinition.getBean();
		assertTrue(targetBean.isValueSet());
		assertTrue(targetBean.getClz() == dependingDefinition.getBean());
	}
	
}
