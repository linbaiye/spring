package org.nalby.spring.bean;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.nalby.spring.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlBeansHandler {
	
	private String xmlFilepath;
	
	private Map<String, Object> beans;

	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	public XmlBeansHandler(String xmlFilepath) {
		Assert.notNull(xmlFilepath, "XML filepath can not be null.");
		this.xmlFilepath = xmlFilepath;
		this.beans = new ConcurrentHashMap<String, Object>();
	}
	
	private void parseBeansRelations() {
		
	}

	
	/**
	 * Resolves relations among bean definitions and creates them accordingly.
	 */
	public void createBeans() {
		
	}

}
