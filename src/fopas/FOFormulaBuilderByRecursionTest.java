package fopas;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fopas.FOFormulaBuilderByRecursion.FOToken;
import fopas.basics.FOConstant;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FORelation;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOElement.FOInteger;

public class FOFormulaBuilderByRecursionTest {

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
	public void test() throws FOConstructionException
	{
		FOConstant c1 = new FOConstantImpl("c1");
		FOConstant c2 = new FOConstantImpl("c2");
		FOConstant c3 = new FOConstantImpl("c3");
		
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		FOInteger three = new FOElementImpl.FOIntImpl(3);
		
		FOSet<FOElement> universe = new FOBridgeSet<>(new HashSet<>(Arrays.asList(one, two, three)));
		FORelation<FOElement> foequals = new FORelationImpl.FORelationImplEquals();
				
		FOStructure structure = new FOStructureImpl(universe, new HashSet<>(Arrays.asList(foequals)), Collections.emptySet());
		structure.setConstantMapping(c1, one);
		structure.setConstantMapping(c2, two);
		structure.setConstantMapping(c3, three);
		
		FOFormulaBuilderByRecursion builder = new FOFormulaBuilderByRecursion();
		List<FOToken> tokens = builder.parseTokens("(forall _v1)(_v1 = c1 | _v1 = c2 | _v1 = c3)", structure);

		Assert.assertEquals(17, tokens.size());
		List<FOToken.Type> expectedTypes = Arrays.asList(
				FOToken.Type.START_GROUP,
				FOToken.Type.SCOPE_COMMAND,
				FOToken.Type.VARIABLE,
				FOToken.Type.END_GROUP,
				FOToken.Type.START_GROUP,
				FOToken.Type.VARIABLE,
				FOToken.Type.INFIX_RELATION_OP,
				FOToken.Type.CONSTANT,
				FOToken.Type.LOGICAL_OP,
				FOToken.Type.VARIABLE,
				FOToken.Type.INFIX_RELATION_OP,
				FOToken.Type.CONSTANT,
				FOToken.Type.LOGICAL_OP,
				FOToken.Type.VARIABLE,
				FOToken.Type.INFIX_RELATION_OP,
				FOToken.Type.CONSTANT,
				FOToken.Type.END_GROUP				
				);
		
		List<String> expectedValues = Arrays.asList(
				"(",
				"forall",
				"v1",
				")",
				"(",
				"v1",
				"=",
				"c1",
				"|",
				"v1",
				"=",
				"c2",
				"|",
				"v1",
				"=",
				"c3",
				")"				
				);
		
		for(int i = 0; i < tokens.size(); i++)
			Assert.assertEquals(expectedTypes.get(i), tokens.get(i).type);
			
		for(int i = 0; i < tokens.size(); i++)
			Assert.assertEquals(expectedValues.get(i), tokens.get(i).value);
	}

	private void printTokens(List<FOToken> tokens)
	{
		for(int i = 0; i < tokens.size(); i++)
		{
			FOToken token = tokens.get(i);
			//System.out.println("" + i + ") '" + token.value + "' : " + token.type);
			System.out.println("\"" + token.value + "\",");
			//System.out.println("FOToken.Type." + token.type + ",");
		}
	}

}
