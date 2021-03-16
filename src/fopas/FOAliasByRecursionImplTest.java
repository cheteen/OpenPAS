package fopas;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.FluentIterable;

import fopas.FOElementImpl.FOIntImpl;
import fopas.FORuntime.FOStats;
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
import fopas.basics.FOEnumerableSet;

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
		sgiser = new FOByRecursionStringiser(200);
	}

	@After
	public void tearDown() throws Exception {
	}

	private FOStructure createSimpleStructure()
	{
		return createSimpleStructure(new FORuntime());
	}
	
	private FOStructure createSimpleStructure(FORuntime runtime)
	{
		FOConstant c0 = new FOConstantImpl("c0");
		FOConstant c1 = new FOConstantImpl("c1");
		FOConstant c2 = new FOConstantImpl("c2");
		FOConstant c3 = new FOConstantImpl("c3");
		FOConstant c4 = new FOConstantImpl("c4");
		
		FOInteger zero = new FOElementImpl.FOIntImpl(0);
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		FOInteger three = new FOElementImpl.FOIntImpl(3);
		FOInteger four = new FOElementImpl.FOIntImpl(4);
		
		FOEnumerableSet<FOInteger> universe = new FOBridgeSet<>("SOMEINTS", new LinkedHashSet<>(Arrays.asList(zero, one, two, three, four)), FOInteger.class);		
		FORelation<FOElement> foequals = new FORelationOfComparison.FORelationImplEquals();
		
		FOFunction funaddmod5 = new FOFunctionsInternalInt.FOInternalSumModulus(5);
		FOFunction funsubmod5 = new FOFunctionsInternalInt.FOInternalSubtractModulus(5);
		
		FOStructure structure = new FOStructureImpl(new FOEnumerableUnionSetImpl(universe), new HashSet<>(Arrays.asList(foequals)), new HashSet<>(Arrays.asList(funaddmod5, funsubmod5)), runtime);
		structure.setConstantMapping(c0, zero);
		structure.setConstantMapping(c1, one);
		structure.setConstantMapping(c2, two);
		structure.setConstantMapping(c3, three);
		structure.setConstantMapping(c4, four);
		return structure;
	}	

	private void testFormula(FOStructure structure, String strFormula, boolean expectSatisfaction, String format) throws FOConstructionException
	{
		FOBRTestUtils.testFormula(builder, sgiser, structure, strFormula, expectSatisfaction, format);
	}
	
	@Test
	public void testSimpleAliasAdd() throws FOConstructionException
	{
		FOStructure structure = createSimpleStructure();
		
		FOAlias formAlias = builder.buildAlias(structure, 
				"add",
				Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("z")), "_z = (_x + _y)");
		
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
		FOAlias formAlias = builder.buildAlias(structure, 
				"substract",
				Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("z")), "_x = (_y + _z)");
		
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
		FOAlias formAlias = builder.buildAlias(structure, 
				"substract",
				Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("z")), "_x = (_y + _z)");
		
		structure.addAlias(formAlias);

		FOFormula form = builder.buildFormula("substract(c3, c3, _v)", structure);

		Assert.assertEquals(5, FluentIterable.from(structure.getAssignments(form)).size());
		Assert.assertEquals(1, FluentIterable.from(structure.getSatisfyingAssignments(form)).size());

		FOVariable fovarV = new FOVariableImpl("v");
		Assert.assertEquals(0, form.getSatisfyingAssignments(structure).iterator().next().get(fovarV).getElement());
		
		//printAssignments(structure, (FOFormulaByRecursionImpl) form, false);
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
				builder.buildAlias(structure, 
						"map_choices",
							Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("y0"), new FOVariableImpl("y1")),
							"¬(_x = c0 | _x = c1) |"
						+ 	" ¬(¬(_x = c0) | ¬(_y = _y0)) |"
						+ 	" ¬(¬(_x = c1) | ¬(_y = _y1))")
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
		
		testFormula(structure, "¬map_choices(_x, _y, c3, c2) | ¬(_x = c2) | (_y = c0) | (_y = c1) | (_y = c2) | (_y = c3) | (_y = c4)", true, "(%s)");
	}

	@Test
	public void testMultiplyUsingRecursionTwoRelations() throws FOConstructionException
	{
		FOStructure structure = createSimpleStructure();
		
		// This defines: x - y = z
		structure.addAlias(
				builder.buildAlias(structure, 
						"substract",
						Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("z")), "_x = (_y + _z)")				
				);

		// Define: x * y = z as multiply(x, y, z) 
		// This implements multiplication using only the existing addition function recursively.
		// Defines its own subtraction from addition.
		FOAlias formAlias = builder.buildAlias(structure, 
				"multiply",
				Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("z")),
					"(_x = c0 -> _z = c0)"
				+ 	"& (_x = c1 -> _z = _y)"
				+   "& (forall _x1)((forall _z1)(¬(_x = c0) & ¬(_x = c1) & substract(_x, c1, _x1) & multiply(_x1, _y, _z1) -> _z = _z1 + _y))"
				);
		
		structure.addAlias(formAlias);
				
		FOStats stats = structure.getRuntime().getStats();
	
		// This set of formulas fail in the worst possible way. elimTrue fails completely, therefore a full
		// assignment is done for both forall's.
		
		//Base case 0
		// true
		testFormula(structure, "multiply(c0, c2, c0)", true, null);
		Assert.assertEquals("Only one entry to the alias - no recursion.", 1, stats.numL1CheckAsgIntoAlias);
		Assert.assertEquals("All elimTrues shuold fail!", 0, stats.numL1ElimTrueForallSuccess1);
		Assert.assertEquals("Same as above.", 0, stats.numL1ElimTrueForallSuccess);
		Assert.assertEquals("Should fail 6 times. One for outer forall, 5x for inner [0-4]", 6, stats.numL1ElimTrueRepeatCall);
		Assert.assertEquals("Since all elimTrue fails, this should get 6 times like the above.", 6, stats.numL1CheckAsgAll);
		Assert.assertEquals("Like the above, all universe gets called for two nested foralls: 5 for the outher, 25 for the innter", 30, stats.numL1CheckAsgAllSub);
		Assert.assertEquals("0 should fail to give satisfaction.", 0, stats.numL1CheckAsgAllSubFail);
		// false
		testFormula(structure, "multiply(c0, c2, c2)", false, null);

		//Base case 1
		testFormula(structure, "multiply(c1, c2, c2)", true, null);
		testFormula(structure, "multiply(c1, c3, c3)", true, null);
		testFormula(structure, "multiply(c1, c2, c1)", false, null);

		// Recursive case
		testFormula(structure, "multiply(c2, c0, c0)", true, null);
		testFormula(structure, "multiply(c2, c1, c0)", false, null);
		testFormula(structure, "multiply(c2, c1, c2)", true, null);
		testFormula(structure, "multiply(c2, c2, c4)", true, null);
		testFormula(structure, "multiply(c4, c4, c1)", true, null); // 4*4 mode 5=16 mod 5=1
		testFormula(structure, "multiply(c4, c4, c0)", false, null); 
		testFormula(structure, "multiply(c4, c3, c2)", true, null); // 4*3 mode 5=12 mod 5=2
		testFormula(structure, "multiply(c3, c4, c2)", true, null); // 3*4 mode 5=12 mod 5=2
	}

	@Test
	public void testMultiplyUsingRecursionOneRelation() throws FOConstructionException
	{
		FOStructure structure = createSimpleStructure();

		// This will have an elimTrue target size of 1 (by default) unlike the next test.
		Assert.assertEquals("Unexpected elimTrue size target.", 1, structure.getRuntime().getTargetElimTrue());

		// Define: x * y = z as multiply(x, y, z) 
		// This implements multiplication using only the existing addition function recursively.
		// But uses a given subtraction function instead of defining it from addition like the above.
		// This tries to optimise the use of forall's as much as possible using elimTrues, but the
		// implementation below gets a direct hit w/o using forall's so it should be faster.
		FOAlias formAlias = builder.buildAlias(structure, 
				"multiply",
				Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("z")),
					"(_x = c0 -> _z = c0)"
				+ 	"& (_x = c1 -> _z = _y)"
				+   "& (forall _x1)((forall _z1)(¬(_x = c0) & ¬(_x = c1) & _x1 = _x - c1 & _z1 = _z - _y -> multiply(_x1, _y, _z1)))"
				);
	
		structure.addAlias(formAlias);

		FOStats stats = structure.getRuntime().getStats();

		//Base case 0
		// Fail
		testFormula(structure, "multiply(c0, c2, c2)", false, null);
		Assert.assertEquals(1, stats.numL1CheckAsgIntoAlias);
		Assert.assertEquals("Should fail in the first step of the two OR formulas (one being the first inside the other).", 2, stats.numL1CheckAsgOr);
		Assert.assertEquals("Should fail right away w/o needing elimTrue.", 0, stats.numL1ElimTrueRelAttempts);
		Assert.assertEquals("Should fail before executing forall.", 0, stats.numL1CheckAsgAll);
		Assert.assertEquals("Does two rel checks for evaluating the first implication.", 2, stats.numL1CheckAsgRel);
		// Success
		testFormula(structure, "multiply(c0, c2, c0)", true, null);
		Assert.assertEquals("Should figure out x1=4 and z1=3 to check the recursive case directly.", 2, stats.numL1ElimTrueForallSuccess1);
		Assert.assertEquals("Shouldn't do any other elimTrue other than the above.", 2, stats.numL1ElimTrueForallSuccess);
		Assert.assertEquals("No elimTrue failures since we will accept the first time we get a set of size 1 for x1 and z1.", 0, stats.numL1ElimTrueRepeatCall);
		Assert.assertEquals("Check assignment should be called once for each forall x1 and z1.", 2, stats.numL1CheckAsgAll);
		Assert.assertEquals("In each forall, there should be once check since elimTrue succeeded precisely.", 2, stats.numL1CheckAsgAllSub);
		Assert.assertEquals("Same as above in checking the failures that should be 0.", 0, stats.numL1CheckAsgAllSubFail);
		
		//Base case 1
		// Success
		testFormula(structure, "multiply(c1, c2, c2)", true, null);
		Assert.assertEquals("Should figure out x1 and z1 values to check the recursive case directly.", 2, stats.numL1ElimTrueForallSuccess1);
		Assert.assertEquals("Shouldn't do any other elimTrue other than the above.", 2, stats.numL1ElimTrueForallSuccess);
		Assert.assertEquals("No elimTrue failures since we will accept the first time we get a set of size 1 for x1 and z1.", 0, stats.numL1ElimTrueRepeatCall);
		Assert.assertEquals("Check assignment should be called once for each forall x1 and z1.", 2, stats.numL1CheckAsgAll);
		Assert.assertEquals("In each forall, there should be once check since elimTrue succeeded precisely.", 2, stats.numL1CheckAsgAllSub);
		Assert.assertEquals("Same as above in checking the failures that should be 0.", 0, stats.numL1CheckAsgAllSubFail);

		testFormula(structure, "multiply(c1, c3, c3)", true, null);
		testFormula(structure, "multiply(c1, c2, c1)", false, null);

		// Recursive case
		testFormula(structure, "multiply(c2, c0, c0)", true, null);
		Assert.assertEquals("Should do one level of recursion, so we have (2, 0, 0) as entry point, and then (1, 0, 0).", 2, stats.numL1CheckAsgIntoAlias);
		Assert.assertEquals("Should figure out x1 and z1 for both cases above to check the recursive case directly, so we have 2x2=4.", 4, stats.numL1ElimTrueForallSuccess1);
		Assert.assertEquals("Shouldn't do any other elimTrue other than the above.", 4, stats.numL1ElimTrueForallSuccess);
		Assert.assertEquals("No elimTrue failures since we will accept the first time we get a set of size 1 for x1 and z1.", 0, stats.numL1ElimTrueRepeatCall);
		Assert.assertEquals("Check assignment should be called once for each forall x1 and z1 for each alias call.", 4, stats.numL1CheckAsgAll);
		Assert.assertEquals("In each forall, there should be once check since elimTrue succeeded precisely.", 4, stats.numL1CheckAsgAllSub);
		Assert.assertEquals("Same as above in checking the failures that should be 0.", 0, stats.numL1CheckAsgAllSubFail);
		Assert.assertEquals("With an elimTrue target of 1 this does fewer elimTrueRel checks than target 0 (test below)", 14, stats.numL1ElimTrueRelAttempts);

		testFormula(structure, "multiply(c2, c1, c0)", false, null);
		testFormula(structure, "multiply(c2, c1, c2)", true, null);
		testFormula(structure, "multiply(c2, c2, c4)", true, null);
		testFormula(structure, "multiply(c4, c4, c1)", true, null); // 4*4 mode 5=16 mod 5=1
		testFormula(structure, "multiply(c4, c4, c0)", false, null); 
		testFormula(structure, "multiply(c4, c3, c2)", true, null); // 4*3 mode 5=12 mod 5=2
		testFormula(structure, "multiply(c3, c4, c2)", true, null); // 3*4 mode 5=12 mod 5=2
	}

	// We force the elimTrue target to be 0 here to see that it spends more time.
	@Test
	public void testMultiplyUsingRecursionOneRelationElimTarget0() throws FOConstructionException
	{
		FORuntime runtime = new FORuntime(0);
		FOStructure structure = createSimpleStructure(runtime);
		
		// Define: x * y = z as multiply(x, y, z) 
		// This implements multiplication using only the existing addition function recursively.
		// But uses a given subtraction function instead of defining it from addition like the above.
		// This tries to optimise the use of forall's as much as possible using elimTrues, but the
		// implementation below gets a direct hit w/o using forall's so it should be faster.
		FOAlias formAlias = builder.buildAlias(structure, 
				"multiply",
				Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("z")),
					"(_x = c0 -> _z = c0)"
				+ 	"& (_x = c1 -> _z = _y)"
				+   "& (forall _x1)((forall _z1)(¬(_x = c0) & ¬(_x = c1) & _x1 = _x - c1 & _z1 = _z - _y -> multiply(_x1, _y, _z1)))"
				);
	
		structure.addAlias(formAlias);

		FOStats stats = structure.getRuntime().getStats();

		//Base case 0
		// Fail
		testFormula(structure, "multiply(c0, c2, c2)", false, null);
		Assert.assertEquals(1, stats.numL1CheckAsgIntoAlias);
		Assert.assertEquals("Should fail in the first step of the two OR formulas (one being the first inside the other).", 2, stats.numL1CheckAsgOr);
		Assert.assertEquals("Should fail right away w/o needing elimTrue.", 0, stats.numL1ElimTrueRelAttempts);
		Assert.assertEquals("Should fail before executing forall.", 0, stats.numL1CheckAsgAll);
		Assert.assertEquals("Does two rel checks for evaluating the first implication.", 2, stats.numL1CheckAsgRel);
		// Success
		testFormula(structure, "multiply(c0, c2, c0)", true, null);
		Assert.assertEquals("Should figure out x1=4 and z1=3 to check the recursive case directly.", 2, stats.numL1ElimTrueForallSuccess1);
		Assert.assertEquals("Shouldn't do any other elimTrue other than the above.", 2, stats.numL1ElimTrueForallSuccess);
		Assert.assertEquals("Should stop due to repeat once for x1 and then for z1.", 2, stats.numL1ElimTrueRepeatCall);
		Assert.assertEquals("Check assignment should be called once for each forall x1 and z1.", 2, stats.numL1CheckAsgAll);
		Assert.assertEquals("In each forall, there should be once check since elimTrue succeeded precisely.", 2, stats.numL1CheckAsgAllSub);
		Assert.assertEquals("Same as above in checking the failures that should be 0.", 0, stats.numL1CheckAsgAllSubFail);
		
		//Base case 1
		// Success
		testFormula(structure, "multiply(c1, c2, c2)", true, null);
		Assert.assertEquals("Should figure out x1 and z1 values to check the recursive case directly.", 2, stats.numL1ElimTrueForallSuccess1);
		Assert.assertEquals("Shouldn't do any other elimTrue other than the above.", 2, stats.numL1ElimTrueForallSuccess);
		Assert.assertEquals("Should stop due to repeat once for x1 and then for z1.", 2, stats.numL1ElimTrueRepeatCall);
		Assert.assertEquals("Check assignment should be called once for each forall x1 and z1.", 2, stats.numL1CheckAsgAll);
		Assert.assertEquals("In each forall, there should be once check since elimTrue succeeded precisely.", 2, stats.numL1CheckAsgAllSub);
		Assert.assertEquals("Same as above in checking the failures that should be 0.", 0, stats.numL1CheckAsgAllSubFail);

		testFormula(structure, "multiply(c1, c3, c3)", true, null);
		testFormula(structure, "multiply(c1, c2, c1)", false, null);

		// Recursive case
		testFormula(structure, "multiply(c2, c0, c0)", true, null);
		Assert.assertEquals("Should do one level of recursion, so we have (2, 0, 0) as entry point, and then (1, 0, 0).", 2, stats.numL1CheckAsgIntoAlias);
		Assert.assertEquals("Should figure out x1 and z1 for both cases above to check the recursive case directly, so we have 2x2=4.", 4, stats.numL1ElimTrueForallSuccess1);
		Assert.assertEquals("Shouldn't do any other elimTrue other than the above.", 4, stats.numL1ElimTrueForallSuccess);
		Assert.assertEquals("Should stop due to repeat once for x1 and then for z1 for each alias call.", 4, stats.numL1ElimTrueRepeatCall);
		Assert.assertEquals("Check assignment should be called once for each forall x1 and z1 for each alias call.", 4, stats.numL1CheckAsgAll);
		Assert.assertEquals("In each forall, there should be once check since elimTrue succeeded precisely.", 4, stats.numL1CheckAsgAllSub);
		Assert.assertEquals("Same as above in checking the failures that should be 0.", 0, stats.numL1CheckAsgAllSubFail);
		// This is the only different test to the above case where it does 50 more elimTrueRel checks compared to the one above.
		Assert.assertEquals("With an elimTrue target of 1 this does fewer elimTrueRel checks than target 0 (test below)", 64, stats.numL1ElimTrueRelAttempts);

		testFormula(structure, "multiply(c2, c1, c0)", false, null);
		testFormula(structure, "multiply(c2, c1, c2)", true, null);
		testFormula(structure, "multiply(c2, c2, c4)", true, null);
		testFormula(structure, "multiply(c4, c4, c1)", true, null); // 4*4 mode 5=16 mod 5=1
		testFormula(structure, "multiply(c4, c4, c0)", false, null); 
		testFormula(structure, "multiply(c4, c3, c2)", true, null); // 4*3 mode 5=12 mod 5=2
		testFormula(structure, "multiply(c3, c4, c2)", true, null); // 3*4 mode 5=12 mod 5=2
	}

	@Test
	public void testInfiniteRecursionThrows() throws FOConstructionException
	{
		FORuntime runtime = new FORuntime(0);
		FOStructure structure = createSimpleStructure(runtime);
		
		FOAlias formAlias2 = builder.buildAlias(structure, 
		"infinine",
		Arrays.asList(new FOVariableImpl("x")),
			"(forall _x1)(_x = c0 & infinine(_x1))"
		);
		structure.addAlias(formAlias2);
		
		FOBRTestUtils.testThrows(builder, sgiser, structure, "infinine(c0)", "java.lang.StackOverflowError");
	}
	
	
	@Test
	public void testMultiplyUsingRecursionNoRelation() throws FOConstructionException
	{
		FOStructure structure = createSimpleStructure();
		
		// Define: x * y = z as multiply(x, y, z) 
		// This implements multiplication using only the existing addition function recursively.
		// Uses no intermediate relations, but adds its thing directly to the recursive alias call.
		// This means it doesn't have to have any forall, no elimTrues, and so it's the fastest.
		FOAlias formAlias = builder.buildAlias(structure, 
				"multiply",
				Arrays.asList(new FOVariableImpl("x"), new FOVariableImpl("y"), new FOVariableImpl("z")),
					"(_x = c0 -> _z = c0)"
				+ 	"& (_x = c1 -> _z = _y)"
				+   "& (¬(_x = c0) & ¬(_x = c1) -> multiply(_x - c1, _y, _z - _y))"
				);
	
		structure.addAlias(formAlias);

		FOStats stats = structure.getRuntime().getStats();

		//Base case 0
		// Fail
		testFormula(structure, "multiply(c0, c2, c2)", false, null);
		Assert.assertEquals(1, stats.numL1CheckAsgIntoAlias);
		Assert.assertEquals("Should fail in the first step of the two OR formulas (one being the first inside the other).", 2, stats.numL1CheckAsgOr);
		Assert.assertEquals("Should fail right away w/o needing elimTrue.", 0, stats.numL1ElimTrueRelAttempts);
		Assert.assertEquals("Should fail before executing forall.", 0, stats.numL1CheckAsgAll);
		Assert.assertEquals("Does two rel checks for evaluating the first implication.", 2, stats.numL1CheckAsgRel);
		// Success
		testFormula(structure, "multiply(c0, c2, c0)", true, null);
		Assert.assertEquals("No recursive alias call.", 1, stats.numL1CheckAsgIntoAlias);
		Assert.assertEquals("No need for elimTrue.", 0, stats.numL1ElimTrueForallSuccess1);
		Assert.assertEquals("No need for elimTrue.", 0, stats.numL1ElimTrueForallSuccess);
		Assert.assertEquals("No need for elimTrue.", 0, stats.numL1ElimTrueRepeatCall);
		Assert.assertEquals("No forall.", 0, stats.numL1CheckAsgAll);
		Assert.assertEquals("No forall.", 0, stats.numL1CheckAsgAllSub);
		Assert.assertEquals("No forall.", 0, stats.numL1CheckAsgAllSubFail);
		
		//Base case 1
		// Success
		testFormula(structure, "multiply(c1, c2, c2)", true, null);
		Assert.assertEquals("No recursive alias call.", 1, stats.numL1CheckAsgIntoAlias);
		Assert.assertEquals("No need for elimTrue.", 0, stats.numL1ElimTrueForallSuccess);

		testFormula(structure, "multiply(c1, c3, c3)", true, null);
		testFormula(structure, "multiply(c1, c2, c1)", false, null);

		// Recursive case
		testFormula(structure, "multiply(c2, c0, c0)", true, null);
		Assert.assertEquals("No need for elimTrue.", 0, stats.numL1ElimTrueForallSuccess);
		Assert.assertEquals("No forall.", 0, stats.numL1CheckAsgAll);
		Assert.assertEquals("One recursive alias call +1 regular alias call.", 2, stats.numL1CheckAsgIntoAlias);

		testFormula(structure, "multiply(c2, c1, c0)", false, null);
		testFormula(structure, "multiply(c2, c1, c2)", true, null);
		testFormula(structure, "multiply(c2, c2, c4)", true, null);

		testFormula(structure, "multiply(c4, c4, c1)", true, null); // 4*4 mode 5=16 mod 5=1
		Assert.assertEquals("3 recursive alias call +1 regular alias call from c4->c1.", 4, stats.numL1CheckAsgIntoAlias);

		testFormula(structure, "multiply(c4, c4, c0)", false, null); 
		testFormula(structure, "multiply(c4, c3, c2)", true, null); // 4*3 mode 5=12 mod 5=2

		testFormula(structure, "multiply(c3, c4, c2)", true, null); // 3*4 mode 5=12 mod 5=2
		Assert.assertEquals("2 recursive alias call +1 regular alias call from c3->c1.", 3, stats.numL1CheckAsgIntoAlias);
	}
}
