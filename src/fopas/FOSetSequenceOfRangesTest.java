package fopas;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Iterables;

import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOEnumerableSet;
import fopas.basics.FOSet;

public class FOSetSequenceOfRangesTest {

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
	public void testSingleRange()
	{
		// This is the most pointless use of this class, but we consider it legal.
		FOSetRangedNaturals forange = new FOSetRangedNaturals(10, 99);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges("SingleRange", Arrays.asList(forange));
		Assert.assertEquals("SingleRange", foseq.getName());
		Assert.assertEquals(10, foseq.iterator().next().getInteger());
		Assert.assertEquals(90, foseq.size());
		Assert.assertEquals(90, Iterables.size(foseq));
	}
	
	@Test
	public void testTwoSeparateRanges()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(10, 19);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(30, 39);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges("TwoRanges", Arrays.asList(forange1, forange2));
		Assert.assertEquals(10, foseq.iterator().next().getInteger());
		Assert.assertEquals(20, foseq.size());
		Assert.assertEquals(20, Iterables.size(foseq));
	}
	
	@Test
	public void testFourSeparateRanges()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(-19, -10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(1, 10);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 29);
		FOSetRangedNaturals forange4 = new FOSetRangedNaturals(30, 39);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges("FourRanges", Arrays.asList(forange1, forange2, forange3, forange4));
		Assert.assertEquals(-10, foseq.iterator().next().getInteger());
		Assert.assertEquals(40, foseq.size());
		Assert.assertEquals(40, Iterables.size(foseq));
	}
	
	@Test
	public void testComplements()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(-19, -10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(1, 10);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 29);
		FOSetRangedNaturals forange4 = new FOSetRangedNaturals(30, 39);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges("FourRanges", Arrays.asList(forange1, forange2, forange3, forange4));
		
		{
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals(-20, fosetc.iterator().next().getInteger());
			Assert.assertEquals(-1, fosetc.size());
			// This complement should be: (-inf, -20] U [-9, 0] U [11, 19] U [40, inf)
			// So let's do pinpoint shots to see whether correct parts are included:
			// TODO: Should contain return true for inf? Mathematically no, so check that/do that.
			assertTrue(fosetc.contains(new FOElementImpl.FOIntImpl(-50)));
			assertTrue(fosetc.contains(new FOElementImpl.FOIntImpl(-20)));
			assertFalse(fosetc.contains(new FOElementImpl.FOIntImpl(-19)));
			assertFalse(fosetc.contains(new FOElementImpl.FOIntImpl(-10)));
			assertTrue(fosetc.contains(new FOElementImpl.FOIntImpl(-9)));
			assertTrue(fosetc.contains(new FOElementImpl.FOIntImpl(0)));
			assertFalse(fosetc.contains(new FOElementImpl.FOIntImpl(1)));
			assertFalse(fosetc.contains(new FOElementImpl.FOIntImpl(2)));
			assertFalse(fosetc.contains(new FOElementImpl.FOIntImpl(10)));
			assertTrue(fosetc.contains(new FOElementImpl.FOIntImpl(11)));
			assertTrue(fosetc.contains(new FOElementImpl.FOIntImpl(15)));
			assertTrue(fosetc.contains(new FOElementImpl.FOIntImpl(19)));
			assertFalse(fosetc.contains(new FOElementImpl.FOIntImpl(29)));
			assertFalse(fosetc.contains(new FOElementImpl.FOIntImpl(30)));
			assertFalse(fosetc.contains(new FOElementImpl.FOIntImpl(39)));
			assertTrue(fosetc.contains(new FOElementImpl.FOIntImpl(40)));
			assertTrue(fosetc.contains(new FOElementImpl.FOIntImpl(100)));
		}
	}
}
