package org.nalby.spring.bean;

public interface BeanDefinition {
	
	/**
	 * Get the bean id.
	 * @return the bean id.
	 */
	public String getId();
	
	/**
	 * Get the bean it-self.
	 * @return the bean instance.
	 */
	public Object getBean();
	
	/**
	 * Test if this bean has unresolved dependencies. Having unresolved dependencies means that
	 * it can not be instantiated yet.
	 * @return if the bean still has dependencies.
	 */
	public boolean hasUnresolvedDependency();
	
	/**
	 * The method to be invoked whenever a bean else is created, so this bean can resolve the
	 * the dependency of the craetedBean.
	 * @param createdBean the bean created.
	 */
	public void onOtherBeanCreated(BeanDefinition createdBean);
}
