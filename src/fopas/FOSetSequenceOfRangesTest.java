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

import fopas.FOSetUtils.EmptySet;
import fopas.basics.FOEnumerableSet;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOOrderedEnumerableSet;
import fopas.basics.FORuntimeException;
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
	public void testSingleRangeThrows()
	{
		FOSetRangedNaturals forange = new FOSetRangedNaturals(10, 99);
		FOSetSequenceOfRanges foseq = null;
		try
		{
			foseq = new FOSetSequenceOfRanges(Arrays.asList(forange));
		}
		catch(FORuntimeException exp)
		{
			assertTrue(exp.getMessage().contains("need at least 2 ranges"));
		}
		assertEquals(null, foseq);
	}
	
	@Test
	public void testContiguousRangeThrows()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(10, 19);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(20, 39);
		FOSetSequenceOfRanges foseq = null;
		try
		{
			foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2), false);
		}
		catch(FORuntimeException exp)
		{
			assertTrue(exp.getMessage().contains("Contiguous sequence of ranges creation where not allowed"));
		}
		assertEquals(null, foseq);
	}

	@Test
	public void testOverlappingRangeThrows()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(10, 20);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(20, 39);
		FOSetSequenceOfRanges foseq = null;
		try
		{
			foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2));
		}
		catch(FORuntimeException exp)
		{
			assertTrue(exp.getMessage().contains("Incorrectly ordered or overlapping invalid range given during creation"));
		}
		assertEquals(null, foseq);
	}

	@Test
	public void testUnorderedRangeThrows()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(10, 20);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(-10, 0);
		FOSetSequenceOfRanges foseq = null;
		try
		{
			foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2));
		}
		catch(FORuntimeException exp)
		{
			assertTrue(exp.getMessage().contains("Incorrectly ordered or overlapping invalid range given during creation"));
		}
		assertEquals(null, foseq);
	}

	@Test
	public void testNamedSeqRange()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(10, 19);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(30, 39);
		FOSetRangedNaturals forangec = new FOSetRangedNaturals(0, 45);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges("MySeqRange", Arrays.asList(forange1, forange2));
		Assert.assertEquals("MySeqRange", foseq.getName());
		Assert.assertEquals("N [0, 45] \\ MySeqRange", foseq.complement(forangec).getName());
	}

	@Test
	public void testTwoSeparateRanges()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(10, 19);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(30, 39);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2));
		assertEquals("N [10, 19] U [30, 39]", foseq.getName());
		assertEquals(10, foseq.iterator().next().getInteger());
		assertEquals(20, foseq.size());
		assertEquals(20, Iterables.size(foseq));
		assertEquals(10, foseq.getFirstOrInfinite().getInteger());
		assertEquals(39, foseq.getLastOrInfinite().getInteger());
	}
	
	@Test
	public void testFourSeparateRanges()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(-19, -10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(1, 10);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 29);
		FOSetRangedNaturals forange4 = new FOSetRangedNaturals(30, 39);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2, forange3, forange4));
		assertEquals("Z [-19, -10] U [1, 10] U [20, 29] U [30, 39]", foseq.getName());
		assertEquals(-10, foseq.iterator().next().getInteger());
		assertEquals(40, foseq.size());
		assertEquals(40, Iterables.size(foseq));
		assertEquals(-19, foseq.getFirstOrInfinite().getInteger());
		assertEquals(39, foseq.getLastOrInfinite().getInteger());
	}
	
	@Test
	public void testComplements1()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(-19, -10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(1, 10);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 29);
		FOSetRangedNaturals forange4 = new FOSetRangedNaturals(30, 39);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2, forange3, forange4));
		Assert.assertEquals("Z [-19, -10] U [1, 10] U [20, 29] U [30, 39]", foseq.getName());
		
		{
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, Integer.MAX_VALUE, false);
			FOEnumerableSet<FOInteger> fosetc = (FOEnumerableSet<FOInteger>) foseq.complement(forangec);
			Assert.assertEquals(-20, fosetc.iterator().next().getInteger());
			Assert.assertEquals(-1, fosetc.size());
			Assert.assertEquals("Z (-inf, -20] U [-9, 0] U [11, 19] U [40, inf)", fosetc.getName());
			// So let's also do pinpoint shots to see whether correct parts are included:
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
			assertEquals(Integer.MIN_VALUE, ((FOSetSequenceOfRanges) fosetc).getFirstOrInfinite().getInteger());
			assertEquals(Integer.MAX_VALUE, ((FOSetSequenceOfRanges) fosetc).getLastOrInfinite().getInteger());

			assertTrue(foseq == foseq.complement(forangec, false));
		}

		//------------------------
		// -inf -> x
		//------------------------
		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, -20, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, -20]", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, -19, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, -20]", fosetc.getName());
		}
		
		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, -15, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, -20]", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, -10, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, -20]", fosetc.getName());
		}				

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, -9, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, -20] U [-9, -9]", fosetc.getName());
		}
		
		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 15, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, -20] U [-9, 0] U [11, 15]", fosetc.getName());
		}
		
		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 19, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, -20] U [-9, 0] U [11, 19]", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 20, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, -20] U [-9, 0] U [11, 19]", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 29, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, -20] U [-9, 0] U [11, 19]", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 30, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, -20] U [-9, 0] U [11, 19]", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 39, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, -20] U [-9, 0] U [11, 19]", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 45, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, -20] U [-9, 0] U [11, 19] U [40, 45]", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, -20] U [-9, 0] U [11, 19] U [40, inf)", fosetc.getName());
		}

		//------------------------
		// x -> inf
		//------------------------

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(-35, false, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z [-34, -20] U [-9, 0] U [11, 19] U [40, inf)", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(-20, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z [-20, -20] U [-9, 0] U [11, 19] U [40, inf)", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(-19, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z [-9, 0] U [11, 19] U [40, inf)", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(-15, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z [-9, 0] U [11, 19] U [40, inf)", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(-10, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z [-9, 0] U [11, 19] U [40, inf)", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(-9, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z [-9, 0] U [11, 19] U [40, inf)", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(-8, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z [-8, 0] U [11, 19] U [40, inf)", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(19, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [19, 19] U [40, inf)", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(20, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [40, inf)", fosetc.getName());
		}
		
		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(21, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [40, inf)", fosetc.getName());
		}
		
		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(29, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [40, inf)", fosetc.getName());
		}
		
		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(30, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [40, inf)", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(31, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [40, inf)", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(39, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [40, inf)", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(40, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [40, inf)", fosetc.getName());
		}

		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(41, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [41, inf)", fosetc.getName());
		}

		//------------------------
		// Other variations
		//------------------------

		
		{//Tricky one as there's a contigous range [30, 39] following the complement end at 30.
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(-15, true, 30, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z [-9, 0] U [11, 19]", fosetc.getName());
		}
		
		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(-30, true, 15, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z [-30, -20] U [-9, 0] U [11, 15]", fosetc.getName());
		}
		
		{
			// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(-30, true, 50, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z [-30, -20] U [-9, 0] U [11, 19] U [40, 50]", fosetc.getName());
		}
	}
	
	
	@Test
	public void testComplements2()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 10, true);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(20, 30);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2));
		Assert.assertEquals("Z (-inf, 10] U [20, 30]", foseq.getName());

		{
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [11, 19] U [31, inf)", fosetc.getName());
		}
		
		{
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 15, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [11, 15]", fosetc.getName());
		}
		
		{
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(25, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [31, inf)", fosetc.getName());
		}
		
		{
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(15, true, 19, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [15, 19]", fosetc.getName());
		}
	}
	
	@Test
	public void testComplements3()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(20, 30);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(40, true, Integer.MAX_VALUE, false);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2));
		Assert.assertEquals("N [20, 30] U [40, inf)", foseq.getName());

		{
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, 19] U [31, 39]", fosetc.getName());
		}
		
		{
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 25, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("Z (-inf, 19]", fosetc.getName());
		}

		{
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(25, true, Integer.MAX_VALUE, false);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [31, 39]", fosetc.getName());
		}

		{
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(30, true, 40, true);
			FOSet<FOInteger> fosetc = foseq.complement(forangec);
			Assert.assertEquals("N [31, 39]", fosetc.getName());
		}		
	}
	
	@Test
	public void testComplementSelf()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(20, 30);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(40, true, Integer.MAX_VALUE, false);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2));
		
		FOSet<FOInteger> fosetc = foseq.complement(foseq);
		assertTrue(fosetc instanceof EmptySet);
	}
	
	@Test
	public void testComplementSelfEffectively1()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(20, 30);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(31, true, Integer.MAX_VALUE, false);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2));
		
		// This set below is equivalent to the set foseq.
		FOSetRangedNaturals forangec = new FOSetRangedNaturals(20, true, Integer.MAX_VALUE, false);

		FOSet<FOInteger> fosetc = foseq.complement(forangec);
		assertTrue(fosetc instanceof EmptySet);
	}
	
	@Test
	public void testComplementSelfEffectively2()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 20, true);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(21, 30);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2));
		
		FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 30, true);

		FOSet<FOInteger> fosetc = foseq.complement(forangec);
		assertTrue(fosetc instanceof EmptySet);
	}
	
	@Test
	public void testComplementSelfEffectively3()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(10, 20);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(21, 30);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(31, 50);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2, forange3));
		
		FOSetRangedNaturals forangec = new FOSetRangedNaturals(10, 50);

		FOSet<FOInteger> fosetc = foseq.complement(forangec);
		assertTrue(fosetc instanceof EmptySet);
	}
	
	@Test
	public void testConstrains()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(-19, -10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(1, 10);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 29);
		FOSetRangedNaturals forange4 = new FOSetRangedNaturals(30, 39);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2, forange3, forange4));
		Assert.assertEquals("Z [-19, -10] U [1, 10] U [20, 29] U [30, 39]", foseq.getName());
		
		assertEquals("Z [-19, -10] U [1, 10] U [20, 29] U [30, 39]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("Z [-19, -10] U [1, 10] U [20, 29]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(29)).getName());
		assertEquals("Z [-19, -10] U [1, 10] U [20, 20]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(20)).getName());
		assertEquals("N [1, 10] U [20, 29] U [30, 39]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(-4), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("N [5, 10] U [20, 29] U [30, 39]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(5), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("N [10, 10] U [20, 29] U [30, 39]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(10), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("Z [-15, -10] U [1, 10] U [20, 29] U [30, 35]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(-15), new FOElementImpl.FOIntImpl(35)).getName());
		assertEquals("Z [-19, -10] U [1, 10] U [20, 29]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(-19), new FOElementImpl.FOIntImpl(29)).getName());
		assertEquals("N [1, 10]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(1), new FOElementImpl.FOIntImpl(10)).getName());
		assertEquals("N [1, 10]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(0), new FOElementImpl.FOIntImpl(11)).getName());
		assertEquals("N [5, 6]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(5), new FOElementImpl.FOIntImpl(6)).getName());
		assertEquals("N [5, 5]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(5), new FOElementImpl.FOIntImpl(5)).getName());
	}

	@Test
	public void testConstrainNamed()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(-19, -10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(1, 10);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 29);
		FOSetRangedNaturals forange4 = new FOSetRangedNaturals(30, 39);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges("MySet", Arrays.asList(forange1, forange2, forange3, forange4));
		Assert.assertEquals("MySet", foseq.getName());
		
		{
			FOOrderedEnumerableSet<FOInteger> conted = foseq.constrainToRange(new FOElementImpl.FOIntImpl(5), new FOElementImpl.FOIntImpl(6)); 
			assertEquals("N [5, 6]", conted.getName()); // Single range doesn't retain name -- too wasteful.
		}
		{
			FOOrderedEnumerableSet<FOInteger> conted = foseq.constrainToRange(new FOElementImpl.FOIntImpl(5), new FOElementImpl.FOIntImpl(25)); 
			assertEquals("MySet [5, 25]", conted.getName());			
			assertFalse(conted.contains(new FOElementImpl.FOIntImpl(4)));
			assertTrue(conted.contains(new FOElementImpl.FOIntImpl(5)));
			assertTrue(conted.contains(new FOElementImpl.FOIntImpl(6)));
			assertFalse(conted.contains(new FOElementImpl.FOIntImpl(11)));			
			assertTrue(conted.contains(new FOElementImpl.FOIntImpl(20)));
			assertTrue(conted.contains(new FOElementImpl.FOIntImpl(25)));
			assertFalse(conted.contains(new FOElementImpl.FOIntImpl(26)));
		}
	}

	@Test
	public void testConstrainSelf()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(-19, -10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(1, 10);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 29);
		FOSetRangedNaturals forange4 = new FOSetRangedNaturals(30, 39);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges("MySet", Arrays.asList(forange1, forange2, forange3, forange4));
		
		{
			FOOrderedEnumerableSet<FOInteger> conted = foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)); 
			Assert.assertTrue(conted == foseq);
		}
		{
			FOOrderedEnumerableSet<FOInteger> conted = foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(39)); 
			Assert.assertTrue(conted == foseq);
		}
		{
			FOOrderedEnumerableSet<FOInteger> conted = foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(50)); 
			Assert.assertTrue(conted == foseq);
		}
		{
			FOOrderedEnumerableSet<FOInteger> conted = foseq.constrainToRange(new FOElementImpl.FOIntImpl(-19), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)); 
			Assert.assertTrue(conted == foseq);
		}
	}
}
