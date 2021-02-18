package fopas;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import fopas.basics.FOConstant;
import fopas.basics.FOElement;
import fopas.basics.FOFunction;
import fopas.basics.FORelation;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOElement.FOInteger;

public class FORelationOfComparisonTest {

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

	static FOStructure createStructureIneq()
	{
		FOConstant c0 = new FOConstantImpl("c0");
		FOConstant c50 = new FOConstantImpl("c50");
		FOConstant c100 = new FOConstantImpl("c100");
		FOConstant c1000 = new FOConstantImpl("c1000");
		
		FOInteger f0 = new FOElementImpl.FOIntImpl(0);
		FOInteger fi50 = new FOElementImpl.FOIntImpl(50);
		FOInteger fi100 = new FOElementImpl.FOIntImpl(100);
		FOInteger fi1000 = new FOElementImpl.FOIntImpl(1000);

		FOSet<FOInteger> universe = new FOSetRangedNaturals(0, 1000);	
		FORelation<FOElement> foequals = new FORelationOfComparison.FORelationImplEquals();
		
		FOFunction funaddmod4 = new FOFunctionsInternalInt.FOInternalSumModulus(4);
		
		FOStructure structure = new FOStructureImpl(universe, new HashSet<>(Arrays.asList(foequals)), new HashSet<>(Arrays.asList(funaddmod4)));
		structure.setConstantMapping(c0, f0);
		structure.setConstantMapping(c1000, fi1000);
		structure.setConstantMapping(c50, fi50);
		structure.setConstantMapping(c100, fi100);
		return structure;
	}
	
	@Test
	public void test()
	{
	}

}
