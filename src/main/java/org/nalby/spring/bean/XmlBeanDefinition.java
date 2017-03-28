package org.nalby.spring.bean;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
public class XmlBeanDefinition implements BeanDefinition {
	/**
	 * Constructor argument element, used to describe constructor argument.
	 */
	private static final String CONSTRUCTOR_ARG_ELEMENT = "constructor-arg";
	/**
	 * Bean property element, used to describe what setters should be invoked.
	 */
	private static final String PROPERTY_ELEMENT = "property";

	private static final Logger logger = LoggerFactory.getLogger(XmlBeanDefinition.class);
	
	// bean id
	private String id = null;

	private Class<?> clazz = null;
	private Map<String, BeanArg> propertyArgs;
	
	private Map<String, BeanArg> ctorArgs;
	// Beans this bean depends on.
	private List<String> dependentBeanNames;

	// Used to store beans this bean depends on.
	private Map<String, BeanDefinition> dependentBeans;

	// The bean instance, singleton only.
	private Object bean;

	private static Map<Class<?>, Class<?>> primitiveTypeConverter = new HashMap<Class<?>, Class<?>>();

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


	private XmlBeanDefinition(List<String> dependentBeanNames, Map<String, BeanArg> ctorArgs,
			Map<String, BeanArg> propertyArgs, String id, Class<?> clazz) {
		this.clazz = clazz;
		this.id = id;
		this.ctorArgs = ctorArgs;
		this.propertyArgs = propertyArgs;
		this.dependentBeanNames = dependentBeanNames;
		this.dependentBeans = new HashMap<String, BeanDefinition>();
	}

	public String getId() {
		return id;
	}

	
	private Object buildArgmentValue(BeanArg arg, Class<?> argType) throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (arg.isReference()) {
			if (!this.dependentBeans.containsKey(arg.getValue())) {
				throw new InvalidBeanConfigException("th argument is configured as a bean-ref, but the refered bean can not be found.");
			}
			// The argument refers to another bean.
			BeanDefinition beanArgDefinition = this.dependentBeans.get(arg.getValue());
			return beanArgDefinition.getBean();
		}
		// The argument refers to a String.
		if (argType == String.class) {
			return arg.getValue();
		}
		if (!primitiveTypeConverter.containsKey(argType)) {
			// The argument is neither a bean, nor a string, nor a primitive type.
			throw new InvalidBeanConfigException("th constructor argument is not recognised:" + argType.getSimpleName());
		}
		// The argument refers to a primitive type.
		Class<?> argClass = primitiveTypeConverter.get(argType);
		Constructor<?> argConstructor = argClass.getConstructor(String.class);
		return argConstructor.newInstance(arg.getValue());
	}

	/*
	 * Get the nth constructor argument value.
	 */
	private Object getNthConstructorArg(int n, Class<?> nthArgType) throws Exception {
		BeanArg arg = this.ctorArgs.get(String.valueOf(n));
		return buildArgmentValue(arg, nthArgType);
	}
	
	
	private Object injectProperties() throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		for (Method method : this.clazz.getMethods()) {
			String methodName = method.getName();
			if (!methodName.startsWith("set")) {
				continue;
			}
			String property = methodName.replaceFirst("set", "");
			property = property.substring(0, 1).toLowerCase() + property.substring(1);
			if (!this.propertyArgs.containsKey(property)) {
				continue;
			}
			Class<?>[] paramTypes = method.getParameterTypes();
			if (paramTypes.length != 1) {
				continue;
			}
			Object argValue = buildArgmentValue(this.propertyArgs.get(property), paramTypes[0]);
			method.invoke(this.bean, argValue);
			this.propertyArgs.remove(property);
		}
		for (String property: this.propertyArgs.keySet()) {
			throw new InvalidBeanConfigException("No setter for property: " + property);
		}
		return this.bean;
	}


	private Object createBean() {
		try {
			Constructor<?>[] beanConstructors = this.clazz.getConstructors();
			if (beanConstructors.length == 0 && this.dependentBeans.isEmpty()) {
				this.bean = this.clazz.newInstance();
			}
			for (Constructor<?> beanConstructor : beanConstructors) {
				Class<?>[] constructorParamTypes = beanConstructor.getParameterTypes();
				if (constructorParamTypes.length != this.ctorArgs.size()) {
					continue;
				}
				try {
					Object[] params = new Object[constructorParamTypes.length];
					for (int i = 0; i < constructorParamTypes.length; i++) {
						params[i] = getNthConstructorArg(i, constructorParamTypes[i]);
					}
					this.bean = beanConstructor.newInstance(params);
					if (this.bean != null) {
						break;
					}
				} catch (Throwable e) {
					// Ignore it since constructors, which have arguments of the same number, may be overloaded.
				}
			}
		} catch (Throwable e) {
			logger.error("Got an expection while creating the bean of " + this.id, e);
			throw new UnresolvedBeanDependencyException(e.getMessage());
		}
		try {
			if (this.bean != null) {
				return injectProperties();
			}
		} catch (Throwable e) {
			logger.error("Got an exception while injecting properties:", e);
			throw new InvalidBeanConfigException("The bean can not be created because properties are not properly configured.");
		}
		throw new UnresolvedBeanDependencyException("Can not find appropriate constructor.");
	}
	
	
	private static String parseId(Element element) {
		String id = element.getAttribute("id");
		Assert.notEmptyText(id, "bean id can not be null");
		Assert.textMatchsRegex(id, "[a-zA-Z][0-9a-zA-Z]+");
		return id;
	}
	
	private static Class<?> parseClass(Element element) throws ClassNotFoundException {
		String className = element.getAttribute("class");
		Class<?> clazz = Class.forName(className);
		if (clazz.isInterface() || clazz.isAnnotation() ||
			clazz.isEnum() || Modifier.isAbstract(clazz.getModifiers())) {
			throw new InvalidBeanConfigException("Not an instantiable class.");
		}
		return clazz;
	}
	

	private static BeanArg parseBeanArg(Element element, List<String> depedentBeanNames) {
		String value = element.getAttribute("value");
		BeanArgType type = BeanArgType.VALUE;
		if (value == null || "".equals(value)) {
			value = element.getAttribute("ref");
			type = BeanArgType.REFERENCE;
		}
		Assert.notEmptyText(value, "Netheir value nor ref was found");
		if (type == BeanArgType.REFERENCE) {
			depedentBeanNames.add(value);
		}
		return new BeanArg(value, type);
	}
	
	
	/*
	 * Make sure that no index gaps exist. The following configuration
	 * is considered having an index gap:
	 * <bean id='test' class='Test'>
	 * 	<constructor-arg index='0' value='0'>
	 * 	<constructor-arg index='2' value='2'>
	 * </bean>
	 */
	private static void validateConstructorArgs(Map<String, BeanArg> args) {
		for (int i = 0; i < args.size(); i++) {
			if (!args.containsKey(String.valueOf(i))) {
				throw new InvalidBeanConfigException("Index " + i + " is missing.");
			}
		}
	}
	
	/**
	 * Parse arguments to construct the bean. These arguments are represented 
	 * by <constructor-arg> and <property> elements.
	 * @param beanElement the <bean> element.
	 * @param ctorArgs the map to hold constructor arguments. Indexed by the 'index' attribute of the <constructor-arg> element.
	 * @param propertyArgs the map to hold property arguments. Indexed by the 'name' attribute of the <property> element
	 * @return A String List that contains the names of all dependent beans.
	 */
	private static List<String> parseBeanArgs(Element beanElement, Map<String, BeanArg> ctorArgs, Map<String, BeanArg> propertyArgs) {
		List<String> dependentBeanNames = new LinkedList<String>();
		NodeList nodeList = beanElement.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			Element e = (Element) node;
			if (CONSTRUCTOR_ARG_ELEMENT.equals(e.getTagName())) {
				String index = e.getAttribute("index");
				Assert.notEmptyText(index, "Index of constructor argument can not be empty.");
				if (ctorArgs.containsKey(index)) {
					throw new InvalidBeanConfigException("Index '" + index + "' appeared more than once.");
				}
				BeanArg arg = parseBeanArg(e, dependentBeanNames);
				ctorArgs.put(index, arg);
			} else if (PROPERTY_ELEMENT.equals(e.getTagName())) {
				String index = e.getAttribute("name");
				Assert.notEmptyText(index, "Bean's property name can not be empty.");
				if (propertyArgs.containsKey(index)) {
					throw new InvalidBeanConfigException("Propery '" + index + "' appeared more than once.");
				}
				BeanArg arg = parseBeanArg(e, dependentBeanNames);
				propertyArgs.put(index, arg);
			}
		}
		validateConstructorArgs(ctorArgs);
		return dependentBeanNames;
	}
	
	private static void assertNoOverloadedSetters(Class<?> clazz) {
		Method[] methods = clazz.getMethods();
		Map<String, Boolean> setters = new HashMap<String, Boolean>();
		for (Method method: methods) {
			String name = method.getName();
			if (!name.startsWith("set")) {
				continue;
			}
			if (setters.containsKey(name)) {
				throw new InvalidBeanConfigException("Setter " + name + " is overloaded.");
			}
			setters.put(name, true);
		}
	}
	
	/**
	 * Parse a <bean> element and return the definition based on the element.
	 * @param element the <bean> element.
	 * @return a bean definition according to the element.
	 * @throws InvalidBeanConfigException if the <bean> element is not configured properly or
	 * the element is not a <bean> element.
	 */
	public static XmlBeanDefinition parseXmlBeanElement(Element element) {
		Assert.notNull(element, "Element can not be null.");
		try {
			if (!"bean".equals(element.getTagName())) {
				throw new InvalidBeanConfigException("Not a bean element.");
			}
			String id = parseId(element);
			Class<?> clazz = parseClass(element);
			assertNoOverloadedSetters(clazz);
			Map<String, BeanArg> ctorArgs = new HashMap<String, BeanArg>();
			Map<String, BeanArg> propertyArgs = new HashMap<String, BeanArg>();
			List<String> dependentBeanNames = parseBeanArgs(element, ctorArgs, propertyArgs);
			return new XmlBeanDefinition(dependentBeanNames, ctorArgs, propertyArgs, id, clazz);
		} catch (Throwable e) {
			throw new InvalidBeanConfigException(e);
		}
	}

	/**
	 * Instantiate the bean.
	 * @throws UnresolvedBeanDependencyException if bean has unresolved dependencies.
	 */
	public Object getBean() {
		if (this.hasUnresolvedDependency()) {
			throw new UnresolvedBeanDependencyException(
					"Bean " + id + " can not be created because unresolved dependencies.");
		}
		if (this.bean != null) {
			return this.bean;
		}
		return createBean();
	}

	/**
	 * @return if this bean definition has unresolved dependencies.
	 */
	public boolean hasUnresolvedDependency() {
		return !this.dependentBeanNames.isEmpty();
	}


	/**
	 * To be called whenever a bean else is created, so that this bean definition can resolve
	 * it's dependencies.
	 * @param createdBean the bean created.
	 */
	public void onOtherBeanCreated(BeanDefinition createdBean) {
		while (this.dependentBeanNames.remove(createdBean.getId()));
		this.dependentBeans.put(createdBean.getId(), createdBean);
	}

}
