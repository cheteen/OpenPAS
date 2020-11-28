package fopas;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fopas.basics.FOAlias;
import fopas.basics.FOConstant;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FOFunction;
import fopas.basics.FORelation;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOElement.FOInteger;

public class FOAliasByRecursionImplTest
{
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
	
	private FOStructure createSimpleStructure()
	{
		FOConstant c0 = new FOConstantImpl("c0");
		FOConstant c1 = new FOConstantImpl("c1");
		FOConstant c2 = new FOConstantImpl("c2");
		FOConstant c3 = new FOConstantImpl("c3");
		
		FOInteger zero = new FOElementImpl.FOIntImpl(0);
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		FOInteger three = new FOElementImpl.FOIntImpl(3);
		
		FOSet<FOElement> universe = new FOBridgeSet<>("FOURINTS", new LinkedHashSet<>(Arrays.asList(zero, one, two, three)));		
		FORelation<FOElement> foequals = new FORelationImpl.FORelationImplEquals();
		
		FOFunction funaddmod4 = new FOInternalIntFunctions.FOInternalSumModulus(4);
		
		FOStructure structure = new FOStructureImpl(new FOUnionSetImpl(universe), new HashSet<>(Arrays.asList(foequals)), new HashSet<>(Arrays.asList(funaddmod4)));
		structure.setConstantMapping(c0, zero);
		structure.setConstantMapping(c1, one);
		structure.setConstantMapping(c2, two);
		structure.setConstantMapping(c3, three);
		return structure;
	}	

	private void testFormula(FOStructure structure, String strFormula, boolean expectSatisfaction, String format) throws FOConstructionException
	{
		FOFormula form = builder.buildFrom(strFormula, structure);
		
		String strReForm = sgiser.stringiseFOFormula(form, 100);
		if(format == null)
			Assert.assertEquals(strFormula, strReForm);
		else
		{
			String strReFormReformat = String.format(format, strFormula);
			Assert.assertEquals(strReFormReformat, strReForm);
		}
		
		Assert.assertEquals(expectSatisfaction, structure.models(form));
	}
	
	@Test
	public void testSimpleAliasAdd() throws FOConstructionException
	{
		FOStructure structure = createSimpleStructure();
		
		FOAlias formAlias = builder.buildAlias("add", 
				Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("z")),
				"_z = (_x + _y)", structure);
		
		structure.addAlias(formAlias);
		
		testFormula(structure, "add(c1, c2, c3)", true, "(%s)");
	}

	@Test
	public void testSimpleAliasSubstract() throws FOConstructionException
	{
		FOStructure structure = createSimpleStructure();
		
		// This defines: x - y = z
		FOAlias formAlias = builder.buildAlias("substract", 
				Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("z")),
				"_x = (_y + _z)", structure);
		
		structure.addAlias(formAlias);
		
		testFormula(structure, "substract(c3, c2, c1)", true, "(%s)");
	}
}
