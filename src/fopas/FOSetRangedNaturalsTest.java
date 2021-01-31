package fopas;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Iterables;

import fopas.basics.FOConstant;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFunction;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOEnumerableSet;

public class FOSetRangedNaturalsTest {

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
	
	private void testFormula(FOStructure structure, String strFormula,
			boolean expectSatisfaction, String format) throws FOConstructionException
	{
		FOBRTestUtils.testFormula(builder, sgiser, structure, strFormula, expectSatisfaction, format);
	}
	private void testFormula(FOStructure structure, String strFormula,
			boolean expectSatisfaction, String format, boolean useExtended) throws FOConstructionException
	{
		FOBRTestUtils.testFormula(builder, sgiser, structure, strFormula, expectSatisfaction, format, useExtended);
	}
	private void testThrows(FOStructure structure, String strFormula, String expContains)
	{
		FOBRTestUtils.testThrows(builder, sgiser, structure, strFormula, expContains);
	}
	
	private FOStructure createStructureWithN()
	{
		FORelation<FOElement> foequals = new FORelationOfComparison.FORelationImplEquals();
		FOFunction funadd = new FOFunctionsInternalInt.FOInternalSumModulus();
		
		FOConstant c0 = new FOConstantImpl("c0");
		FOConstant c1 = new FOConstantImpl("c1");
		FOConstant c2 = new FOConstantImpl("c2");
		FOConstant c100 = new FOConstantImpl("c100");
		
		FOInteger zero = new FOElementImpl.FOIntImpl(0);
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		FOInteger hundred = new FOElementImpl.FOIntImpl(100);
		
		FOStructure structure = new FOStructureImpl(new FOSetRangedNaturals(), new HashSet<>(Arrays.asList(foequals)),
				new HashSet<>(Arrays.asList(funadd)));
		structure.setConstantMapping(c0, zero);
		structure.setConstantMapping(c1, one);
		structure.setConstantMapping(c2, two);
		structure.setConstantMapping(c100, hundred);
		return structure;
	}

	@Test
	public void testSimpleExpressions() throws FOConstructionException
	{
		FOStructure structure = createStructureWithN();
		
		testFormula(structure, "c0 = c1", false, "(%s)");
		testFormula(structure, "c1 = c1", true, "(%s)");

		// These are the only non-trivial formulas that terminate without range constraints.
		testFormula(structure, "(exists _v1)(_v1 = c0)", true, "%s");
		testFormula(structure, "(exists _v1)(_v1 = c100)", true, "%s");

		testFormula(structure, "(forall _v1)(_v1 = c0)", false, "%s");
		testFormula(structure, "(forall _v1)(_v1 = c1)", false, "%s");
	}


	@Test
	public void testN()
	{
		FOSetRangedNaturals foset = new FOSetRangedNaturals();
		Assert.assertEquals("N", foset.getName());
		Assert.assertEquals(-1, foset.size());
		Assert.assertEquals(0, foset.iterator().next().getInteger());
		Assert.assertEquals(0, foset.getStartOrInfinite(true).getInteger());
		Assert.assertEquals(0, foset.getStart().getInteger());
		Assert.assertEquals(-1, foset.getStartOrInfinite(false).getInteger());
		Assert.assertEquals(Integer.MAX_VALUE, foset.getEnd().getInteger());
		Assert.assertEquals(Integer.MAX_VALUE, foset.getEndOrInfinite(true).getInteger());
		Assert.assertEquals(Integer.MAX_VALUE, foset.getEndOrInfinite(false).getInteger());
	}
	
	@Test
	public void testZ()
	{
		FOSetRangedNaturals foset = new FOSetRangedNaturals(Integer.MIN_VALUE, false, Integer.MAX_VALUE, false);
		Assert.assertEquals("Z", foset.getName());
		Assert.assertEquals(-1, foset.size());
		Assert.assertEquals(0, foset.iterator().next().getInteger());// this is an exception where we start from 0 instead of the start.
		Assert.assertEquals(Integer.MIN_VALUE, foset.getStartOrInfinite(true).getInteger());
		Assert.assertEquals(Integer.MIN_VALUE, foset.getStartOrInfinite(false).getInteger());
		Assert.assertEquals(Integer.MIN_VALUE, foset.getStart().getInteger());
		Assert.assertEquals(Integer.MAX_VALUE, foset.getEnd().getInteger());
		Assert.assertEquals(Integer.MAX_VALUE, foset.getEndOrInfinite(true).getInteger());
		Assert.assertEquals(Integer.MAX_VALUE, foset.getEndOrInfinite(false).getInteger());
	}
	
	@Test
	public void testPositiveRange1()
	{
		FOSetRangedNaturals foset = new FOSetRangedNaturals(10, 99);
		Assert.assertEquals("N [10, 99]", foset.getName());
		Assert.assertEquals(90, foset.size());
		Assert.assertEquals(10, foset.iterator().next().getInteger());
		Assert.assertEquals(10, foset.getStartOrInfinite(true).getInteger());
		Assert.assertEquals(90, Iterables.size(foset));
		Assert.assertEquals(10, foset.getStart().getInteger());
		Assert.assertEquals(99, foset.getEnd().getInteger());
		Assert.assertEquals(99, foset.getEndOrInfinite(true).getInteger());
		Assert.assertTrue(foset.getIncludeStart());
		Assert.assertTrue(foset.getIncludeEnd());
	}

	@Test
	public void testPositiveRange2()
	{
		FOSetRangedNaturals foset = new FOSetRangedNaturals(10, true, 100, false);
		Assert.assertEquals("N [10, 100)", foset.getName());
		Assert.assertEquals(90, foset.size());
		Assert.assertEquals(10, foset.iterator().next().getInteger());
		Assert.assertEquals(10, foset.getStartOrInfinite(true).getInteger());
		Assert.assertEquals(90, Iterables.size(foset));
		Assert.assertEquals(10, foset.getStart().getInteger());
		Assert.assertEquals(100, foset.getEnd().getInteger());
		Assert.assertEquals(99, foset.getEndOrInfinite(true).getInteger());
		Assert.assertTrue(foset.getIncludeStart());
		Assert.assertFalse(foset.getIncludeEnd());
	}

	@Test
	public void testPositiveRange3()
	{
		FOSetRangedNaturals foset = new FOSetRangedNaturals(9, false, 99, true);
		Assert.assertEquals("N (9, 99]", foset.getName());
		Assert.assertEquals(90, foset.size());
		Assert.assertEquals(10, foset.iterator().next().getInteger());
		Assert.assertEquals(10, foset.getStartOrInfinite(true).getInteger());
		Assert.assertEquals(90, Iterables.size(foset));
		Assert.assertEquals(9, foset.getStart().getInteger());
		Assert.assertEquals(99, foset.getEnd().getInteger());
		Assert.assertEquals(99, foset.getEndOrInfinite(true).getInteger());
		Assert.assertFalse(foset.getIncludeStart());
		Assert.assertTrue(foset.getIncludeEnd());
	}

	@Test
	public void testPositiveRange4()
	{
		FOSetRangedNaturals foset = new FOSetRangedNaturals(9, false, 100, false);
		Assert.assertEquals("N (9, 100)", foset.getName());
		Assert.assertEquals(90, foset.size());
		Assert.assertEquals(10, foset.iterator().next().getInteger());
		Assert.assertEquals(10, foset.getStartOrInfinite(true).getInteger());
		Assert.assertEquals(90, Iterables.size(foset));
		Assert.assertEquals(9, foset.getStart().getInteger());
		Assert.assertEquals(100, foset.getEnd().getInteger());
		Assert.assertEquals(99, foset.getEndOrInfinite(true).getInteger());
		Assert.assertFalse(foset.getIncludeStart());
		Assert.assertFalse(foset.getIncludeEnd());
	}

	@Test
	public void testNegativeRange()
	{
		FOSetRangedNaturals foset = new FOSetRangedNaturals(-49, -40);
		Assert.assertEquals("Z [-49, -40]", foset.getName());
		Assert.assertEquals(10, foset.size());
		Assert.assertEquals(10, Iterables.size(foset));
		Assert.assertEquals(-40, foset.iterator().next().getInteger());
		Assert.assertEquals(-49, foset.getStartOrInfinite(true).getInteger());
		Assert.assertEquals(-49, foset.getStart().getInteger());
		Assert.assertEquals(-40, foset.getEnd().getInteger());
		Assert.assertEquals(-40, foset.getEndOrInfinite(true).getInteger());
	}

	@Test
	public void testXRange1()
	{
		FOSetRangedNaturals foset = new FOSetRangedNaturals(-10, 10);
		Assert.assertEquals("Z [-10, 10]", foset.getName());
		Assert.assertEquals(21, foset.size());
		Assert.assertEquals(-10, foset.iterator().next().getInteger());
		Assert.assertEquals(21, Iterables.size(foset));
	}

	@Test
	public void testXRange2()
	{
		FOSetRangedNaturals foset = new FOSetRangedNaturals(-11, false, 11, false);
		Assert.assertEquals("Z (-11, 11)", foset.getName());
		Assert.assertEquals(21, foset.size());
		Assert.assertEquals(-10, foset.iterator().next().getInteger());
		Assert.assertEquals(21, Iterables.size(foset));
	}

	@Test
	public void testInfNegativeRange1()
	{
		FOSetRangedNaturals foset = new FOSetRangedNaturals(Integer.MIN_VALUE, false, -5, true);
		Assert.assertEquals("Z (-inf, -5]", foset.getName());
		Assert.assertEquals(-1, foset.size());
		Assert.assertEquals(-5, foset.iterator().next().getInteger());
		Assert.assertEquals(Integer.MIN_VALUE, foset.getStartOrInfinite(true).getInteger());
		Assert.assertEquals(Integer.MIN_VALUE, foset.getStartOrInfinite(false).getInteger());
		Assert.assertEquals(Integer.MIN_VALUE, foset.getStart().getInteger());
		Assert.assertEquals(-5, foset.getEnd().getInteger());
		Assert.assertEquals(-5, foset.getEndOrInfinite(true).getInteger());
		Assert.assertEquals(-4, foset.getEndOrInfinite(false).getInteger());
	}

	@Test
	public void testInfNegativeRange2()
	{
		FOSetRangedNaturals foset = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 0, true);
		Assert.assertEquals("Z (-inf, 0]", foset.getName());
		Assert.assertEquals(-1, foset.size());
		Assert.assertEquals(0, foset.iterator().next().getInteger());
		Assert.assertEquals(Integer.MIN_VALUE, foset.getStartOrInfinite(true).getInteger());
		Assert.assertEquals(Integer.MIN_VALUE, foset.getStart().getInteger());
		Assert.assertEquals(0, foset.getEnd().getInteger());
		Assert.assertEquals(0, foset.getEndOrInfinite(true).getInteger());
	}

	@Test
	public void testNegNRange()
	{
		FOSetRangedNaturals foset = new FOSetRangedNaturals(Integer.MIN_VALUE, false, 0, true);
		Assert.assertEquals("Z (-inf, 0]", foset.getName());
		Assert.assertEquals(-1, foset.size());
		Assert.assertEquals(0, foset.iterator().next().getInteger());
	}

	@Test
	public void testConstrain1()
	{
		FOSetRangedNaturals foset1 = new FOSetRangedNaturals(0, true, Integer.MAX_VALUE, false);
		FOSetRangedNaturals foset2 = (FOSetRangedNaturals) foset1.constrainToRange(new FOElementImpl.FOIntImpl(1), new FOElementImpl.FOIntImpl(100));
		Assert.assertEquals("N [1, 100]", foset2.getName());
		Assert.assertEquals(100, foset2.size());
	}
	
	@Test
	public void testConstrain2()
	{
		FOSetRangedNaturals foset1 = new FOSetRangedNaturals(Integer.MIN_VALUE, false, Integer.MAX_VALUE, false);
		FOSetRangedNaturals foset2 = (FOSetRangedNaturals) foset1.constrainToRange(new FOElementImpl.FOIntImpl(-10), new FOElementImpl.FOIntImpl(Integer.MAX_VALUE));
		Assert.assertEquals("Z [-10, inf)", foset2.getName());
		Assert.assertEquals(-1, foset2.size());
	}
	
	@Test
	public void testComplement1a()
	{
		FOSetRangedNaturals foset1 = new FOSetRangedNaturals(10, 20);
		FOSetRangedNaturals foset2 = new FOSetRangedNaturals(0, 15);
		FOSetRangedNaturals foset2_1 = (FOSetRangedNaturals) foset1.complement(foset2);
		Assert.assertEquals("N [0, 9]", foset2_1.getName());
	}
	
	@Test
	public void testComplement1b()
	{
		FOSetRangedNaturals foset1 = new FOSetRangedNaturals(10, 20);
		FOSetRangedNaturals foset2 = new FOSetRangedNaturals(15, 25);
		FOSetRangedNaturals foset2_1 = (FOSetRangedNaturals) foset1.complement(foset2);
		Assert.assertEquals("N [21, 25]", foset2_1.getName());
	}

	@Test
	public void testComplement1c()
	{
		FOSetRangedNaturals foset1 = new FOSetRangedNaturals(-10, 20);
		FOSetRangedNaturals foset2 = new FOSetRangedNaturals(-15, 15);
		FOSetRangedNaturals foset2_1 = (FOSetRangedNaturals) foset1.complement(foset2);
		Assert.assertEquals("Z [-15, -11]", foset2_1.getName());
	}

	@Test
	public void testComplement1d()
	{
		FOSetRangedNaturals foset1 = new FOSetRangedNaturals(15, 20);
		FOSetRangedNaturals foset2 = new FOSetRangedNaturals(0, 15);
		FOSetRangedNaturals foset2_1 = (FOSetRangedNaturals) foset1.complement(foset2);
		Assert.assertEquals("N [0, 14]", foset2_1.getName());
	}
	
	@Test
	public void testComplement1e()
	{
		FOSetRangedNaturals foset1 = new FOSetRangedNaturals(15, 20);
		FOSetRangedNaturals foset2 = new FOSetRangedNaturals(20, 25);
		FOSetRangedNaturals foset2_1 = (FOSetRangedNaturals) foset1.complement(foset2);
		Assert.assertEquals("N [21, 25]", foset2_1.getName());
	}

	@Test
	public void testComplement2()
	{
		FOSetRangedNaturals foset1 = new FOSetRangedNaturals(); //N
		FOSetRangedNaturals foset2 = new FOSetRangedNaturals(Integer.MIN_VALUE, false, Integer.MAX_VALUE, false);
		FOSetRangedNaturals foset2_1 = (FOSetRangedNaturals) foset1.complement(foset2);
		Assert.assertEquals("Z (-inf, -1]", foset2_1.getName());
	}
	
	@Test
	public void testComplement3a()
	{
		FOSetRangedNaturals foset1 = new FOSetRangedNaturals(10, 20);
		FOSetRangedNaturals foset2 = new FOSetRangedNaturals(0, 30);
		FOEnumerableSet<FOInteger> foset2_1 = (FOEnumerableSet<FOInteger>) foset1.complement(foset2);
		Assert.assertEquals(20, foset2_1.size());
		Assert.assertEquals(20, Iterables.size(foset2_1));
		Assert.assertEquals(0, foset2_1.iterator().next().getInteger());
		Assert.assertEquals("N [0, 30] \\ N [10, 20]", foset2_1.getName());
	}
	
	@Test
	public void testComplement3b()
	{
		FOSetRangedNaturals foset1 = new FOSetRangedNaturals(10, 20);
		FOSetRangedNaturals foset2 = new FOSetRangedNaturals(5, true, Integer.MAX_VALUE, false);
		FOEnumerableSet<FOInteger> foset2_1 = (FOEnumerableSet<FOInteger>) foset1.complement(foset2);
		Assert.assertEquals(-1, foset2_1.size());
		// TODO: test that iterating this throws
		Assert.assertEquals(5, foset2_1.iterator().next().getInteger());
		Assert.assertEquals("N [5, inf) \\ N [10, 20]", foset2_1.getName());
	}
}

