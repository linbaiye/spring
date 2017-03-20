package org.nalby.spring.util;

import org.junit.Test;

public class AssertTest {
	
	@Test(expected = NullPointerException.class)
	public void testAssertNotNull() {
		Assert.notNull(null, "Not null");
	}
	
	@Test
	public void testAssertNotNullOk() {
		Assert.notNull("ok", "Not null");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void testAssertNotEmptyWithNull() {
		Assert.notEmptyText(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testAssertNotEmptyWithEmptyString() {
		Assert.notEmptyText("");
	}

	@Test
	public void testAssertNotEmptyWithString() {
		Assert.notEmptyText("String");
	}

}
