package org.nalby.spring.bean;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class XmlBeanHandlerTest {
	
	public static class Class1 {
	}
	
	public static class Class2 {
		private Class1 class1;
		public Class2(Class1 class1) {
			this.class1 = class1;
		}
		public Class1 getClass1() {
			return class1;
		}
	}
	

	private void displayDocument(Document document) {
	    try {
	       DOMSource domSource = new DOMSource(document);
	       StringWriter writer = new StringWriter();
	       StreamResult result = new StreamResult(writer);
	       TransformerFactory tf = TransformerFactory.newInstance();
	       Transformer transformer = tf.newTransformer();
	       transformer.transform(domSource, result);
	       System.out.println(writer.toString());
	    } catch(TransformerException ex) {
	       ex.printStackTrace();
	    }	
	}
	
	
	@Test(expected = NullPointerException.class)
	public void testWithNullArgConstructor() {
		new XmlBeansHandler(null);
	}

	
	@Test
	public void testWithEmptyDoc() throws ParserConfigurationException {
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element beans = document.createElement("beans");
		document.appendChild(beans);
		try {
			XmlBeansHandler handler = new XmlBeansHandler(document);
			handler.createBeans();
		} catch (Exception e) {
			fail();
		}
	}
	
	private Element createBeanElement(String id, String clazz, Document document) {
		Element element = document.createElement("bean");
		element.setAttribute("id", id);
		element.setAttribute("class", clazz);
		return element;
	}
	
	
	@Test
	public void testFlatTopography() throws ParserConfigurationException {
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element beans = document.createElement("beans");
		Element bean = createBeanElement("bean1", Class1.class.getName(), document);
		beans.appendChild(bean);
		bean = createBeanElement("bean2", Class1.class.getName(), document);
		beans.appendChild(bean);
		document.appendChild(beans);
		try {
			XmlBeansHandler handler = new XmlBeansHandler(document);
			handler.createBeans();
			Object object = handler.getBean("bean1");
			assertTrue(object instanceof Class1);
			object = handler.getBean("bean2");
			assertTrue(object instanceof Class1);
		} catch (Exception e) {
			fail();
		}
	}
	
	@Test
	public void testTreeTopography() throws ParserConfigurationException {
		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
		Element beans = document.createElement("beans");
		Element bean = createBeanElement("bean1", Class1.class.getName(), document);
		beans.appendChild(bean);
		bean = createBeanElement("bean2", Class2.class.getName(), document);
		Element ctorArg = document.createElement("constructor-arg");
		ctorArg.setAttribute("index", "0");
		ctorArg.setAttribute("ref", "bean1");
		bean.appendChild(ctorArg);
		beans.appendChild(bean);
		document.appendChild(beans);
		try {
			XmlBeansHandler handler = new XmlBeansHandler(document);
			handler.createBeans();
			Object object = handler.getBean("bean1");
			assertTrue(object instanceof Class1);
			object = handler.getBean("bean2");
			assertTrue(object instanceof Class2);
		} catch (Exception e) {
			fail();
		}
	}
	


}
