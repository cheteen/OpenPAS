package fopas;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
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
import fopas.basics.FOElement.FOInteger;
import fopas.FOBRTestUtils;
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
			assertFalse(true);
		}
	}
	
	//TODO: Add tests that directly test the relation as a unit test.

	static FOStructure createStructureIneq()
	{
		FOConstant c0 = new FOConstantImpl("c0");
		FOConstant c50 = new FOConstantImpl("c50");
		FOConstant c100 = new FOConstantImpl("c100");
		FOConstant c1000 = new FOConstantImpl("c1000");
		
		FOInteger f0 = new FOElementImpl.FOIntImpl(0);
		FOInteger fi50 = new FOElementImpl.FOIntImpl(50);
		FOInteger fi100 = new FOElementImpl.FOIntImpl(100);
		FOInteger fi1000 = new FOElementImpl.FOIntImpl(1000);

		FOSet<FOInteger> universe = new FOSetRangedNaturals(0, 1000);
		Set<FORelation<FOElement>> rels = new HashSet<>();
		rels.add(new FORelationOfComparison.FORelationImplEquals());
		rels.add(new FORelationOfComparison.FORelationImplInequality(FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR, false, false));
		rels.add(new FORelationOfComparison.FORelationImplInequality(FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR, false, true));
		rels.add(new FORelationOfComparison.FORelationImplInequality(FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR, true, false));
		rels.add(new FORelationOfComparison.FORelationImplInequality(FOElementImpl.FOIntImpl.DEFAULT_COMPARATOR, true, true));
		
		FOFunction funaddmod4 = new FOFunctionsInternalInt.FOInternalSumModulus(4);
		
		FOStructure structure = new FOStructureImpl(universe, rels, new HashSet<>(Arrays.asList(funaddmod4)));
		structure.setConstantMapping(c0, f0);
		structure.setConstantMapping(c1000, fi1000);
		structure.setConstantMapping(c50, fi50);
		structure.setConstantMapping(c100, fi100);
		return structure;
	}
	
	@Test
	public void testLessThan()
	{
		FOStructure structure = createStructureIneq();
		testFormula(structure, "(exists _v1)(_v1 > c50)", true, "%s");
		FOStats stats = structure.getRuntime().getStats();
	
		// Should constrain universe to [51, 1000]
		assertEquals(1, stats.numL1ElimTrueRel);
		assertEquals(1, stats.numL1ElimTrueSuccess);
		// Should fail after trying only 51 once - and therefore succeed with (negated) assignment.
		assertEquals(1, stats.numL1CheckAsgRel);
		assertEquals(1, stats.numL1CheckAsgAllSub);
		assertEquals(1, stats.numL1CheckAsgAllSubFail);
	}
}
