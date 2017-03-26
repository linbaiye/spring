package org.nalby.spring.bean;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


import org.nalby.spring.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Mainly to scan a document(xml file) and create beans defined inside the file.
 *
 */
public class XmlBeansHandler {
	
	private Map<String, XmlBeanDefinition> createdBeans;
	
	/* Beans wait to create. */
	private Map<String, XmlBeanDefinition> pendingBeans;
	
	private Document document;

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public static final String BEAN_ELEMENT = "bean";
	

	public XmlBeansHandler(Document document) {
		Assert.notNull(document, "Document can not be null.");
		this.document = document;
		this.pendingBeans = new HashMap<String, XmlBeanDefinition>();
		this.createdBeans = new HashMap<String, XmlBeanDefinition>();
	}
	
	/*
	 * Find all bean definitions in this.document and mark them as pending, throws an
	 * InvalidBeanConfigException if duplicated beans are found.
	 */
	private void scanBeanDefinitionsInDocument() {
		Element root = this.document.getDocumentElement();
		NodeList nodeList = root.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE && BEAN_ELEMENT.equalsIgnoreCase(node.getNodeName())) {
				XmlBeanDefinition beanDefinition = new XmlBeanDefinition((Element)node);
				beanDefinition.parseBeanDefinition();
				if (this.pendingBeans.containsKey(beanDefinition.getId())) {
					throw new InvalidBeanConfigException("Duplcated bean name found: " + beanDefinition.getId());
				}
				this.pendingBeans.put(beanDefinition.getId(), beanDefinition);
			}
		}
	}

	/*
	 * Notify all depending beans of the creation of dependent beans.
	 */
	private void notifyBeansCreation(List<XmlBeanDefinition> createdBeans) {
		for (XmlBeanDefinition beanDefinition: createdBeans) {
			for (XmlBeanDefinition pendingBean: this.pendingBeans.values()) {
				pendingBean.onExternalBeanCreated(beanDefinition);
			}
			this.createdBeans.put(beanDefinition.getId(), beanDefinition);
		}
	}
	
	private void initBeans() {
		boolean hasBeanCreated = false;
		List<XmlBeanDefinition> createdBeans = new LinkedList<XmlBeanDefinition>();
		do {
			hasBeanCreated = false;
			Iterator<Map.Entry<String, XmlBeanDefinition>> iterator = this.pendingBeans.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry<String, XmlBeanDefinition> entry = iterator.next();
				XmlBeanDefinition beanDefinition = entry.getValue();
				if (!beanDefinition.hasDependency()) {
					createdBeans.add(beanDefinition);
					iterator.remove();
					hasBeanCreated = true;
				}
			}
			if (hasBeanCreated) {
				notifyBeansCreation(createdBeans);
				createdBeans.clear();
			}
		} while(hasBeanCreated);
		if (!this.pendingBeans.isEmpty()) {
			throw new InvalidBeanConfigException("Not all beans could be created due to unresolved dependencies.");
		}
	}


	/**
	 * Resolve relations among bean definitions and create beans accordingly.
	 */
	public void createBeans() {
		try {
			scanBeanDefinitionsInDocument();
			initBeans();
		} catch (Exception e) {
			logger.error("Failed to create beans:", e);
			throw new InvalidBeanConfigException(e);
		} 
	}
	
	/**
	 * Get the bean by bean id.
	 * @param id the bean id.
	 * @return null if the bean id does not exist, the bean otherwise.
	 * @throws IllegalArgumentException if the id passed is empty.
	 */
	public Object getBean(String id) {
		Assert.notEmptyText(id, "Bean name can not be empty.");
		if (!this.createdBeans.containsKey(id)) {
			return null;
		}
		XmlBeanDefinition beanDefinition = this.createdBeans.get(id);
		return beanDefinition.getBean();
	}

}
