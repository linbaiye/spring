package org.nalby.spring.bean;

import static org.junit.Assert.fail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.Test;

public class AbstractBeanDefinitionTest {
	
	private static class PrivateClass {
		
	}
	
	public static enum EnumClass {
		
	}
	
	public static interface InterfaceClass {
		
	}
	
	public static abstract class AbstractClass {
		
	}
	
	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface AnnotationClass {
	}
	
	@Test
	public void testClasses() {
		Class<?>[] classes = new Class<?>[] {
			PrivateClass.class,
			EnumClass.class,
			InterfaceClass.class,
			AbstractClass.class,
			AnnotationClass.class,
			int.class,
			String[].class,
		};
		for (Class<?> clazz: classes) {
			try {
				AbstractBeanDefinition.assertClassAcceptable(clazz);
				fail("Should throw an exception.");
			} catch (InvalidBeanConfigException e) {
			}
		}
	}
}
