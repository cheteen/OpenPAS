package fopas;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fopas.basics.FOEnumerableSet;
import fopas.basics.FORange;
import fopas.basics.FOElement.FOInteger;

public class FOSetUtilsTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testEmptySet()
	{
		FOEnumerableSet<FOInteger> emptySet = new FOSetUtils.EmptySet<>();
		assertEquals("(Empty)", emptySet.getName());
	}
}
