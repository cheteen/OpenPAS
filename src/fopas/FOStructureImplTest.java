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
import java.util.LinkedHashSet;
import java.util.Map;

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
import fopas.basics.FOFormula;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOVariable;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOEnumerableSet;

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
	public void testFreeVars() throws FOConstructionException
	{
		FOConstant c1 = new FOConstantImpl("c1");
		FOConstant c2 = new FOConstantImpl("c2");
		FOConstant c3 = new FOConstantImpl("c3");
		
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		FOInteger three = new FOElementImpl.FOIntImpl(3);
		
		FOEnumerableSet<FOInteger> universe = new FOBridgeSet<>("FOURINTS", new HashSet<>(Arrays.asList(one, two, three)), FOInteger.class);
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

		// Test free variable success
		// (_v1 = c1 | _v1 = c2 | _v1 = c3)  
		{
			FOFormula subform1 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant1));
			FOFormula subform2 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant2));
			FOFormula subform3 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant3));

			FOFormula form = new FOFormulaBROr(false, Arrays.asList(subform1, subform2, subform3));
			
			Assert.assertEquals("((_v1 = c1) | (_v1 = c2) | (_v1 = c3))", sgiser.stringiseFormula(form, 100));

			Assert.assertTrue(structure.models(form));			
		}
		
		// Test free variable fail
		// (_v1 = c1 | _v1 = c2)  
		{
			FOFormula subform1 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant1));
			FOFormula subform2 = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_var1, term_constant2));

			FOFormula form = new FOFormulaBROr(false, Arrays.asList(subform1, subform2));
			
			Assert.assertEquals("((_v1 = c1) | (_v1 = c2))", sgiser.stringiseFormula(form, 100));

			Assert.assertFalse(structure.models(form));			
		}
	}
	
	@Test
	public void testFreeVariablePickings() throws FOConstructionException
	{
		FOConstant c0 = new FOConstantImpl("c0");
		FOConstant c1 = new FOConstantImpl("c1");
		FOConstant c2 = new FOConstantImpl("c2");
		FOConstant c3 = new FOConstantImpl("c3");
		
		FOInteger zero = new FOElementImpl.FOIntImpl(0);
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		FOInteger three = new FOElementImpl.FOIntImpl(3);
		
		FOEnumerableSet<FOInteger> universe = new FOBridgeSet<>("FOURINTS", new LinkedHashSet<>(Arrays.asList(zero, one, two, three)), FOInteger.class);
		FORelation<FOElement> foequals = new FORelationOfComparison.FORelationImplEquals();
		
		FOStructure structure = new FOStructureImpl(universe, new HashSet<>(Arrays.asList(foequals)), Collections.emptySet());
		structure.setConstantMapping(c0, zero);
		structure.setConstantMapping(c1, one);
		structure.setConstantMapping(c2, two);
		structure.setConstantMapping(c3, three);
		
		FOTermByRecursionImpl.FOTermConstant term_constant0 = new FOTermByRecursionImpl.FOTermConstant(c0);
		FOTermByRecursionImpl.FOTermConstant term_constant1 = new FOTermByRecursionImpl.FOTermConstant(c1);
		FOTermByRecursionImpl.FOTermConstant term_constant2 = new FOTermByRecursionImpl.FOTermConstant(c2);
		FOTermByRecursionImpl.FOTermConstant term_constant3 = new FOTermByRecursionImpl.FOTermConstant(c3);
		
		FOVariable v1 = new FOVariableImpl("v1");
		FOVariable v2 = new FOVariableImpl("v2");
		FOVariable v3 = new FOVariableImpl("v3");
		FOTermByRecursionImpl.FOTermVariable term_var1 = new FOTermByRecursionImpl.FOTermVariable(v1);
		FOTermByRecursionImpl.FOTermVariable term_var2 = new FOTermByRecursionImpl.FOTermVariable(v2);
		FOTermByRecursionImpl.FOTermVariable term_var3 = new FOTermByRecursionImpl.FOTermVariable(v3);
		
		FOByRecursionStringiser sgiser = new FOByRecursionStringiser();
		
		// Test single variable
		{
			FOTermByRecursionImpl.FOTermFunction term_addition =
					new FOTermByRecursionImpl.FOTermFunction(
							new FOFunctionsInternalInt.FOInternalSumModulus(4), Arrays.asList(term_var1, term_constant0));

			FOFormula form = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_addition, term_constant0));

			Assert.assertEquals("((_v1 + c0) = c0)", sgiser.stringiseFormula(form, 100));

			Assert.assertEquals(4, FluentIterable.from(structure.getAssignments(form)).size());
			Assert.assertEquals(1, FluentIterable.from(structure.getSatisfyingAssignments(form)).size());
		}

		// Test simple addition
		// (_v1 + _v2 + _v3 = c3)
		{
			// (_v1 + _v2 + _v3)
			FOTermByRecursionImpl.FOTermFunction term_addition =
					new FOTermByRecursionImpl.FOTermFunction(
							new FOFunctionsInternalInt.FOInternalSumModulus(4), Arrays.asList(term_var1, term_var2, term_var3));

			FOFormula form = new FOFormulaBRRelation(false, foequals, Arrays.asList(term_addition, term_constant3));
			
			Assert.assertEquals("((_v1 + _v2 + _v3) = c3)", sgiser.stringiseFormula(form, 100));

			Assert.assertFalse(structure.models(form));

			Assert.assertEquals(4 * 4 * 4, FluentIterable.from(structure.getAssignments(form)).size());
			
			Assert.assertEquals(4 * 4 * 1, FluentIterable.from(structure.getSatisfyingAssignments(form)).size());

			// Now let's iterate over satisfying combinations
			//for(Map<FOVariable, FOElement> assignment : structure.getSatisfyingAssignments(form))
			//	System.out.println("" + assignment.get(v1).getElement() + "," + assignment.get(v2).getElement() + "," + assignment.get(v3).getElement());
		}		
	}	
}
