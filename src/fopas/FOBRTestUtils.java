package fopas;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.Assert;

import fopas.FOElementImpl.FOIntImpl;
import fopas.FOFormulaBuilderByRecursion.FOToken;
import fopas.FOFunctionsInternalInt.FOInternalSumModulus;
import fopas.FORelationImpl.FORelationImplEquals;
import fopas.basics.FOConstant;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOFormula;
import fopas.basics.FOFunction;
import fopas.basics.FORelation;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;

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
		
		FOSet<FOElement> universe = new FOBridgeSet<>("FOURINTS", new LinkedHashSet<>(Arrays.asList(zero, one, two, three)));		
		FORelation<FOElement> foequals = new FORelationImpl.FORelationImplEquals();
		
		FOFunction funaddmod4 = new FOFunctionsInternalInt.FOInternalSumModulus(4);
		
		FOStructure structure = new FOStructureImpl(new FOUnionSetImpl(universe), new HashSet<>(Arrays.asList(foequals)), new HashSet<>(Arrays.asList(funaddmod4)));
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
		
		String strReForm = sgiser.stringiseFOFormula(form, 100, useExtended);
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
			builder.buildFormula(strFormula, structure);
		} catch (FOConstructionException e)
		{
			Assert.assertTrue(e.toString().contains(expContains));
			return;
		}
		Assert.fail("Expected exception not found.");
	}

}
