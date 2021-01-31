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
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges("TwoRanges", Arrays.asList(forange1, forange2, forange3, forange4));
		Assert.assertEquals(-10, foseq.iterator().next().getInteger());
		Assert.assertEquals(40, foseq.size());
		Assert.assertEquals(40, Iterables.size(foseq));
	}
	
}
