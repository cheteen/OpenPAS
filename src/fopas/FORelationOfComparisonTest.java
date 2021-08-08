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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fopas.basics.FOConstant;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFunction;
import fopas.basics.FORelation;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOVariable;
import fopas.basics.FOElement.FOInteger;
import fopas.FOBRTestUtils;
import fopas.FORelationOfComparison.FORelationImplEquals;
import fopas.FORuntime.FOStats;

public class FORelationOfComparisonTest {

	FOFormulaBuilderByRecursion builder;
	FOByRecursionStringiser sgiser;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception
	{
		builder = new FOFormulaBuilderByRecursion();
		sgiser = new FOByRecursionStringiser();
	}

	@After
	public void tearDown() throws Exception {
	}

	
	public void testFormula(FOStructure structure, String strFormula, boolean expectSatisfaction, String format)
	{
		try
		{
			FOBRTestUtils.testFormula(builder, sgiser, structure, strFormula, expectSatisfaction, format);
		} catch (FOConstructionException e)
		{
			e.printStackTrace();
			assertFalse(e.getMessage(), true);
		}
	}
	
	static class NoConstrainEquals extends FORelationImplEquals
	{
		@Override
		public String getInfix() { return "#="; }
		@Override
		public String getName() { return "NoConstrainEquals"; }
		

		@Override
		public int getPrecedence() { return super.getPrecedence() + 1; }
		@Override
		public <TI extends FOElement> FOSet<? extends TI> tryConstrain(FOVariable var, FOSet<TI> universeSubset, List<FOTerm> terms, boolean isComplemented)
		{
			// Disable constrain so we force checkAsg in action.
			return null;
		}	
	}
	
	@Test
	public void testEquals()
	{
		FORelation<FOElement> rel = new FORelationOfComparison.FORelationImplEquals();
		assertEquals(rel.getInfix(), "=");
		assertEquals(rel.getName(), "Equals");
		assertFalse(rel.satisfies(new FOElementImpl.FOIntImpl(10), new FOElementImpl.FOIntImpl(9)));
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(9), new FOElementImpl.FOIntImpl(9)));
		assertTrue(rel.satisfies(new FOElementImpl.FOStringImpl("Alo"), new FOElementImpl.FOStringImpl("Alo")));
		assertFalse(rel.satisfies(new FOElementImpl.FOStringImpl("Alo"), new FOElementImpl.FOStringImpl("Yalo")));
	}

	@Test
	public void testGreater()
	{
		FORelation<FOElement> rel = new FORelationOfComparison.FORelationImplInequality<FOElement>(FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR, false, false, FOElement.class); // >
		assertEquals(rel.getInfix(), ">");
		assertEquals(rel.getName(), "GreaterThan");
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(10), new FOElementImpl.FOIntImpl(9)));
		assertFalse(rel.satisfies(new FOElementImpl.FOIntImpl(9), new FOElementImpl.FOIntImpl(9)));
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(100), new FOElementImpl.FOIntImpl(0)));
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(0), new FOElementImpl.FOIntImpl(-1)));
		assertFalse(rel.satisfies(new FOElementImpl.FOIntImpl(1), new FOElementImpl.FOIntImpl(2)));
		// TODO: Test order, strings, etc.
	}

	@Test
	public void testGreaterEq()
	{
		FORelation<FOElement> rel = new FORelationOfComparison.FORelationImplInequality<FOElement>(FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR, false, true, FOElement.class); // >=
		assertEquals(rel.getInfix(), ">=");
		assertEquals(rel.getName(), "GreaterThanEquals");
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(10), new FOElementImpl.FOIntImpl(9)));
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(9), new FOElementImpl.FOIntImpl(9)));
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(100), new FOElementImpl.FOIntImpl(0)));
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(0), new FOElementImpl.FOIntImpl(-1)));
		assertFalse(rel.satisfies(new FOElementImpl.FOIntImpl(1), new FOElementImpl.FOIntImpl(2)));
	}

	@Test
	public void testLessThan()
	{
		FORelation<FOElement> rel = new FORelationOfComparison.FORelationImplInequality<FOElement>(FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR, true, false, FOElement.class); // <
		assertEquals(rel.getInfix(), "<");
		assertEquals(rel.getName(), "LessThan");
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(9), new FOElementImpl.FOIntImpl(10)));
		assertFalse(rel.satisfies(new FOElementImpl.FOIntImpl(9), new FOElementImpl.FOIntImpl(9)));
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(0), new FOElementImpl.FOIntImpl(100)));
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(-1), new FOElementImpl.FOIntImpl(0)));
		assertFalse(rel.satisfies(new FOElementImpl.FOIntImpl(2), new FOElementImpl.FOIntImpl(1)));
	}

	@Test
	public void testLessEq()
	{
		FORelation<FOElement> rel = new FORelationOfComparison.FORelationImplInequality<FOElement>(FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR, true, true, FOElement.class); // <=
		assertEquals(rel.getInfix(), "<=");
		assertEquals(rel.getName(), "LessThanEquals");
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(9), new FOElementImpl.FOIntImpl(10)));
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(9), new FOElementImpl.FOIntImpl(9)));
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(0), new FOElementImpl.FOIntImpl(100)));
		assertTrue(rel.satisfies(new FOElementImpl.FOIntImpl(-1), new FOElementImpl.FOIntImpl(0)));
		assertFalse(rel.satisfies(new FOElementImpl.FOIntImpl(2), new FOElementImpl.FOIntImpl(1)));
	}

	static FOStructure createStructureIneq()
	{
		return createStructureIneqWithRange(0, 1000);
	}

	static FOStructure createStructureIneqWithRange(int first, int last)
	{
		FOConstant c0 = new FOConstantImpl("c0");
		FOConstant c10 = new FOConstantImpl("c10");
		FOConstant c20 = new FOConstantImpl("c20");
		FOConstant c50 = new FOConstantImpl("c50");
		FOConstant c100 = new FOConstantImpl("c100");
		FOConstant c1000 = new FOConstantImpl("c1000");
		FOConstant cm100 = new FOConstantImpl("cm100");
		
		FOInteger fi0 = new FOElementImpl.FOIntImpl(0);
		FOInteger fi10 = new FOElementImpl.FOIntImpl(10);
		FOInteger fi20 = new FOElementImpl.FOIntImpl(20);
		FOInteger fi50 = new FOElementImpl.FOIntImpl(50);
		FOInteger fi100 = new FOElementImpl.FOIntImpl(100);
		FOInteger fim100 = new FOElementImpl.FOIntImpl(-100);
		FOInteger fi1000 = new FOElementImpl.FOIntImpl(1000);

		FOSet<FOInteger> universe = new FOSetRangedNaturals(
				first, first == Integer.MIN_VALUE ? false : true,
				last, last == Integer.MAX_VALUE ? false : true);
		Set<FORelation<FOElement>> rels = new HashSet<>();
		rels.add(new FORelationOfComparison.FORelationImplEquals());
		rels.add(new FORelationOfComparison.FORelationImplInequality<FOElement>(FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR, false, false, FOElement.class));
		rels.add(new FORelationOfComparison.FORelationImplInequality<FOElement>(FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR, false, true, FOElement.class));
		rels.add(new FORelationOfComparison.FORelationImplInequality<FOElement>(FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR, true, false, FOElement.class));
		rels.add(new FORelationOfComparison.FORelationImplInequality<FOElement>(FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR, true, true, FOElement.class));
		rels.add(new NoConstrainEquals());
		
		FOFunction funaddmod = new FOFunctionsInternalInt.FOInternalSumModulus(1000);
		
		FOStructure structure = new FOStructureImpl(universe, rels, new HashSet<>(Arrays.asList(funaddmod)));
		structure.setConstantMapping(c0, fi0);
		structure.setConstantMapping(c10, fi10);
		structure.setConstantMapping(c20, fi20);
		structure.setConstantMapping(c50, fi50);
		structure.setConstantMapping(c100, fi100);
		structure.setConstantMapping(c1000, fi1000);
		if(first <= fim100.getInteger())
			structure.setConstantMapping(cm100, fim100);
		return structure;
	}
	
	@Test
	public void testFormulaExistsGreaterThan()
	{
		FOStructure structure = createStructureIneq();
		testFormula(structure, "(exists _v1)(_v1 > c50)", true, "%s");
		FOStats stats = structure.getRuntime().getStats();
	
		// Should constrain universe to [51, 1000]
		assertEquals(1, stats.numL1ElimTrueRelAttempts);
		assertEquals(1, stats.numL1ElimTrueForallSuccess);
		// Should fail after trying only 51 once - and therefore succeed with (negated) assignment.
		assertEquals(1, stats.numL1CheckAsgRel);
		assertEquals(1, stats.numL1CheckAsgAllSub);
		assertEquals(1, stats.numL1CheckAsgAllSubFail);
	}

	@Test
	public void testFormulaForallLessThan()
	{
		FOStructure structure = createStructureIneq();
		testFormula(structure, "(forall _v1)(_v1 < c100 -> _v1 < c1000)", true, "(forall _v1)((_v1 < c100) -> (_v1 < c1000))");
		FOStats stats = structure.getRuntime().getStats();
		
		assertEquals(1, stats.numL1ElimTrueForallSuccess); // one elimtrue needed at forall level
		assertEquals(1, stats.numL1ElimTrueForallSuccess0); // it eliminated to emptyset
		assertEquals(2, stats.numL1ElimTrueRelAttempts); // this was throught two successive relation level elimtrues.

		assertEquals(1, stats.numL1CheckAsgAll); // one forall used
		assertEquals(0, stats.numL1CheckAsgAllSub); // forall succeeded w/o trying a single assignment
	}

	@Test
	public void testConstrainedEquals()
	{
		FOStructure structure = createStructureIneq();
		testFormula(structure, "(forall _v1)(_v1 < c100 -> ¬(_v1 = c100))", true, "(forall _v1)((_v1 < c100) -> ¬(_v1 = c100))");
		FOStats stats = structure.getRuntime().getStats();

		assertEquals(2, stats.numL1ElimTrueRelAttempts); // one for <100 another for =100		
		assertEquals(1, stats.numL1ElimTrueForallSuccess);
		assertEquals(1, stats.numL1ElimTrueForallSuccess1);
		assertEquals(0, stats.numL1ElimTrueForallSuccess0); // at target 0 this should've been 1

		assertEquals(1, stats.numL1CheckAsgAll); // one forall used
		assertEquals(1, stats.numL1CheckAsgAllSub); // forall goes to subformula once
		assertEquals(1, stats.numL1CheckAsgRel);
	}


	@Test
	public void testNoConstrainEquals()
	{
		FOStructure structure = createStructureIneq();
		testFormula(structure, "(forall _v1)(_v1 < c100 -> ¬(_v1 #= c100))", true, "(forall _v1)((_v1 < c100) -> ¬(_v1 #= c100))");
		FOStats stats = structure.getRuntime().getStats();
		
		assertEquals(2, stats.numL1ElimTrueRelAttempts); // one for <100 another for =100		
		assertEquals(1, stats.numL1ElimTrueForallSuccess);
		assertEquals(0, stats.numL1ElimTrueForallSuccess1);
		assertEquals(0, stats.numL1ElimTrueForallSuccess0);

		assertEquals(1, stats.numL1CheckAsgAll); // one forall used
		assertEquals(100, stats.numL1CheckAsgAllSub); // forall goes to subformula 100 times after the first constrain
		assertEquals(200, stats.numL1CheckAsgRel); // 2x rel checks per loop
		assertEquals(100, stats.numL1CheckAsgOr);
	}
	//TODO: Add another test as above to create multiple ranges.

	@Test
	public void testMultiAndedFalse()
	{
		FOStructure structure = createStructureIneqWithRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
		
		testFormula(structure,
				"(exists _v1)((_v1 >= c0 & _v1 < c10 & _v1 #= c10))", false,
				"(exists _v1)((_v1 >= c0) & (_v1 < c10) & (_v1 #= c10))");
		FOStats stats = structure.getRuntime().getStats();
		// Should look at 10 possibilities, then give up.
		assertEquals(10, stats.numL1CheckAsgOr);
		// There are three relations to try and elimTrue, one of the is designed to fail
		assertEquals(3, stats.numL1ElimTrueRelAttempts);
		// And the other two (inequalities) should succeed.
		assertEquals(2, stats.numL1ElimTrueRelSuccess);
	}
	
	@Test
	public void testMultiAndedTrue()
	{
		// This test is suboptimal because it relies on the order of execution for suceeeding, not the logical setup.
		FOStructure structure = createStructureIneqWithRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
		
		testFormula(structure,
				"(exists _v1)((_v1 > c0 & _v1 <= c10 & _v1 #= c10))", true,
				"(exists _v1)((_v1 > c0) & (_v1 <= c10) & (_v1 #= c10))");
		FOStats stats = structure.getRuntime().getStats();
		// Should look at 10 possibilities, then give up.
		assertEquals(10, stats.numL1CheckAsgOr);
		// There are three relations to try and elimTrue, one of the is designed to fail
		assertEquals(3, stats.numL1ElimTrueRelAttempts);
		// And the other two (inequalities) should succeed.
		assertEquals(2, stats.numL1ElimTrueRelSuccess);
	}


	@Test
	public void testMultiOred()
	{
		FOStructure structure = createStructureIneqWithRange(0, Integer.MAX_VALUE);
		
		testFormula(structure,
				"(exists _v1)((_v1 < c10 | _v1 < c20) & _v1 #= c20)", false,
				"(exists _v1)(((_v1 < c10) | (_v1 < c20)) & (_v1 #= c20))");
		FOStats stats = structure.getRuntime().getStats();
		// 50 rel checks - 10 for the first only + (10+10) for the second + 20 for the third.
		assertEquals(50, stats.numL1CheckAsgRel);
		// Should look at 20 possibilities, then give up.
		assertEquals(20, stats.numL1CheckAsgAllSub);
		// There are three relations to try and elimTrue, one of the is designed to fail
		assertEquals(3, stats.numL1ElimTrueRelAttempts);
		// And the other two (inequalities) should succeed.
		assertEquals(2, stats.numL1ElimTrueRelSuccess);
		stats.printStats(System.out);
	}

	@Test
	public void testMultiTermsFalse()
	{
		FOStructure structure = createStructureIneqWithRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
		
		testFormula(structure,
				"(exists _v1)(((_v1 >= c0 & _v1 < c10) | ((_v1 >= c20 & _v1 < c50))) & _v1 #= c50)", false,
				"(exists _v1)((((_v1 >= c0) & (_v1 < c10)) | ((_v1 >= c20) & (_v1 < c50))) & (_v1 #= c50))");
		FOStats stats = structure.getRuntime().getStats();
		// Should look at 10 possibilities, then give up.
//		assertEquals(10, stats.numL1CheckAsgOr);
//		// There are three relations to try and elimTrue, one of the is designed to fail
//		assertEquals(3, stats.numL1ElimTrueRelAttempts);
//		// And the other two (inequalities) should succeed.
//		assertEquals(2, stats.numL1ElimTrueRelSuccess);
		stats.printStats(System.out);
	}

	@Test
	public void testNoConstrainMultiRange()
	{
		FOStructure structure = createStructureIneqWithRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
		testFormula(structure,
				"(forall _v1)((_v1 > c0 & _v1 < c10 | _v1 > c20 & _v1 < c50) -> ¬(_v1 #= c100))", true, 
				"(forall _v1)((((_v1 > c0) & (_v1 < c10)) | ((_v1 > c20) & (_v1 < c50))) -> ¬(_v1 #= c100))");
//		FOStats stats = structure.getRuntime().getStats();
//		stats.printStats(System.out);
	}
	
	@Test
	public void testInfiniteUniverse()
	{
		FOStructure structure = createStructureIneqWithRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
		//assertEquals(Integer.MAX_VALUE, structure.getUniverse().size());
		testFormula(structure, "(forall _v1)((_v1 > cm100 & _v1 < c100) -> (¬(_v1 = c100) & ¬(_v1 = cm100)))", true,
				"(forall _v1)(((_v1 > cm100) & (_v1 < c100)) -> (¬(_v1 = c100) & ¬(_v1 = cm100)))");
//		FOStats stats = structure.getRuntime().getStats();
//		stats.printStats(System.out);
	}

	@Test
	public void testTwoIneq()
	{
		FOStructure structure = createStructureIneqWithRange(Integer.MIN_VALUE, Integer.MAX_VALUE);
		//assertEquals(Integer.MAX_VALUE, structure.getUniverse().size());
		testFormula(structure, "(forall _v1)((_v1 > c0 & _v1 < c10) -> ¬(_v1 = cm100))", true,
				"(forall _v1)(((_v1 > c0) & (_v1 < c10)) -> ¬(_v1 = cm100))");
//		FOStats stats = structure.getRuntime().getStats();
//		stats.printStats(System.out);
	}
}
