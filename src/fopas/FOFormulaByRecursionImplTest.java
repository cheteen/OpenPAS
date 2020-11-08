package fopas;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fopas.basics.FOConstant;
import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOFormula;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOVariable;

public class FOFormulaByRecursionImplTest {

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
	public void testConstants() throws FORuntimeException
	{
		FOConstant c1 = new FOContantImpl("c1");
		FOConstant c2 = new FOContantImpl("c2");
		
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		
		FOSet<FOElement> universe = new FOBridgeSet<>(new HashSet<>(Arrays.asList(one, two)));
		
		FOStructure structure = new FOStructureImpl(universe);
		structure.setConstantMapping(c1, one);
		structure.setConstantMapping(c2, two);
		
		FORelation<FOElement> foequals = new FORelationImpl.FORelationImplEquals();
		
		FOTermByRecursionImpl.FOTermConstant term_constant1 = new FOTermByRecursionImpl.FOTermConstant(c1);
		FOTermByRecursionImpl.FOTermConstant term_constant2 = new FOTermByRecursionImpl.FOTermConstant(c2);

		FOByRecursionStringiser sgiser = new FOByRecursionStringiser();

		// Test that the same constant is equal to itself.
		{
			FOFormula form = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant1, term_constant1));
			Assert.assertTrue(structure.models(form));
			
			Assert.assertEquals("(c1 = c1)", sgiser.stringiseFOFormula(form, 100));
		}
		
		// Test that different constants with different values differ.
		{
			FOFormula form = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant1, term_constant2));
			Assert.assertFalse(structure.models(form));			

			Assert.assertEquals("(c1 = c2)", sgiser.stringiseFOFormula(form, 100));
		}
		
		// Check that a different constant with the same mapping succeeds in being equal.
		FOConstant c3 = new FOContantImpl("c3");
		structure.setConstantMapping(c3, one);
		FOTermByRecursionImpl.FOTermConstant term_constant3 = new FOTermByRecursionImpl.FOTermConstant(c3);
		{
			FOFormula form = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant1, term_constant3));
			Assert.assertTrue(structure.models(form));			

			Assert.assertEquals("(c1 = c3)", sgiser.stringiseFOFormula(form, 100));
		}

		// Test that negation works.
		{
			FOFormula form = new FOFormulaByRecursionImpl.FOFormulaBRRelation(true, foequals, Arrays.asList(term_constant1, term_constant1));
			Assert.assertFalse(structure.models(form));
			
			Assert.assertEquals("¬(c1 = c1)", sgiser.stringiseFOFormula(form, 100));
		}
	}

	@Test
	public void testOrFormulas() throws FORuntimeException
	{
		FOConstant c1 = new FOContantImpl("c1");
		FOConstant c2 = new FOContantImpl("c2");
		FOConstant c3 = new FOContantImpl("c3");
		
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		FOInteger three = new FOElementImpl.FOIntImpl(3);
		
		FOSet<FOElement> universe = new FOBridgeSet<>(new HashSet<>(Arrays.asList(one, two, three)));
		
		FOStructure structure = new FOStructureImpl(universe);
		structure.setConstantMapping(c1, one);
		structure.setConstantMapping(c2, two);
		structure.setConstantMapping(c3, three);
		
		FORelation<FOElement> foequals = new FORelationImpl.FORelationImplEquals();
		
		FOTermByRecursionImpl.FOTermConstant term_constant1 = new FOTermByRecursionImpl.FOTermConstant(c1);
		FOTermByRecursionImpl.FOTermConstant term_constant2 = new FOTermByRecursionImpl.FOTermConstant(c2);
		FOTermByRecursionImpl.FOTermConstant term_constant3 = new FOTermByRecursionImpl.FOTermConstant(c3);

		FOByRecursionStringiser sgiser = new FOByRecursionStringiser();

		// Test Or fomula satisfies.
		{
			FOFormula subform1 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant1, term_constant2));
			FOFormula subform2 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant2, term_constant3));
			FOFormula subform3 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant3, term_constant3));

			FOFormula form = new FOFormulaByRecursionImpl.FOFormulaBROr(false, Arrays.asList(subform1, subform2, subform3));
			Assert.assertEquals("((c1 = c2) + (c2 = c3) + (c3 = c3))", sgiser.stringiseFOFormula(form, 100));

			Assert.assertTrue(structure.models(form));			
		}

		// Test Or formula fails.
		{
			FOFormula subform1 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant1, term_constant2));
			FOFormula subform2 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant2, term_constant3));
			FOFormula subform3 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant3, term_constant1));

			FOFormula form = new FOFormulaByRecursionImpl.FOFormulaBROr(false, Arrays.asList(subform1, subform2, subform3));
			Assert.assertEquals("((c1 = c2) + (c2 = c3) + (c3 = c1))", sgiser.stringiseFOFormula(form, 100));

			Assert.assertFalse(structure.models(form));			
		}
	}

	@Test
	public void testForAllFormulas() throws FORuntimeException
	{
		FOConstant c1 = new FOContantImpl("c1");
		FOConstant c2 = new FOContantImpl("c2");
		FOConstant c3 = new FOContantImpl("c3");
		
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		FOInteger three = new FOElementImpl.FOIntImpl(3);
		
		FOSet<FOElement> universe = new FOBridgeSet<>(new HashSet<>(Arrays.asList(one, two, three)));
		
		FOStructure structure = new FOStructureImpl(universe);
		structure.setConstantMapping(c1, one);
		structure.setConstantMapping(c2, two);
		structure.setConstantMapping(c3, three);
		
		FORelation<FOElement> foequals = new FORelationImpl.FORelationImplEquals();
		
		FOTermByRecursionImpl.FOTermConstant term_constant1 = new FOTermByRecursionImpl.FOTermConstant(c1);
		FOTermByRecursionImpl.FOTermConstant term_constant2 = new FOTermByRecursionImpl.FOTermConstant(c2);
		FOTermByRecursionImpl.FOTermConstant term_constant3 = new FOTermByRecursionImpl.FOTermConstant(c3);

		FOByRecursionStringiser sgiser = new FOByRecursionStringiser();

		FOVariable v1 = new FOVariableImpl("v1");
		FOTermByRecursionImpl.FOTermVariable term_var1 = new FOTermByRecursionImpl.FOTermVariable(v1);

		// Test forall success
		// (\forall _v1)(_v1 = c1 + _v1 = c2 + _v1 = c3)  
		{
			FOFormula subsubform1 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant1));
			FOFormula subsubform2 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant2));
			FOFormula subsubform3 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant3));

			FOFormula subform1 = new FOFormulaByRecursionImpl.FOFormulaBROr(false, Arrays.asList(subsubform1, subsubform2, subsubform3));
			
			FOFormula form = new FOFormulaByRecursionImpl.FOFormulaBRForAll(false, v1, subform1);
			
			Assert.assertEquals("(\\forall _v1)(((_v1 = c1) + (_v1 = c2) + (_v1 = c3)))", sgiser.stringiseFOFormula(form, 100));

			Assert.assertTrue(structure.models(form));			
		}
		
		// Test forall fail
		// (\forall _v1)(_v1 = c1 + _v1 = c2)  
		{
			FOFormula subsubform1 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant1));
			FOFormula subsubform2 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant2));

			FOFormula subform1 = new FOFormulaByRecursionImpl.FOFormulaBROr(false, Arrays.asList(subsubform1, subsubform2));
			
			FOFormula form = new FOFormulaByRecursionImpl.FOFormulaBRForAll(false, v1, subform1);
			
			Assert.assertEquals("(\\forall _v1)(((_v1 = c1) + (_v1 = c2)))", sgiser.stringiseFOFormula(form, 100));

			Assert.assertFalse(structure.models(form));			
		}
		
		// Test "exists".
		// (\exists _v1)(_v1 = c1)  
		{
			FOFormula subform1 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(true, foequals, Arrays.asList(term_var1, term_constant3));

			FOFormula form = new FOFormulaByRecursionImpl.FOFormulaBRForAll(true, v1, subform1);
			
			Assert.assertEquals("¬(\\forall _v1)(¬(_v1 = c3))", sgiser.stringiseFOFormula(form, 100));

			Assert.assertTrue(structure.models(form));			
		}		
	}
}
