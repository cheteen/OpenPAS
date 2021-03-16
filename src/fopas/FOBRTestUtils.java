package fopas;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.junit.Assert;

import fopas.FOElementImpl.FOIntImpl;
import fopas.FOFormulaBuilderByRecursion.FOToken;
import fopas.FOFunctionsInternalInt.FOInternalSumModulus;
import fopas.FORelationOfComparison.FORelationImplEquals;
import fopas.basics.FOConstant;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOFormula;
import fopas.basics.FOFunction;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOVariable;

class FOBRTestUtils {

	static FOStructure createSimpleStructure4Ints()
	{
		FOConstant c0 = new FOConstantImpl("c0");
		FOConstant c1 = new FOConstantImpl("c1");
		FOConstant c2 = new FOConstantImpl("c2");
		FOConstant c3 = new FOConstantImpl("c3");
		
		FOInteger zero = new FOElementImpl.FOIntImpl(0);
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		FOInteger three = new FOElementImpl.FOIntImpl(3);
		
		FOSet<? extends FOElement> universe = new FOBridgeSet<>("FOURINTS", new LinkedHashSet<>(Arrays.asList(zero, one, two, three)), FOInteger.class);		
		FORelation<FOElement> foequals = new FORelationOfComparison.FORelationImplEquals();
		
		FOFunction funaddmod4 = new FOFunctionsInternalInt.FOInternalSumModulus(4);
		
		FOStructure structure = new FOStructureImpl(universe, new HashSet<>(Arrays.asList(foequals)), new HashSet<>(Arrays.asList(funaddmod4)));
		structure.setConstantMapping(c0, zero);
		structure.setConstantMapping(c1, one);
		structure.setConstantMapping(c2, two);
		structure.setConstantMapping(c3, three);
		return structure;
	}

	static void printTokens(List<FOToken> tokens)
	{
		for(int i = 0; i < tokens.size(); i++)
		{
			FOToken token = tokens.get(i);
			//System.out.println("" + i + ") '" + token.value + "' : " + token.type);
			System.out.println("\"" + token.value + "\",");
			//System.out.println("FOToken.Type." + token.type + ",");
		}
	}

	static void testFormula(FOFormulaBuilderByRecursion builder, FOByRecursionStringiser sgiser, FOStructure structure, String strFormula,
			boolean expectSatisfaction, String format) throws FOConstructionException
	{
		FOBRTestUtils.testFormula(builder, sgiser, structure, strFormula, expectSatisfaction, format, true);
	}

	static void testFormula(FOFormulaBuilderByRecursion builder, FOByRecursionStringiser sgiser, FOStructure structure, String strFormula,
			boolean expectSatisfaction, String format, boolean useExtended) throws FOConstructionException
	{
		FOFormula form = builder.buildFormula(strFormula, structure);
		
		String strReForm = sgiser.stringiseFormula(form, 200, useExtended);
		if(format == null)
			Assert.assertEquals(strFormula, strReForm);
		else
		{
			String strReFormReformat = String.format(format, strFormula);
			Assert.assertEquals(strReFormReformat, strReForm);
		}			
		
		Assert.assertEquals(expectSatisfaction, structure.models(form));
	}

	static void testThrows(FOFormulaBuilderByRecursion builder, FOByRecursionStringiser sgiser, FOStructure structure, String strFormula, String expContains)
	{
		try
		{
			FOFormula form = builder.buildFormula(strFormula, structure);
			form.models(structure);
		} catch (Throwable e)
		{
			Assert.assertTrue(e.toString().contains(expContains));
			return;
		}
		Assert.fail("Expected exception not found.");
	}

	static void printAssignments(FOStructure structure, FOFormulaBRImpl form, boolean satisfying) throws FOConstructionException 
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

	static void assertThrows(Runnable r, String containsMsg)
	{
		String exceptionCaught = null;
		try
		{
			r.run();
		}
		catch(FORuntimeException ex)
		{
			exceptionCaught = ex.getMessage();
		}
		assertFalse("Expected exception not found.", exceptionCaught == null);
		assertTrue(exceptionCaught.contains(containsMsg));
	}
}
