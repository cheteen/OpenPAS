package fopas;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
import fopas.basics.FOFunction;
import fopas.basics.FORelation;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOFormula;

public class FOFormulaBuilderByRecursionTest {
	
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

	@Test
	public void testTokens() throws FOConstructionException
	{
		FOConstant c1 = new FOConstantImpl("c1");
		FOConstant c2 = new FOConstantImpl("c2");
		FOConstant c3 = new FOConstantImpl("c3");
		
		FOInteger one = new FOElementImpl.FOIntImpl(1);
		FOInteger two = new FOElementImpl.FOIntImpl(2);
		FOInteger three = new FOElementImpl.FOIntImpl(3);
		
		FOSet<FOElement> universe = new FOBridgeSet<>("FOURINTS", new HashSet<>(Arrays.asList(one, two, three)));
		FORelation<FOElement> foequals = new FORelationOfComparison.FORelationImplEquals();
				
		FOStructure structure = new FOStructureImpl(new FOUnionSetImpl(universe), new HashSet<>(Arrays.asList(foequals)), Collections.emptySet());
		structure.setConstantMapping(c1, one);
		structure.setConstantMapping(c2, two);
		structure.setConstantMapping(c3, three);
		
		FOFormulaBuilderByRecursion builder = new FOFormulaBuilderByRecursion();

		Map<String, FORelation<FOElement>> mapRels = new HashMap<>();
		Map<String, FORelation<FOElement>> mapInfixRels = new HashMap<>();		
		Map<String, FOFunction> mapFuns = new HashMap<>();
		Map<String, FOFunction> mapInfixFuns = new HashMap<>();
		Map<String, FOConstant> mapConstants = new HashMap<>();
		Map<String, FOFormula> mapAliases = new HashMap<>();
		builder.buildMaps(structure, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases);
		
		List<FOToken> tokens = builder.parseTokens("(forall _v1)(_v1 = c1 | _v1 = c2 | _v1 = c3)",
				structure, mapRels, mapInfixRels, mapFuns, mapInfixFuns, mapConstants, mapAliases);

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

	private void testFormula(FOStructure structure, String strFormula,
			boolean expectSatisfaction, String format) throws FOConstructionException
	{
		FOBRTestUtils.testFormula(builder, sgiser, structure, strFormula, expectSatisfaction, format);
	}
	private void testFormula(FOStructure structure, String strFormula,
			boolean expectSatisfaction, String format, boolean useExtended) throws FOConstructionException
	{
		FOBRTestUtils.testFormula(builder, sgiser, structure, strFormula, expectSatisfaction, format, useExtended);
	}
	private void testThrows(FOStructure structure, String strFormula, String expContains)
	{
		FOBRTestUtils.testThrows(builder, sgiser, structure, strFormula, expContains);
	}

	@Test
	public void testBuildSimpleFormulas() throws FOConstructionException
	{
		FOStructure structure = FOBRTestUtils.createSimpleStructure4Ints();
		
		testFormula(structure, "c0 = c1", false, "(%s)");
		testFormula(structure, "(c0 = c1)", false, null);
		testFormula(structure, "  c0  =   c1 ", false, "(c0 = c1)");
		testFormula(structure, "¬(c1 = c1)", false, null);
		
		testFormula(structure, "c3 = (c1 + c2)", true, "(%s)");
		testFormula(structure, "c3 = c1 + c2", true, "(c3 = (c1 + c2))");
		testFormula(structure, "c0 = (c1 + c1 + c1 + c1)", true, "(%s)");
		
		testFormula(structure, "c3 = sum(c1, c2)", true, "(%s)");
	}

	
	@Test
	public void testBuildSimpleOrFormulas() throws FOConstructionException
	{
		FOStructure structure = FOBRTestUtils.createSimpleStructure4Ints();
		
		testFormula(structure, "(c0 = c1) | (c1 = c1)", true, "(%s)");
		testFormula(structure, "(c0 = c1) | ¬(c1 = c1)", false, "(%s)");
		testFormula(structure, "(c0 = c1) | (c1 = c1)", true, "(%s)");
		testFormula(structure, "(  ( c0 = c1) | (c1 = c1))", true, "((c0 = c1) | (c1 = c1))");
		testFormula(structure, "(c0 = c1) | (c1 = c2) | (c2 = c3)", false, "(%s)");
		testFormula(structure, "(c0 = c1) | (c1 = c2) | (c2 = c3) | (c0 = c0)", true, "(%s)");
	}
	
	@Test
	public void testBuildForAllFormulas() throws FOConstructionException
	{
		FOStructure structure = FOBRTestUtils.createSimpleStructure4Ints();

		testFormula(structure, "(forall _v1)(_v1 = c1)", false, null);
		testFormula(structure, "(forall _v1)(c1 = c1)", true, null);
		testFormula(structure, "¬(forall _v1)(c1 = c1)", false, null);
		testThrows(structure, "(forall _v1) c1 = c1", "Expected scoped formula not found");
		testFormula(structure, "(forall _v1)(c1 = c2) | (forall _v1)(c1 = c2)", false, "(%s)");
		testFormula(structure, "(forall _v1)(c1 = c2) | (forall _v1)(c1 = c1)", true, "(%s)");
		testFormula(structure, "(forall _v1)((_v1 = c0) | (_v1 = c1) | (_v1 = c2) | (_v1 = c3))", true, null);
		
		// Negation of forall scope affects the whole formula.
		testFormula(structure, "¬(forall _v1)¬(_v1 = c1)", true, null);
		// Double negation of forall directly via surrounding parenthesis is swallowed.
		testFormula(structure, "¬(¬(forall _v1)¬(c0 = c1))", true, "(forall _v1)¬(c0 = c1)");
		
		// Test multiple scopes
		testFormula(structure, "(forall _v1)(forall _v2)(forall _v3)(c1 = c1)", true, null);
		testThrows(structure, "(forall _v1)(forall _v1)(c1 = c1)", "Variable name collision");
		// "(exists _v1)(_exists _v2)(_v1 = _v2)"
		// This follows from: (exists v1, v2, ...)(L) :- ¬(forall v1, v2, ...) (¬L)
		// is "¬(forall _v1)¬(¬forall(¬(_v1 = _v2)))"
		// Hence it becomes: "¬((forall _v1)(forall _v2)¬(_v1 = _v2))"
		testFormula(structure, "¬(forall _v1)(forall _v2)¬(_v1 = _v2)", true, null);
	}
	
	@Test
	public void testBuildAndFormulas() throws FOConstructionException
	{
		FOStructure structure = FOBRTestUtils.createSimpleStructure4Ints();

		testFormula(structure, "(c0 = c0) & (c1 = c1)", true, "¬(¬(c0 = c0) | ¬(c1 = c1))", false);
		testFormula(structure, "(c0 = c0) & (c0 = c1)", false, "(%s)");
		testFormula(structure, "(c0 = c0) & ¬(c1 = c1)", false, "(%s)");
		testFormula(structure, "(c0 = c1) & (c1 = c1)", false, "(%s)");
		testFormula(structure, "  (  c0  =  c1  )   &   (  c1  =   c1 )  ", false, "((c0 = c1) & (c1 = c1))");
		testFormula(structure, "(c0 = c0) & (c1 = c1) & (c2 = c2)", true, "(%s)");
		testFormula(structure, "(c0 = c0) & (c1 = c1) & (c2 = c2) & (c1 = c2)", false, "(%s)");		
	}

	@Test
	public void testBuildAndOrFormulas() throws FOConstructionException
	{
		FOStructure structure = FOBRTestUtils.createSimpleStructure4Ints();
		
		testFormula(structure, "(c0 = c0) & (c1 = c1) | (c0 = c1)", true, "(¬(¬(c0 = c0) | ¬(c1 = c1)) | (c0 = c1))", false);
		testFormula(structure, "(c0 = c0) & (c1 = c1) | (c0 = c1)", true, "(((c0 = c0) & (c1 = c1)) | (c0 = c1))");
		testFormula(structure, "c0 = c0 & c1 = c1 | c0 = c1", true, "(((c0 = c0) & (c1 = c1)) | (c0 = c1))");

		testFormula(structure, "¬(forall _v1)¬(_v1 = c1)", true, null);		
		testFormula(structure, "c0 = c0 & c1 = c1 | ¬(forall _v1)¬(_v1 = c1)", true, "(((c0 = c0) & (c1 = c1)) | ¬(forall _v1)¬(_v1 = c1))");
		testFormula(structure, "¬(forall _v1)(¬(_v1 = c1) & ¬(_v1 = c2))", true, null);
		// TODO: Need more tests like the following that tests multiple negations to exercise eliminateTrue properly (e.g. across aliases etc).
		testFormula(structure, "¬(forall _v1)¬(¬(_v1 = c1) | (c1 = c0))", true, null);
	}

	@Test
	public void testBuildAndOrForAllFormulas() throws FOConstructionException
	{
		FOStructure structure = FOBRTestUtils.createSimpleStructure4Ints();
		
		testFormula(structure, "c0 = c1 & c1 = c0 | ¬(forall _v1)¬(_v1 = c1)", true, "(((c0 = c1) & (c1 = c0)) | ¬(forall _v1)¬(_v1 = c1))");
	}

	@Test
	public void testBuildCannedFormulas() throws FOConstructionException
	{
		FOStructure structure = new FOStructureImpl(new FOBridgeSet<>("dummy", new HashSet<>(0)), new HashSet<>(0), new HashSet<>(0));

		{
			FOFormula form = builder.buildTautology();
			Assert.assertTrue(form.models(structure));			
		}
		
		{
			FOFormula form = builder.buildContradiction();
			Assert.assertFalse(form.models(structure));			
		}
	}

	@Test
	public void testBuildImplication() throws FOConstructionException
	{
		FOStructure structure = FOBRTestUtils.createSimpleStructure4Ints();
		
		testFormula(structure, "_v = c0 -> (_v + c1) = c1", true, "(¬(_v = c0) | ((_v + c1) = c1))", false);
		testFormula(structure, "_v = c0 -> (_v + c1) = c1", true, "((_v = c0) -> ((_v + c1) = c1))");
		testFormula(structure, "c0 = c0 & c1 = c0 | c0 = c1 -> c1 = c1", true, "((((c0 = c0) & (c1 = c0)) | (c0 = c1)) -> (c1 = c1))");
	}
	
	@Test
	public void testLogicalFunctionalFormulas() throws FOConstructionException
	{
		FOStructure structure = FOBRTestUtils.createSimpleStructure4Ints();
		
		testFormula(structure, "c1 + c0 = c1 & c1 + c2 = c3 | c1 = c2", true, "((((c1 + c0) = c1) & ((c1 + c2) = c3)) | (c1 = c2))");
		testFormula(structure, "c1 + c0 = c0 & c1 + c2 = c3 | c1 = c1", true, "((((c1 + c0) = c0) & ((c1 + c2) = c3)) | (c1 = c1))");
		testFormula(structure, "c1 + c0 = c0 & (c1 + c2 = c3 | c1 = c1)", false, "(((c1 + c0) = c0) & (((c1 + c2) = c3) | (c1 = c1)))");
	}
	

	@Test
	public void testExistsFormulas() throws FOConstructionException
	{
		FOStructure structure = FOBRTestUtils.createSimpleStructure4Ints();
		
		testFormula(structure, "(exists _v1)(_v1 = c1)", true, "¬(forall _v1)¬(_v1 = c1)", false);
		testFormula(structure, "(exists _v1)(exists _v2)(_v1 = _v2)", true, "¬(forall _v1)(forall _v2)¬(_v1 = _v2)", false);

		testFormula(structure, "(exists _v1)(_v1 = _c1)", true, null);
		testFormula(structure, "(exists _v1)(exists _v2)(_v1 = _v2)", true, null);

		testFormula(structure, "¬(exists _v1)(c1 = c0)", true, null);
		testFormula(structure, "(exists _v1)(_v1 = _v2)", true, "¬(forall _v1)¬(_v1 = _v2)", false);
	}
}
