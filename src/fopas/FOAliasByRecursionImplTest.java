package fopas;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.FluentIterable;

import fopas.basics.FOAlias;
import fopas.basics.FOConstant;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FOFunction;
import fopas.basics.FORelation;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOVariable;
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
		FOFormula form = builder.buildFormula(strFormula, structure);
		
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
		
		testFormula(structure, "add(c1, c2, c3)", true, null);
		testFormula(structure, "add(c1, c0, c1)", true, null);
		testFormula(structure, "add((c1 + c2), c0, c3)", true, null);
		testFormula(structure, "add(c1 + c2, c0, c3)", true, "add((c1 + c2), c0, c3)");
		testFormula(structure, "add(c1 + c0, c0 + c1, c1 + c1)", true, "add((c1 + c0), (c0 + c1), (c1 + c1))");
	}

	@Test
	public void testSimpleAliasSubstract() throws FOConstructionException
	{
		FOStructure structure = createSimpleStructure();
		
		// This defines: x - y = z\
		FOAlias formAlias = builder.buildAlias("substract", 
				Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("z")),
				"_x = (_y + _z)", structure);
		
		structure.addAlias(formAlias);
		
		testFormula(structure, "substract(c3, c2, c1)", true, null);
		testFormula(structure, "substract(c3, c3, c1)", false, null);
		testFormula(structure, "substract(c3, c3, c0)", true, null);
		testFormula(structure, "¬(substract(c3, c1, c0))", true, "¬substract(c3, c1, c0)");
		testFormula(structure, "¬substract(c3, c1, c0)", true, null);
	}

	@Test
	public void testSubstractEquationSolving() throws FOConstructionException
	{
		FOStructure structure = createSimpleStructure();
		
		// This defines: x - y = z\
		FOAlias formAlias = builder.buildAlias("substract", 
				Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("z")),
				"_x = (_y + _z)", structure);
		
		structure.addAlias(formAlias);

		FOFormula form = builder.buildFormula("substract(c3, c3, _v)", structure);

		Assert.assertEquals(4, FluentIterable.from(structure.getAssignments(form)).size());
		Assert.assertEquals(1, FluentIterable.from(structure.getSatisfyingAssignments(form)).size());

		FOVariable fovarV = new FOVariableImpl("v");
		Assert.assertEquals(0, form.getSatisfyingAssignments(structure).iterator().next().get(fovarV).getElement());
		
		//printAssignments(structure, (FOFormulaByRecursionImpl) form, false);
	}

	private void printAssignments(FOStructure structure, FOFormulaByRecursionImpl form, boolean satisfying) throws FOConstructionException 
	{
		Iterable<Map<FOVariable, FOElement>> assigners;
		if(satisfying)
			assigners = form.getSatisfyingAssignments(structure);
		else
			assigners = form.getAssignments(structure);
		
		for(Map<FOVariable, FOElement> asg : assigners)
		{
			StringBuilder sb = new StringBuilder();
			sb.append(":");
			for(FOVariable var : asg.keySet())
				sb.append("[" + var.getName() + "=" + asg.get(var).getElement() + "]");

			System.out.println(sb);
		}
	}
	
	@Test
	public void testSimpleImplicationUsingOr() throws FOConstructionException
	{
		FOStructure structure = createSimpleStructure();
		
		// map_choices (x, y, y0, y1):
		// if(x = 0) then y = y0
		// if(x = 1) then y = y1
		//
		// This is implemented using: (x = 0 -> y = y0) & (x = 1 -> y = y1) 
		// 
		// Using only | and ¬ this becomes:
		//
		// map_choices(x, y, y0, y1) := ¬(x = 0 | x = 1) | ¬(x = 0 | y = y1) | ¬(x = 1 | y = y0)
		// 
		structure.addAlias(
				builder.buildAlias("map_choices", 
						Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("y0"), new FOVariableImpl("y1")),
							"¬(_x = c0 | _x = c1) |"
						+ 	" ¬(¬(_x = c0) | ¬(_y = _y0)) |"
						+ 	" ¬(¬(_x = c1) | ¬(_y = _y1))",
						structure)
				);
		
		// (map_choices(x, y, c3, c2) & (x = 0)) -> (y = c3)
		testFormula(structure, "¬map_choices(_x, _y, c3, c2) | ¬(_x = c0) | (_y = c3)", true, "(%s)");
		testFormula(structure, "¬map_choices(_x, _y, c3, c2) | ¬(_x = c1) | (_y = c2)", true, "(%s)");
		testFormula(structure, "¬map_choices(_x, _y, c3, c2) | ¬(_x = c0) | (_y = c1)", false, "(%s)");
		
		// When x isn't in the map (it isn't c0 or c1) anyone of them is possible.
		testFormula(structure, "¬map_choices(_x, _y, c3, c2) | ¬(_x = c2) | (_y = c0)", false, "(%s)");
		testFormula(structure, "¬map_choices(_x, _y, c3, c2) | ¬(_x = c2) | (_y = c1)", false, "(%s)");
		testFormula(structure, "¬map_choices(_x, _y, c3, c2) | ¬(_x = c2) | (_y = c2)", false, "(%s)");
		testFormula(structure, "¬map_choices(_x, _y, c3, c2) | ¬(_x = c2) | (_y = c3)", false, "(%s)");
		
		testFormula(structure, "¬map_choices(_x, _y, c3, c2) | ¬(_x = c2) | (_y = c0) | (_y = c1) | (_y = c2) | (_y = c3)", true, "(%s)");
	}

	@Test
	public void testSimpleArithmetics() throws FOConstructionException
	{
		FOStructure structure = createSimpleStructure();
		
		// This defines: x - y = z
		structure.addAlias(
				builder.buildAlias("substract", 
						Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("z")),
						"_x = (_y + _z)", structure)				
				);

		// Define: x * y = z
		// multiply(x, y, z) :-
		// (((x = 0) -> (_z = c0)) & 		% base case 0
		// ((x = 1) -> (_z = _y))) |		% base case 1
		// (substract(_x, c1, _x1) & (multiply(_x1, _y, _z1) & (_z = (_z1 + _y)))) % recursive case
//		FOAlias formAlias = builder.buildAlias("multiply", 
//				Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("z")),
//				"_x", structure);
//		
//		structure.addAlias(formAlias);
//		
//		testFormula(structure, "substract(c3, c2, c1)", true, "(%s)");
//		testFormula(structure, "substract(c1, c1, c1)", true, "(%s)");
	}
}
