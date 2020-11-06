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
	public void testConstants() throws FORuntimeException {
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
		
		// Test that the same constant is equal to itself.
		{
			FOFormula form = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant1, term_constant1));
			Assert.assertTrue(structure.models(form));
		}
		
		// Test that different constants with different values differ.
		{
			FOFormula form = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant1, term_constant2));
			Assert.assertFalse(structure.models(form));			
		}
		
		// Check that a different constant with the same mapping succeeds in being equal.
		FOConstant c3 = new FOContantImpl("c3");
		structure.setConstantMapping(c3, one);
		FOTermByRecursionImpl.FOTermConstant term_constant3 = new FOTermByRecursionImpl.FOTermConstant(c3);
		{
			FOFormula form = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant1, term_constant3));
			Assert.assertTrue(structure.models(form));			
		}
	}

}
