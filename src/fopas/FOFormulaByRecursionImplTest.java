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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.FluentIterable;

import fopas.basics.FOConstant;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOEnumerableSet;
import fopas.basics.FOFormula;
import fopas.basics.FOFunction;
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
	public void testConstants() throws FOConstructionException
	{
		FOConstant c1 = new FOConstantImpl("c1");
		FOConstant c2 = new FOConstantImpl("c2");
		
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		
		FOEnumerableSet<? extends FOElement> universe = new FOBridgeSet<>("TWOINTS", new HashSet<>(Arrays.asList(one, two)), FOInteger.class);
		
		FORelation<FOElement> foequals = new FORelationOfComparison.FORelationImplEquals();
		
		FOStructure structure = new FOStructureImpl(universe, new HashSet<>(Arrays.asList(foequals)), Collections.emptySet());
		structure.setConstantMapping(c1, one);
		structure.setConstantMapping(c2, two);
		
		FOTermByRecursionImpl.FOTermConstant term_constant1 = new FOTermByRecursionImpl.FOTermConstant(c1);
		FOTermByRecursionImpl.FOTermConstant term_constant2 = new FOTermByRecursionImpl.FOTermConstant(c2);

		FOByRecursionStringiser sgiser = new FOByRecursionStringiser();

		// Test that the same constant is equal to itself.
		{
			FOFormula form = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant1, term_constant1));
			Assert.assertTrue(structure.models(form));
			
			Assert.assertEquals("(c1 = c1)", sgiser.stringiseFormula(form, 100));
		}
		
		// Test that different constants with different values differ.
		{
			FOFormula form = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant1, term_constant2));
			Assert.assertFalse(structure.models(form));			

			Assert.assertEquals("(c1 = c2)", sgiser.stringiseFormula(form, 100));
		}
		
		// Check that a different constant with the same mapping succeeds in being equal.
		FOConstant c3 = new FOConstantImpl("c3");
		structure.setConstantMapping(c3, one);
		FOTermByRecursionImpl.FOTermConstant term_constant3 = new FOTermByRecursionImpl.FOTermConstant(c3);
		{
			FOFormula form = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant1, term_constant3));
			Assert.assertTrue(structure.models(form));			

			Assert.assertEquals("(c1 = c3)", sgiser.stringiseFormula(form, 100));
		}

		// Test that negation works.
		{
			FOFormula form = new FOFormulaBRRelation(true, foequals, Arrays.asList(term_constant1, term_constant1));
			Assert.assertFalse(structure.models(form));
			
			Assert.assertEquals("¬(c1 = c1)", sgiser.stringiseFormula(form, 100));
		}
	}

	@Test
	public void testOrFormulas() throws FOConstructionException
	{
		FOConstant c1 = new FOConstantImpl("c1");
		FOConstant c2 = new FOConstantImpl("c2");
		FOConstant c3 = new FOConstantImpl("c3");
		
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		FOInteger three = new FOElementImpl.FOIntImpl(3);
		
		FOEnumerableSet<? extends FOElement> universe = new FOBridgeSet<>("THREEINTS", new HashSet<>(Arrays.asList(one, two, three)), FOInteger.class);		
		FORelation<FOElement> foequals = new FORelationOfComparison.FORelationImplEquals();
		
		FOStructure structure = new FOStructureImpl(universe, new HashSet<>(Arrays.asList(foequals)), Collections.emptySet());
		structure.setConstantMapping(c1, one);
		structure.setConstantMapping(c2, two);
		structure.setConstantMapping(c3, three);
		
		FOTermByRecursionImpl.FOTermConstant term_constant1 = new FOTermByRecursionImpl.FOTermConstant(c1);
		FOTermByRecursionImpl.FOTermConstant term_constant2 = new FOTermByRecursionImpl.FOTermConstant(c2);
		FOTermByRecursionImpl.FOTermConstant term_constant3 = new FOTermByRecursionImpl.FOTermConstant(c3);

		FOByRecursionStringiser sgiser = new FOByRecursionStringiser();

		// Test Or fomula satisfies.
		{
			FOFormula subform1 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant1, term_constant2));
			FOFormula subform2 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant2, term_constant3));
			FOFormula subform3 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant3, term_constant3));

			FOFormula form = new FOFormulaBROr(false, Arrays.asList(subform1, subform2, subform3));
			Assert.assertEquals("((c1 = c2) | (c2 = c3) | (c3 = c3))", sgiser.stringiseFormula(form, 100));

			Assert.assertTrue(structure.models(form));			
		}

		// Test Or formula fails.
		{
			FOFormula subform1 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant1, term_constant2));
			FOFormula subform2 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant2, term_constant3));
			FOFormula subform3 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant3, term_constant1));

			FOFormula form = new FOFormulaBROr(false, Arrays.asList(subform1, subform2, subform3));
			Assert.assertEquals("((c1 = c2) | (c2 = c3) | (c3 = c1))", sgiser.stringiseFormula(form, 100));

			Assert.assertFalse(structure.models(form));			
		}
	}

	@Test
	public void testForAllFormulas() throws FOConstructionException
	{
		FOConstant c1 = new FOConstantImpl("c1");
		FOConstant c2 = new FOConstantImpl("c2");
		FOConstant c3 = new FOConstantImpl("c3");
		
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		FOInteger three = new FOElementImpl.FOIntImpl(3);
		
		FOEnumerableSet<? extends FOElement> universe = new FOBridgeSet<>("THREEINTS", new HashSet<>(Arrays.asList(one, two, three)), FOInteger.class);
		FORelation<FOElement> foequals = new FORelationOfComparison.FORelationImplEquals();
				
		FOStructure structure = new FOStructureImpl(universe, new HashSet<>(Arrays.asList(foequals)), Collections.emptySet());
		structure.setConstantMapping(c1, one);
		structure.setConstantMapping(c2, two);
		structure.setConstantMapping(c3, three);
		
		FOTermByRecursionImpl.FOTermConstant term_constant1 = new FOTermByRecursionImpl.FOTermConstant(c1);
		FOTermByRecursionImpl.FOTermConstant term_constant2 = new FOTermByRecursionImpl.FOTermConstant(c2);
		FOTermByRecursionImpl.FOTermConstant term_constant3 = new FOTermByRecursionImpl.FOTermConstant(c3);

		FOByRecursionStringiser sgiser = new FOByRecursionStringiser();

		FOVariable v1 = new FOVariableImpl("v1");
		FOTermByRecursionImpl.FOTermVariable term_var1 = new FOTermByRecursionImpl.FOTermVariable(v1);

		// Test forall success
		// (forall _v1)(_v1 = c1 | _v1 = c2 | _v1 = c3)  
		{
			FOFormula subsubform1 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant1));
			FOFormula subsubform2 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant2));
			FOFormula subsubform3 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant3));

			FOFormulaBRImpl subform1 = new FOFormulaBROr(false, Arrays.asList(subsubform1, subsubform2, subsubform3));
			
			FOFormulaBRImpl form = new FOFormulaBRForAll(false, v1, subform1);
			
			Assert.assertEquals("(forall _v1)((_v1 = c1) | (_v1 = c2) | (_v1 = c3))", sgiser.stringiseFormula(form, 100));

			Assert.assertTrue(structure.models(form));			
		}
		
		// Test forall fail
		// (forall _v1)(_v1 = c1 | _v1 = c2)  
		{
			FOFormula subsubform1 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant1));
			FOFormula subsubform2 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant2));

			FOFormulaBRImpl subform1 = new FOFormulaBROr(false, Arrays.asList(subsubform1, subsubform2));
			
			FOFormulaBRImpl form = new FOFormulaBRForAll(false, v1, subform1);
			
			Assert.assertEquals("(forall _v1)((_v1 = c1) | (_v1 = c2))", sgiser.stringiseFormula(form, 100));

			Assert.assertFalse(structure.models(form));			
		}
		
		// Test "exists".
		// (exists _v1)(_v1 = c1)  
		{
			FOFormulaBRImpl subform1 = new FOFormulaBRRelation(true, foequals, Arrays.asList(term_var1, term_constant3));

			FOFormulaBRImpl form = new FOFormulaBRForAll(true, v1, subform1);
			
			Assert.assertEquals("¬(forall _v1)¬(_v1 = c3)", sgiser.stringiseFormula(form, 100));

			Assert.assertTrue(structure.models(form));			
		}		
	}
	
	@Test
	public void testFunctions() throws FOConstructionException
	{
		FOConstant c0 = new FOConstantImpl("c0");
		FOConstant c1 = new FOConstantImpl("c1");
		FOConstant c2 = new FOConstantImpl("c2");
		FOConstant c3 = new FOConstantImpl("c3");
		
		FOInteger zero = new FOElementImpl.FOIntImpl(0);
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		FOInteger three = new FOElementImpl.FOIntImpl(3);
		
		FOEnumerableSet<? extends FOElement> universe = new FOBridgeSet<>("THREEINTS", new HashSet<>(Arrays.asList(zero, one, two, three)), FOInteger.class);
		FORelation<FOElement> foequals = new FORelationOfComparison.FORelationImplEquals();
		FOFunction funaddmod4 = new FOFunctionsInternalInt.FOInternalSumModulus(4);

		FOStructure structure = new FOStructureImpl(universe, new HashSet<>(Arrays.asList(foequals)), new HashSet<>(Arrays.asList(funaddmod4)));
		structure.setConstantMapping(c0, zero);
		structure.setConstantMapping(c1, one);
		structure.setConstantMapping(c2, two);
		structure.setConstantMapping(c3, three);
		
		FOTermByRecursionImpl.FOTermConstant term_constant0 = new FOTermByRecursionImpl.FOTermConstant(c0);
		FOTermByRecursionImpl.FOTermConstant term_constant1 = new FOTermByRecursionImpl.FOTermConstant(c1);
		FOTermByRecursionImpl.FOTermConstant term_constant2 = new FOTermByRecursionImpl.FOTermConstant(c2);
		FOTermByRecursionImpl.FOTermConstant term_constant3 = new FOTermByRecursionImpl.FOTermConstant(c3);

		FOByRecursionStringiser sgiser = new FOByRecursionStringiser();

		// Test simple addition
		// c3 = (c1 + c2)
		{
			// (1 + 2)
			FOTermByRecursionImpl.FOTermFunction term_addition =
					new FOTermByRecursionImpl.FOTermFunction(funaddmod4, Arrays.asList(term_constant1, term_constant2));

			FOFormula form = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_constant3, term_addition));
			
			Assert.assertEquals("(c3 = (c1 + c2))", sgiser.stringiseFormula(form, 100));

			Assert.assertTrue(structure.models(form));			
		}
		
		FOVariable v1 = new FOVariableImpl("v1");
		FOTermByRecursionImpl.FOTermVariable term_var1 = new FOTermByRecursionImpl.FOTermVariable(v1);

		// Now let's test something meatier:
		// (forall _v1)((1 + _v1 + 1) = _v1 + 2)  
		{
			FOTermByRecursionImpl.FOTermFunction term_addition1 =
					new FOTermByRecursionImpl.FOTermFunction(funaddmod4, Arrays.asList(term_constant1, term_var1, term_constant1));

			FOTermByRecursionImpl.FOTermFunction term_addition2 =
					new FOTermByRecursionImpl.FOTermFunction(funaddmod4, Arrays.asList(term_var1, term_constant2));
			
			FOFormulaBRImpl subform = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_addition1, term_addition2));

			FOFormulaBRImpl form = new FOFormulaBRForAll(false, v1, subform);
			
			Assert.assertEquals("(forall _v1)((c1 + _v1 + c1) = (_v1 + c2))", sgiser.stringiseFormula(form, 100));

			Assert.assertTrue(structure.models(form));			
		}		
	}
}
