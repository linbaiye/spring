package org.nalby.spring.bean;


import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.nalby.spring.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class to describe a bean including id, class, and dependent beans or values.
 * Currently it can only create a singleton bean and is not thread-safe.
 */
public class XmlBeanDefinition {
	/**
	 * Constructor argument element, used to describe constructor argument.
	 */
	public static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";
	/**
	 * Bean property element, used to describe what setters should be invoked.
	 */
	public static final String PROPERTY_ELEMENT = "property";

	// bean id
	private String id;
	private Class<?> clazz;
	private Map<Integer, BeanArg> constructorArgs;
	private Map<String, BeanArg> properties;
	// Beans this bean depends on.
	private List<String> dependentBeanNames;
	private Map<String, XmlBeanDefinition> dependentBeans;

	private static Map<Class<?>, Class<?>> primitiveTypeConverter = new HashMap<Class<?>, Class<?>>();
	private static final Logger logger = LoggerFactory.getLogger(XmlBeanDefinition.class);

	static {
		primitiveTypeConverter.put(boolean.class, Boolean.class);
		primitiveTypeConverter.put(short.class, Short.class);
		primitiveTypeConverter.put(int.class, Integer.class);
		primitiveTypeConverter.put(long.class, Long.class);
		primitiveTypeConverter.put(float.class, Float.class);
		primitiveTypeConverter.put(double.class, Double.class);
		primitiveTypeConverter.put(char.class, Character.class);
		primitiveTypeConverter.put(byte.class, Byte.class);
	}

	private Object bean;

	private Element element;

	public XmlBeanDefinition(Element element) {
		Assert.notNull(element, "bean element can not be null");
		this.element = element;
		constructorArgs = new HashMap<Integer, BeanArg>();
		properties = new HashMap<String, BeanArg>();
		dependentBeanNames = new LinkedList<String>();
		dependentBeans = new HashMap<String, XmlBeanDefinition>();
	}

	public String getId() {
		return id;
	}

	private BeanArg parseBeanArg(Element element) {
		String value = element.getAttribute("value");
		BeanArgType type = BeanArgType.VALUE;
		if (value == null || "".equals(value)) {
			value = element.getAttribute("ref");
			type = BeanArgType.REFERENCE;
		}
		Assert.notEmptyText(value, "netheir value nor ref was found");
		if (type == BeanArgType.REFERENCE) {
			dependentBeanNames.add(value);
		}
		return new BeanArg(value, type);
	}

	private void parseConstructorArg(Element e) {
		String index = e.getAttribute("index");
		Assert.notEmptyText(index, "index can not be null");
		Integer indexedKey = Integer.parseInt(index);
		if (this.constructorArgs.containsKey(indexedKey)) {
			throw new InvalidBeanConfigException("Duplicated index.");
		}
		this.constructorArgs.put(indexedKey, parseBeanArg(e));
	}

	private void parsePropertyArg(Element e) {
		String name = e.getAttribute("name");
		Assert.notEmptyText(name, "name for property can not be null");
		if (this.properties.containsKey(name)) {
			throw new InvalidBeanConfigException("Duplicated property names.");
		}
		this.properties.put(name, parseBeanArg(e));
	}

	/*
	 * Figure out all the beans this bean relies on.
	 */
	private void parseDependecies() {
		NodeList nodeList = this.element.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() == Node.ELEMENT_NODE
					&& CONSTRUCTOR_ARG_ELEMENT.equalsIgnoreCase(node.getNodeName())) {
				parseConstructorArg((Element)node);
			} else if (node.getNodeType() == Node.ELEMENT_NODE
					&& PROPERTY_ELEMENT.equalsIgnoreCase(node.getNodeName())) {
				parsePropertyArg((Element)node);
			}
		}
	}

	/**
	 * To be called when another bean is created, so that dependent beans could
	 * be eliminated.
	 */
	public void onExternalBeanCreated(XmlBeanDefinition beanDefinition) {
		while (this.dependentBeanNames.remove(beanDefinition.getId()));
		this.dependentBeans.put(beanDefinition.getId(), beanDefinition);
	}

	/**
	 * Test if this bean depends on other beans.
	 * @return true if so.
	 */
	public boolean hasDependency() {
		return !dependentBeanNames.isEmpty();
	}

	/*
	 * Get the nth constructor argument value.
	 */
	private Object getNthConstructorArg(int n, Class<?> nthArgType) throws Exception {
		BeanArg arg = this.constructorArgs.get(n);
		if (arg.isReference()) {
			if (!this.dependentBeans.containsKey(arg.getValue())) {
				throw new InvalidBeanConfigException(
						n + "th argument is configured as a bean-ref, but the refered bean can not be found.");
			}
			XmlBeanDefinition beanArgDefinition = this.dependentBeans.get(arg.getValue());
			return beanArgDefinition.getBean();
		}
		if (nthArgType == String.class) {
			return arg.getValue();
		}
		if (!primitiveTypeConverter.containsKey(nthArgType)) {
			throw new InvalidBeanConfigException(n + "th constructor argument is not recognised.");
		}
		Class<?> nthArgClass = primitiveTypeConverter.get(nthArgType);
		Constructor<?> argConstructor = nthArgClass.getConstructor(String.class);
		return argConstructor.newInstance(arg.getValue());
	}


	private Object createBean() {
		try {
			Constructor<?>[] beanConstructors = this.clazz.getConstructors();
			if (beanConstructors.length == 0 && this.dependentBeans.isEmpty()) {
				return this.clazz.newInstance();
			}
			for (Constructor<?> beanConstructor : beanConstructors) {
				Class<?>[] constructorParamTypes = beanConstructor.getParameterTypes();
				if (constructorParamTypes.length != this.constructorArgs.size()) {
					continue;
				}
				Object[] params = new Object[constructorParamTypes.length];
				for (int i = 0; i < constructorParamTypes.length; i++) {
					params[i] = getNthConstructorArg(i, constructorParamTypes[i]);
				}
				try {
					this.bean = beanConstructor.newInstance(params);
					if (this.bean != null) {
						return this.bean;
					}
				} catch (Exception e) {
					// Ignore it since constructors may be overload with arguments of the same number.
				}
			}
		} catch (Throwable e) {
			logger.error("Got an expection while creating the bean of " + this.id, e);
			throw new UnresolvedBeanDependencyException(e.getMessage());
		}
		throw new UnresolvedBeanDependencyException("Can not find appropriate constructor.");
	}

	/**
	 * Instantiate the bean.
	 * @throws UnresolvedBeanDependencyException if bean has unresolved dependencies.
	 */
	public Object getBean() {
		if (this.hasDependency()) {
			throw new UnresolvedBeanDependencyException(
					"Bean " + id + " can not be created because unresolved dependencies.");
		}
		if (this.bean != null) {
			return this.bean;
		}
		this.bean = createBean();
		return this.bean;
	}
	
	/**
	 * Parse the bean element in order to resolve dependent beans/values.
	 * @throws InvalidBeanConfigException if the bean element is mis-configured.
	 */
	public void parseBeanDefinition() {
		try {
			this.id = this.element.getAttribute("id");
			Assert.notEmptyText(this.id, "bean id can not be null");
			Assert.textMatchsRegex(this.id, "[a-zA-Z][0-9a-zA-Z]+");
			String className = this.element.getAttribute("class");
			this.clazz = Class.forName(className);
			parseDependecies();
		} catch (ClassNotFoundException e) {
			throw new InvalidBeanConfigException("Invalid bean class");
		} catch (UnresolvedBeanDependencyException e) {
			throw e;
		}catch (Exception e) {
			throw new InvalidBeanConfigException(e);
		}
	}

}
