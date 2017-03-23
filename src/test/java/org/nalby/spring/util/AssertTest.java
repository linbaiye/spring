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
	
	private void assertMatch(String text, String regex) {
		try {
			Assert.textMatchsRegex(text, regex);
		} catch (Exception e) {
			throw new IllegalStateException();
		}
	}
	private void assertNotMatch(String text, String regex) {
		try {
			Assert.textMatchsRegex(text, regex);
		} catch (IllegalArgumentException e) {
			return;
		}
		throw new IllegalStateException();
	}
	
	@Test
	public void testRegex() {
		assertNotMatch("hello", "[0-9][0-9]+");
		assertNotMatch("1s", "[a-z][a-z0-9]+");
		assertNotMatch("s-", "[a-z][a-z0-9]+");
		assertMatch("ts10", "[a-z][a-z0-9]+");
		assertMatch("testCode1", "[a-z][a-zA-Z0-9]+");
	}
	

}
