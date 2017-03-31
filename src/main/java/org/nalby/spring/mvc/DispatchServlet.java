package org.nalby.spring.mvc;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.nalby.spring.bean.XmlBeansHandler;
import org.nalby.spring.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

@SuppressWarnings("serial")
public class DispatchServlet extends HttpServlet {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	
	private void logExceptionAndThrowException(Exception e) throws ServletException {
		logger.error("Got exception:", e);
		throw new ServletException("Got an exception while initing servlet.");
	}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String configPath = config.getInitParameter("contextConfigLocation");
		Assert.notNull(configPath, "configure file path can't not be null.");
		configPath = configPath.replace("classpath:", "WEB-INF/classes/");
		logger.info("loading config file from:{}.", getServletContext().getRealPath(configPath));
		try {
			File fXmlFile = new File(getServletContext().getRealPath(configPath));
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			XmlBeansHandler xmlBeansHandler = new XmlBeansHandler(dBuilder.parse(fXmlFile));
			xmlBeansHandler.createBeans();
		} catch (ParserConfigurationException e) {
			logExceptionAndThrowException(e);
		} catch (SAXException e) {
			logExceptionAndThrowException(e);
		} catch (IOException e) {
			logExceptionAndThrowException(e);
		}
	}

}
