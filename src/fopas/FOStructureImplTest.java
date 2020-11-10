package fopas;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fopas.basics.FOConstant;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOVariable;
import fopas.basics.FOElement.FOInteger;

public class FOStructureImplTest {

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
	public void testFreeVars() throws FORuntimeException, FOConstructionException
	{
		FOConstant c1 = new FOConstantImpl("c1");
		FOConstant c2 = new FOConstantImpl("c2");
		FOConstant c3 = new FOConstantImpl("c3");
		
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

		// Test free variable success
		// (_v1 = c1 | _v1 = c2 | _v1 = c3)  
		{
			FOFormula subform1 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant1));
			FOFormula subform2 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant2));
			FOFormula subform3 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant3));

			FOFormula form = new FOFormulaByRecursionImpl.FOFormulaBROr(false, Arrays.asList(subform1, subform2, subform3));
			
			Assert.assertEquals("((_v1 = c1) | (_v1 = c2) | (_v1 = c3))", sgiser.stringiseFOFormula(form, 100));

			Assert.assertTrue(structure.models(form));			
		}
		
		// Test free variable fail
		// (_v1 = c1 | _v1 = c2)  
		{
			FOFormula subform1 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant1));
			FOFormula subform2 = new FOFormulaByRecursionImpl.FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant2));

			FOFormula form = new FOFormulaByRecursionImpl.FOFormulaBROr(false, Arrays.asList(subform1, subform2));
			
			Assert.assertEquals("((_v1 = c1) | (_v1 = c2))", sgiser.stringiseFOFormula(form, 100));

			Assert.assertFalse(structure.models(form));			
		}	
	}
}
