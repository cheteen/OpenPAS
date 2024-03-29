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

import java.util.function.Function;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;

import fopas.basics.FOOrderedEnumerableSet;
import fopas.basics.FORange;
import fopas.basics.FOSet;
import fopas.basics.FOEnumerableSet;
import fopas.FOSetUtils.ComplementedSingleElementSet;
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
		FOOrderedEnumerableSet<FOInteger> emptySet = new FOSetUtils.EmptySet<>(FOInteger.class);
		assertEquals("(Empty)", emptySet.getName());
		FOSet<FOInteger> other = new FOSetRangedNaturals(10,100);
		assertTrue(other == emptySet.complement(other));
		assertTrue(other == emptySet.complementAcross(other));
		assertEquals(0, emptySet.size());
		assertFalse(emptySet.contains(new FOElementImpl.FOIntImpl(0)));
		assertFalse(emptySet.iterator().hasNext());
		assertTrue(emptySet == emptySet.constrainToRange(new FOElementImpl.FOIntImpl(0), new FOElementImpl.FOIntImpl(19)));
		FOBRTestUtils.assertThrows(() -> {emptySet.getFirstOrInfinite(); }, "Empty set has no first element");
		FOBRTestUtils.assertThrows(() -> {emptySet.getLastOrInfinite(); }, "Empty set has no last element");
		FOBRTestUtils.assertThrows(() -> {emptySet.getNextOrNull(new FOElementImpl.FOIntImpl(0)); }, "Empty set has no next element");
		FOBRTestUtils.assertThrows(() -> {emptySet.getPreviousOrNull(new FOElementImpl.FOIntImpl(0)); }, "Empty set has no previous element");
	}

	@Test
	public void testSingleEltSet()
	{
		FOOrderedEnumerableSet<FOInteger> singleSet = new FOSetUtils.SingleElementSet<FOInteger>(new FOElementImpl.FOIntImpl(10), FOInteger.class);
		assertEquals("{10}", singleSet.getName());
		FOSet<FOInteger> other = new FOSetRangedNaturals(10,99);
		assertEquals(10, singleSet.getFirstOrInfinite().getInteger());
		assertEquals(10, singleSet.getLastOrInfinite().getInteger());		
		assertEquals(FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR, singleSet.getOrder());
		
		{
			FOSet<? extends FOInteger> compSet = singleSet.complement(other);
			assertFalse(compSet.contains(new FOElementImpl.FOIntImpl(10)));
			assertTrue(compSet.contains(new FOElementImpl.FOIntImpl(11)));
			assertEquals(89, compSet.size());
			assertTrue(compSet instanceof ComplementedSingleElementSet);
			assertEquals(FOInteger.class, compSet.getType());

			assertEquals(compSet, singleSet.complement(other));
		}
		
		{
			FOOrderedEnumerableSet<FOInteger> constrained = singleSet.constrainToRange(new FOElementImpl.FOIntImpl(5), new FOElementImpl.FOIntImpl(15));
			assertTrue(singleSet == constrained);
		}
		{
			FOOrderedEnumerableSet<FOInteger> constrained = singleSet.constrainToRange(new FOElementImpl.FOIntImpl(5), new FOElementImpl.FOIntImpl(10));
			assertTrue(singleSet == constrained);
		}
		{
			FOOrderedEnumerableSet<FOInteger> constrained = singleSet.constrainToRange(new FOElementImpl.FOIntImpl(1), new FOElementImpl.FOIntImpl(5));
			assertEquals("(Empty)", constrained.getName());
		}
	}

	@Test
	public void testSingleEltComplementSet1()
	{
		FOOrderedEnumerableSet<FOInteger> singleSet = new FOSetUtils.SingleElementSet<FOInteger>(new FOElementImpl.FOIntImpl(10), FOInteger.class);
		FOOrderedEnumerableSet<FOInteger> singleSet2 = new FOSetUtils.SingleElementSet<FOInteger>(new FOElementImpl.FOIntImpl(100), FOInteger.class);
		
		assertTrue(singleSet.complement(singleSet2) == singleSet2);
	}

	@Test
	public void testSingleEltComplementSet2()
	{
		FOOrderedEnumerableSet<FOInteger> singleSet = new FOSetUtils.SingleElementSet<FOInteger>(new FOElementImpl.FOIntImpl(10), FOInteger.class);
		FOSet<FOInteger> other = new FOSetRangedNaturals(20,100);
		
		assertTrue(singleSet.complement(other) == other);
	}

	@Test
	public void testSingleEltComplementSet3()
	{
		FOOrderedEnumerableSet<FOInteger> singleSet = new FOSetUtils.SingleElementSet<FOInteger>(new FOElementImpl.FOIntImpl(10), FOInteger.class);
		FOSet<FOInteger> other = new FOSetRangedNaturals(0,100);
		FOEnumerableSet<FOInteger> complement = (FOEnumerableSet<FOInteger>) singleSet.complement(other);
		
		assertTrue(!complement.contains(new FOElementImpl.FOIntImpl(10)));
		assertTrue(complement.contains(new FOElementImpl.FOIntImpl(0)));
		assertTrue(complement.contains(new FOElementImpl.FOIntImpl(100)));
		assertTrue(complement.contains(new FOElementImpl.FOIntImpl(50)));
		assertEquals(100, Iterables.size(complement));
	}
}
