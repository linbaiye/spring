package org.nalby.spring.bean;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBeanDefinition implements BeanDefinition {

	// bean id
	String id = null;

	Class<?> clazz = null;
	Map<String, BeanArg> propertyArgs;
	
	Map<String, BeanArg> ctorArgs;
	// Beans this bean depends on.
	List<String> dependentBeanNames;

	// Used to store beans this bean depends on.
	Map<String, BeanDefinition> dependentBeans;

	// The bean instance, singleton only.
	Object bean;

	static Map<Class<?>, Class<?>> primitiveTypeConverter = new HashMap<Class<?>, Class<?>>();

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

	AbstractBeanDefinition(List<String> dependentBeanNames, Map<String, BeanArg> ctorArgs,
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
	
	static void assertClassAcceptable(Class<?> clazz) {
		if (clazz.isArray() || clazz.isInterface() || clazz.isAnnotation()
			|| clazz.isEnum() || clazz.isPrimitive() || Modifier.isAbstract(clazz.getModifiers())
			|| Modifier.isPrivate(clazz.getModifiers())) {
			throw new InvalidBeanConfigException("Bean class is not acceptable.");
		}
	}

	abstract Object injectProperties();

	abstract Object createBean();

	/**
	 * Instantiate the bean.
	 * @throws UnresolvedBeanDependencyException if the bean has unresolved dependencies.
	 * @throws InvalidBeanConfigException if the bean is not properly configured.
	 */
	public Object getBean() {
		if (this.hasUnresolvedDependency()) {
			throw new UnresolvedBeanDependencyException(
					"Bean " + id + " can not be created because unresolved dependencies.");
		}
		if (this.bean != null) {
			return this.bean;
		}
		createBean();
		return injectProperties();
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
