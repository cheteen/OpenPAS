package fopas;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

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

public class FORangedNaturalsTest {

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
		
		FOStructure structure = new FOStructureImpl(new FORangedNaturals(), new HashSet<>(Arrays.asList(foequals)),
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

}
