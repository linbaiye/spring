package org.nalby.spring.mvc;

import java.util.jar.Attributes.Name;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("serial")
public class DispatchServlet extends HttpServlet {
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

}
