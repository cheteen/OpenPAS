//    Copyright (c) 2021 Burak Cetin
//
//    This file is part of OpenPAS.
//
//    OpenPAS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    OpenPAS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with OpenPAS.  If not, see <https://www.gnu.org/licenses/>.

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

import fopas.FOSetSequenceOfRanges.FOInvalidSingleRangeSequence;
import fopas.FOSetUtils.EmptySet;
import fopas.basics.FOEnumerableSet;
import fopas.basics.FOElement;
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
		catch (FOInvalidSingleRangeSequence e)
		{
		}
		assertEquals(null, foseq);
	}
	

	@Test
	public void testContigousSingleRangeThrows()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(20, 30);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(31, true, Integer.MAX_VALUE, false);
		
		FOSetSequenceOfRanges foseq = null;
		try
		{
			foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2));
		}
		catch (FOInvalidSingleRangeSequence e)
		{
		}		
		assertEquals(null, foseq);
	}

	@Test
	public void testContiguousRangeThrows() throws FOInvalidSingleRangeSequence
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
	public void testOverlappingRangeThrows() throws FOInvalidSingleRangeSequence
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
	public void testUnorderedRangeThrows() throws FOInvalidSingleRangeSequence
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
	public void testNamedSeqRange() throws FOInvalidSingleRangeSequence
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(10, 19);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(30, 39);
		FOSetRangedNaturals forangec = new FOSetRangedNaturals(0, 45);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges("MySeqRange", Arrays.asList(forange1, forange2));
		Assert.assertEquals("MySeqRange", foseq.getName());
		Assert.assertEquals("N [0, 45] \\ MySeqRange", foseq.complement(forangec).getName());
	}

	@Test
	public void testTwoSeparateRanges() throws FOInvalidSingleRangeSequence
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
	public void testFourSeparateRanges() throws FOInvalidSingleRangeSequence
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(-19, -10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(1, 10);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 29);
		FOSetRangedNaturals forange4 = new FOSetRangedNaturals(30, 39);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2, forange3, forange4));
		assertEquals("Z [-19, -10] U [1, 10] U [20, 39]", foseq.getName()); // should merge [20, 29] U [30, 39] --> [20, 39]  
		assertEquals(-10, foseq.iterator().next().getInteger());
		assertEquals(40, foseq.size());
		assertEquals(40, Iterables.size(foseq));
		assertEquals(-19, foseq.getFirstOrInfinite().getInteger());
		assertEquals(39, foseq.getLastOrInfinite().getInteger());
	}
	
	@Test
	public void testComplements1() throws FOInvalidSingleRangeSequence
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(-19, -10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(1, 10);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 29);
		FOSetRangedNaturals forange4 = new FOSetRangedNaturals(30, 39);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2, forange3, forange4));
		Assert.assertEquals("Z [-19, -10] U [1, 10] U [20, 39]", foseq.getName());
		
		{
			FOSetRangedNaturals forangec = new FOSetRangedNaturals(Integer.MIN_VALUE, false, Integer.MAX_VALUE, false);
			FOEnumerableSet<FOInteger> fosetc = (FOEnumerableSet<FOInteger>) foseq.complement(forangec);
			Assert.assertEquals(-20, fosetc.iterator().next().getInteger());
			Assert.assertEquals(Integer.MAX_VALUE, fosetc.size());
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

			assertTrue(foseq.equals(fosetc.complement(forangec)));
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
	public void testComplements2() throws FOInvalidSingleRangeSequence
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
	public void testComplements3() throws FOInvalidSingleRangeSequence
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
	public void testComplementSelf() throws FOInvalidSingleRangeSequence
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(20, 30);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(40, true, Integer.MAX_VALUE, false);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2));
		
		FOSet<FOInteger> fosetc = foseq.complement(foseq);
		assertTrue(fosetc instanceof EmptySet);
	}
	
	@Test
	public void testComplementSelfEffectively1() throws FOInvalidSingleRangeSequence
	{
		// TODO: Self complement probably needs own impl.
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(20, 29);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(31, true, Integer.MAX_VALUE, false);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2));
		
		FOSet<FOInteger> fosetc = foseq.complement(foseq);
		assertTrue(fosetc instanceof EmptySet);
	}
	
	@Test
	public void testComplementSelfEffectively2() throws FOInvalidSingleRangeSequence
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 20, true);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(22, 30);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2));
		
		FOSet<FOInteger> fosetc = foseq.complement(foseq);
		assertTrue(fosetc instanceof EmptySet);
	}
	
	@Test
	public void testComplementSelfEffectively3() throws FOInvalidSingleRangeSequence
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(10, 20);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(22, 30);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(32, 50);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2, forange3));
		
		FOSet<FOInteger> fosetc = foseq.complement(foseq);
		assertTrue(fosetc instanceof EmptySet);
	}
	
	@Test
	public void testConstrains1() throws FOInvalidSingleRangeSequence
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(-19, -10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(1, 10);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 29);
		FOSetRangedNaturals forange4 = new FOSetRangedNaturals(30, 39);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2, forange3, forange4));
		Assert.assertEquals("Z [-19, -10] U [1, 10] U [20, 39]", foseq.getName());
		
		assertEquals("Z [-19, -10] U [1, 10] U [20, 39]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("Z [-19, -10] U [1, 10] U [20, 29]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(29)).getName());
		assertEquals("Z [-19, -10] U [1, 10] U [20, 20]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(20)).getName());
		assertEquals("N [1, 10] U [20, 39]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(-4), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("N [5, 10] U [20, 39]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(5), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("N [10, 10] U [20, 39]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(10), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("Z [-15, -10] U [1, 10] U [20, 35]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(-15), new FOElementImpl.FOIntImpl(35)).getName());
		assertEquals("Z [-19, -10] U [1, 10] U [20, 29]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(-19), new FOElementImpl.FOIntImpl(29)).getName());
		assertEquals("N [1, 10]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(1), new FOElementImpl.FOIntImpl(10)).getName());
		assertEquals("N [1, 10]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(0), new FOElementImpl.FOIntImpl(11)).getName());
		assertEquals("N [5, 6]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(5), new FOElementImpl.FOIntImpl(6)).getName());
		assertEquals("N [5, 5]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(5), new FOElementImpl.FOIntImpl(5)).getName());
	}

	@Test
	public void testConstrains2() throws FOInvalidSingleRangeSequence
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 0, true);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(3, 5);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(10, true, Integer.MAX_VALUE, false);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges(Arrays.asList(forange1, forange2, forange3));
		Assert.assertEquals("Z (-inf, 0] U [3, 5] U [10, inf)", foseq.getName());
		
		assertEquals("Z (-inf, -10]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(-10)).getName());
		assertEquals("Z (-inf, 0]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(0)).getName());
		assertEquals("Z (-inf, 0]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(1)).getName());
		assertEquals("Z (-inf, 0]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(2)).getName());
		assertEquals("Z (-inf, 0] U [3, 3]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(3)).getName());
		assertEquals("Z (-inf, 0] U [3, 4]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(4)).getName());
		assertEquals("Z (-inf, 0] U [3, 5]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(5)).getName());
		assertEquals("Z (-inf, 0] U [3, 5]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(6)).getName());
		assertEquals("Z (-inf, 0] U [3, 5] U [10, 10]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(10)).getName());
		assertEquals("Z (-inf, 0] U [3, 5] U [10, 15]", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(15)).getName());
		assertEquals("Z (-inf, 0] U [3, 5] U [10, inf)", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());

		assertEquals("N [21, inf)", foseq.constrainToRange(new FOElementImpl.FOIntImpl(21), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("N [10, inf)", foseq.constrainToRange(new FOElementImpl.FOIntImpl(10), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("N [10, inf)", foseq.constrainToRange(new FOElementImpl.FOIntImpl(9), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("N [10, inf)", foseq.constrainToRange(new FOElementImpl.FOIntImpl(7), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("N [5, 5] U [10, inf)", foseq.constrainToRange(new FOElementImpl.FOIntImpl(5), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("N [4, 5] U [10, inf)", foseq.constrainToRange(new FOElementImpl.FOIntImpl(4), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("N [3, 5] U [10, inf)", foseq.constrainToRange(new FOElementImpl.FOIntImpl(3), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("N [3, 5] U [10, inf)", foseq.constrainToRange(new FOElementImpl.FOIntImpl(2), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("N [3, 5] U [10, inf)", foseq.constrainToRange(new FOElementImpl.FOIntImpl(1), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("N [0, 0] U [3, 5] U [10, inf)", foseq.constrainToRange(new FOElementImpl.FOIntImpl(0), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("Z [-10, 0] U [3, 5] U [10, inf)", foseq.constrainToRange(new FOElementImpl.FOIntImpl(-10), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
		assertEquals("Z (-inf, 0] U [3, 5] U [10, inf)", foseq.constrainToRange(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)).getName());
	}
	
	@Test
	public void testConstrainNamed() throws FOInvalidSingleRangeSequence
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
	public void testConstrainSelf() throws FOInvalidSingleRangeSequence
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
	
	@Test
	public void testNextAndPrev1() throws FOInvalidSingleRangeSequence
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(-19, -10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(1, 10);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 29);
		FOSetRangedNaturals forange4 = new FOSetRangedNaturals(30, 39);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges("MySet", Arrays.asList(forange1, forange2, forange3, forange4));
		// Main set: [-19, -10] U [1, 10] U [20, 29] U [30, 39]

		assertEquals(new FOElementImpl.FOIntImpl(-19), foseq.getNextOrNull(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE)));
		assertEquals(new FOElementImpl.FOIntImpl(-19), foseq.getNextOrNull(new FOElementImpl.FOIntImpl(-30)));
		assertEquals(new FOElementImpl.FOIntImpl(6), foseq.getNextOrNull(new FOElementImpl.FOIntImpl(5)));
		assertEquals(new FOElementImpl.FOIntImpl(20), foseq.getNextOrNull(new FOElementImpl.FOIntImpl(15)));
		assertEquals(new FOElementImpl.FOIntImpl(20), foseq.getNextOrNull(new FOElementImpl.FOIntImpl(10)));
		assertEquals(new FOElementImpl.FOIntImpl(30), foseq.getNextOrNull(new FOElementImpl.FOIntImpl(29)));
		assertEquals(null, foseq.getNextOrNull(new FOElementImpl.FOIntImpl(39)));
		assertEquals(null, foseq.getNextOrNull(new FOElementImpl.FOIntImpl(100)));
		assertEquals(null, foseq.getNextOrNull(new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)));
		
		assertEquals(null, foseq.getPreviousOrNull(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE)));
		assertEquals(null, foseq.getPreviousOrNull(new FOElementImpl.FOIntImpl(-30)));
		assertEquals(new FOElementImpl.FOIntImpl(4), foseq.getPreviousOrNull(new FOElementImpl.FOIntImpl(5)));
		assertEquals(new FOElementImpl.FOIntImpl(10), foseq.getPreviousOrNull(new FOElementImpl.FOIntImpl(15)));
		assertEquals(new FOElementImpl.FOIntImpl(10), foseq.getPreviousOrNull(new FOElementImpl.FOIntImpl(20)));
		assertEquals(new FOElementImpl.FOIntImpl(29), foseq.getPreviousOrNull(new FOElementImpl.FOIntImpl(30)));
		assertEquals(new FOElementImpl.FOIntImpl(39), foseq.getPreviousOrNull(new FOElementImpl.FOIntImpl(40)));
		assertEquals(new FOElementImpl.FOIntImpl(39), foseq.getPreviousOrNull(new FOElementImpl.FOIntImpl(100)));
		assertEquals(new FOElementImpl.FOIntImpl(39), foseq.getPreviousOrNull(new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)));
	}	

	@Test
	public void testNextAndPrev2() throws FOInvalidSingleRangeSequence
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(Integer.MIN_VALUE, false, -10, true);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(1, 10);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 29);
		FOSetRangedNaturals forange4 = new FOSetRangedNaturals(30, true, Integer.MAX_VALUE, false);
		FOSetSequenceOfRanges foseq = new FOSetSequenceOfRanges("MySet", Arrays.asList(forange1, forange2, forange3, forange4));
		// Main set: [-inf, -10] U [1, 10] U [20, 29] U [30, inf]

		assertEquals(new FOElementImpl.FOIntImpl(6), foseq.getNextOrNull(new FOElementImpl.FOIntImpl(5)));
		assertEquals(new FOElementImpl.FOIntImpl(35), foseq.getNextOrNull(new FOElementImpl.FOIntImpl(34)));
		assertEquals(new FOElementImpl.FOIntImpl(Integer.MAX_VALUE), foseq.getNextOrNull(new FOElementImpl.FOIntImpl(Integer.MAX_VALUE)));
		
		assertEquals(new FOElementImpl.FOIntImpl(4), foseq.getPreviousOrNull(new FOElementImpl.FOIntImpl(5)));
		assertEquals(new FOElementImpl.FOIntImpl(-15), foseq.getPreviousOrNull(new FOElementImpl.FOIntImpl(-14)));
		assertEquals(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE), foseq.getPreviousOrNull(new FOElementImpl.FOIntImpl(Integer.MIN_VALUE)));
	}
	
	@Test
	public void testUnion1()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(-20, -10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(-15, -10);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(-15, 0);
		FOSetRangedNaturals forange4 = new FOSetRangedNaturals(5, 10);
		// Union call order below is different to the naming order!
		FOOrderedEnumerableSet<FOInteger> foseq = FOSetSequenceOfRanges.createUnion(Arrays.asList(forange1, forange4, forange2, forange3));
		assertEquals("Z [-20, 0] U [5, 10]", foseq.getName());
	}

	@Test
	public void testUnion2()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(0, 10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(10, 15);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 30);
		// Union call order below is different to the naming order!
		FOOrderedEnumerableSet<FOInteger> foseq = FOSetSequenceOfRanges.createUnion(Arrays.asList(forange3, forange2, forange1));
		assertEquals("N [0, 15] U [20, 30]", foseq.getName());
	}
	
	@Test
	public void testUnion3()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(0, 10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(11, 15);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 30);
		// Union call order below is different to the naming order!
		FOOrderedEnumerableSet<FOInteger> foseq = FOSetSequenceOfRanges.createUnion(Arrays.asList(forange3, forange2, forange1));
		assertEquals("N [0, 15] U [20, 30]", foseq.getName());
	}	

	@Test
	public void testUnion4()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(0, 10);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(11, 15);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 30);
		FOSetRangedNaturals forange4 = new FOSetRangedNaturals(0, 100);
		// Union call order below is different to the naming order!
		FOOrderedEnumerableSet<FOInteger> foseq = FOSetSequenceOfRanges.createUnion(Arrays.asList(forange3, forange2, forange1, forange4));
		assertEquals("N [0, 100]", foseq.getName());
	}
	
	@Test
	public void testUnion5()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 10, true);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(10, 15);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(20, 30);
		// Union call order below is different to the naming order!
		FOOrderedEnumerableSet<FOInteger> foseq = FOSetSequenceOfRanges.createUnion(Arrays.asList(forange3, forange2, forange1));
		assertEquals("Z (-inf, 15] U [20, 30]", foseq.getName());
	}
	
	@Test
	public void testUnion6()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(0, 9);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(10, 20);
		FOSetRangedNaturals forange3 = new FOSetRangedNaturals(15, true, Integer.MAX_VALUE, false);
		// Union call order below is different to the naming order!
		FOOrderedEnumerableSet<FOInteger> foseq = FOSetSequenceOfRanges.createUnion(Arrays.asList(forange3, forange2, forange1));
		assertEquals("N", foseq.getName()); // simplifies to just N
		assertEquals(FOSetRangedNaturals.class, foseq.getClass()); // simplifies to just N
	}
	
	@Test
	public void testUnion7()
	{
		FOSetRangedNaturals forange1 = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 10, true);
		FOSetRangedNaturals forange2 = new FOSetRangedNaturals(5, true, Integer.MAX_VALUE, false);
		// Union call order below is different to the naming order!
		FOOrderedEnumerableSet<FOInteger> foseq = FOSetSequenceOfRanges.createUnion(Arrays.asList(forange2, forange1));
		assertEquals("Z", foseq.getName());
	}
}
