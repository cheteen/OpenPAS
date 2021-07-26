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
import java.util.HashSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Iterables;

import fopas.FOSetUtils.EmptySet;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOElement.FOString;
import fopas.basics.FOElement.FOSymbol;
import fopas.basics.FOEnumerableSet;
import fopas.basics.FOSet;

public class FOEnumerableUnionSetImplTest {

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

	private FOBridgeSet<FOSymbol> createMathsSmybolsSet()
	{
		FOSymbol exp = new FOElementImpl.FOSymbolImpl("exp");
		FOSymbol pi = new FOElementImpl.FOSymbolImpl("pi");
		FOSymbol sum = new FOElementImpl.FOSymbolImpl("sum");
		FOBridgeSet<FOSymbol> symbols = new FOBridgeSet<>("maths_symbols", new HashSet<>(Arrays.asList(exp, pi, sum)), FOSymbol.class);
		return symbols;
	}

	private FOBridgeSet<FOString> createNamesSet()
	{
		FOString burak = new FOElementImpl.FOStringImpl("burak");
		FOString selami = new FOElementImpl.FOStringImpl("selami");
		FOString suleyman = new FOElementImpl.FOStringImpl("suleyman");
		FOBridgeSet<FOString> names = new FOBridgeSet<>("names", new HashSet<>(Arrays.asList(burak, selami, suleyman)), FOString.class);
		return names;
	}


	private FOEnumerableUnionSetImpl<FOElement> createUnionSetRN() throws FOConstructionException
	{
		FOSetRangedNaturals range = new FOSetRangedNaturals(10, 14);
		FOBridgeSet<FOString> names = createNamesSet();
		FOEnumerableUnionSetImpl<FOElement> unionSet = new FOEnumerableUnionSetImpl<>(Arrays.asList(range, names), FOElement.class);
		return unionSet;
	}

	private FOEnumerableUnionSetImpl<FOElement> createUnionSetRNS() throws FOConstructionException
	{
		FOSetRangedNaturals range = new FOSetRangedNaturals(10, 14);
		FOBridgeSet<FOString> names = createNamesSet();
		FOBridgeSet<FOSymbol> symbols = createMathsSmybolsSet();
		FOEnumerableUnionSetImpl<FOElement> unionSet = new FOEnumerableUnionSetImpl<>(Arrays.asList(range, names, symbols), FOElement.class);
		return unionSet;
	}

	@Test
	public void testSetCreation() throws FOConstructionException
	{
		FOEnumerableUnionSetImpl<FOElement> unionSet = createUnionSetRN();
		
		assertEquals("N [10, 14] U names", unionSet.getName());
		assertEquals(8, unionSet.size());
		assertEquals(unionSet.size(), Iterables.size(unionSet));
		assertTrue(unionSet.contains(new FOElementImpl.FOStringImpl("burak")));
		assertFalse(unionSet.contains(new FOElementImpl.FOStringImpl("mulayim")));
	}

	@Test
	public void testSetCreationThrows()
	{
		FOEnumerableSet<FOElement> unionSet = null;
		try
		{
			unionSet = new FOEnumerableUnionSetImpl<>(Arrays.asList(createNamesSet()), FOElement.class);
		}
		catch (FOConstructionException e)
		{
			assertTrue(e.getMessage().contains("Can't create union set with fewer than 2 sets"));
		}
		assertNull(unionSet); // ensures the exception was thrown.
	}

	@Test
	public void testSetComplement1() throws FOConstructionException
	{
		FOEnumerableUnionSetImpl<FOElement> unionSetRN = createUnionSetRN();
		FOEnumerableUnionSetImpl<FOElement> unionSetRNS = createUnionSetRNS();
		
		{
			FOSet<? extends FOElement> complementS = unionSetRN.complementAcross(unionSetRNS);
			assertEquals("maths_symbols", complementS.getName());			
		}
		{
			FOSet<? extends FOElement> complement = unionSetRN.complement(unionSetRNS);
			assertNull(complement);
		}
		{
			FOSet<? extends FOElement> complementEmpty = unionSetRNS.complement(unionSetRN);
			assert(complementEmpty instanceof EmptySet);
		}
		{
			FOSet<? extends FOElement> complementEmpty = unionSetRNS.complement(unionSetRN);
			assertTrue(complementEmpty.size() == 0);
			assertTrue(complementEmpty instanceof EmptySet);
		}
	}

	@Test
	public void testSetComplement2() throws FOConstructionException
	{
		FOEnumerableUnionSetImpl<FOElement> unionSetRNS = createUnionSetRNS();
		FOEnumerableUnionSetImpl<FOElement> unionSetRN = createUnionSetRN();
		FOBridgeSet<FOSymbol> setS = createMathsSmybolsSet();
		
		{
			FOSet<? extends FOElement> complementRN = setS.complementAcross(unionSetRNS);
			assertEquals(unionSetRN, complementRN);
		}
	}

	@Test
	public void testSetComplement3() throws FOConstructionException
	{
		FOEnumerableUnionSetImpl<FOElement> unionSetRNS = createUnionSetRNS();
		EmptySet<FOElement> emptyElementSet = new EmptySet<>(FOElement.class);
		
		{
			FOSet<? extends FOElement> complementSetRNS = emptyElementSet.complementAcross(unionSetRNS);
			assertEquals(complementSetRNS, unionSetRNS);
		}
	}
}
