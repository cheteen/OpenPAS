// OpenPAS
//
// Copyright (c) 2017 Burak Cetin
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// SOFTWARE.

package openpas;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import openpas.basics.Expressions.Expression;
import openpas.basics.Literal;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.PropFactory;
import openpas.basics.Proposition;
import openpas.utils.ArrayIterable;

public abstract class ImplsExpressionsTester_Abstract{
	
	PropFactory mFac;
	
	//TODO: Can we and how do we do test inheritance with JUnit4, if not should switch to JUnit3. Perhaps a class attribute?
	//TODO: Should redesign the tests to use inheritance to change behaviour, and get them to test assumptions and props.
	public interface LiteralCreator
	{
		Literal create(String name, boolean neg);
	}
	public class PropCreator implements LiteralCreator
	{
		PropFactory mFac;
		public PropCreator(PropFactory fac)
		{
			mFac = fac;
		}
		@Override
		public Literal create(String name, boolean neg) {
			return mFac.createProposition(name, neg);
		}
	}
	public class AsmtCreator implements LiteralCreator
	{
		PropFactory mFac;
		public AsmtCreator(PropFactory fac)
		{
			mFac = fac;
		}
		@Override
		public Literal create(String name, boolean neg) {
			return mFac.createAssumption(name, neg, 0.5);
		}
	}
	
	abstract PropFactory createFactory();
	
	@Before
	public void setUp() throws Exception {
		mFac = createFactory();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	// TODO: Need to test that the clone of true/false are the same, and the negations return correct opposites.

	@Test
	public void testCreateTerm()
	{
		Expression<LogicalAnd> trm = mFac.createTerm();
		Assert.assertTrue(trm.addLiteral(mFac.createProposition("a", false)));
		Assert.assertTrue(trm.addLiteral(mFac.createProposition("b", false)));
		
		String str = mFac.getDefaultStringer().stringise(trm);
		Assert.assertEquals("a b", str);
	}
	@Test
	public void testCreateEmptyTerm()
	{
		Expression<LogicalAnd> trm = mFac.createTerm();
		Assert.assertTrue(trm.isTrue());
		Assert.assertFalse(trm.addLiteral(mFac.getTrue()));

		String str = mFac.getDefaultStringer().stringise(trm);
		Assert.assertEquals("True", str);
		Assert.assertEquals(0, trm.getLength());
	}
	@Test
	public void testCreateEmptyTrueTerm()
	{
		Expression<LogicalAnd> trm = mFac.createTerm(new ArrayIterable<Literal>(new Literal[] { mFac.getTrue(), mFac.getTrue()}));
		Assert.assertTrue(trm.isTrue());
		
		String str = mFac.getDefaultStringer().stringise(trm);
		Assert.assertEquals("True", str);
	}	
	@Test
	public void testCreateEmptyTrueTerm2()
	{
		Literal la = mFac.createProposition("a", false);
		Expression<LogicalAnd> trm = mFac.createTerm(new ArrayIterable<Literal>(new Literal[] { mFac.getTrue(), la, mFac.getTrue()}));

		String str = mFac.getDefaultStringer().stringise(trm);
		Assert.assertEquals("a", str);
	}	
	@Test
	public void testCreateLongerTerm()
	{
		Expression<LogicalAnd> trm = mFac.createTerm();
		trm.addLiteral(mFac.createProposition("alfa", false));
		trm.addLiteral(mFac.createProposition("beta", false));
		trm.addLiteral(mFac.createProposition("ceta", false));
		trm.addLiteral(mFac.createProposition("deta", false));
		
		String str = mFac.getDefaultStringer().stringise(trm);
		Assert.assertEquals("alfa beta ceta deta", str);
	}
	@Test
	public void testCreateTermWithRedundants()
	{
		Expression<LogicalAnd> trm = mFac.createTerm();
		
		Literal alfa = mFac.createProposition("alfa", false);
		Literal beta = mFac.createProposition("beta", false);
		Literal ceta = mFac.createProposition("ceta", false);

		trm.addLiteral(alfa);
		trm.addLiteral(beta);
		trm.addLiteral(ceta);

		Assert.assertFalse(trm.addLiteral(alfa));
		Assert.assertFalse(trm.addLiteral(beta));
		
		String str = mFac.getDefaultStringer().stringise(trm);
		Assert.assertEquals("alfa beta ceta", str);
	}
	@Test
	public void testCreateTermWithNegs()
	{
		Expression<LogicalAnd> trm = mFac.createTerm();
		
		trm.addLiteral(mFac.createProposition("a", false));
		Assert.assertTrue(trm.addLiteral(mFac.createProposition("b", true)));

		String str = mFac.getDefaultStringer().stringise(trm);
		Assert.assertEquals("a ¬b", str);
	}
	@Test
	public void testCreateTermWithTrue()
	{
		Expression<LogicalAnd> trm = mFac.createTerm();
		
		trm.addLiteral(mFac.createProposition("a", false));
		trm.addLiteral(mFac.createProposition("b", true));
		Assert.assertFalse(trm.addLiteral(mFac.getTrue())); // No effect, should return false
		Assert.assertTrue(trm.addLiteral(mFac.createProposition("c", true))); // as normal

		String str = mFac.getDefaultStringer().stringise(trm);
		Assert.assertEquals("a ¬b ¬c", str);
	}
	@Test
	public void testCreateTermWithFalse()
	{
		Expression<LogicalAnd> trm = mFac.createTerm();
		
		trm.addLiteral(mFac.createProposition("a", false));
		trm.addLiteral(mFac.createProposition("b", true));
		Assert.assertTrue(trm.addLiteral(mFac.getFalse())); // Changes to False at this point.
		Assert.assertFalse(trm.addLiteral(mFac.createProposition("c", true))); // Already False, no change.

		String str = mFac.getDefaultStringer().stringise(trm);
		Assert.assertEquals("False", str);

		Assert.assertFalse(trm.addLiteral(mFac.getFalse())); // False doesn't change False
	}
	@Test
	public void testCreateTermWithContradiction()
	{
		Expression<LogicalAnd> trm = mFac.createTerm();
		
		Literal la = mFac.createProposition("a", false);
		Literal lna = la.cloneNegated();
		
		trm.addLiteral(la);
		trm.addLiteral(mFac.createProposition("b", true));
		Assert.assertTrue(trm.addLiteral(lna));

		{
			String str = mFac.getDefaultStringer().stringise(trm);
			Assert.assertEquals("False", str);			
		}

		// Quick test that this way of creating false is sane.
		Assert.assertFalse(trm.addLiteral(mFac.createProposition("c", true))); // False at this point.

		{
			String str = mFac.getDefaultStringer().stringise(trm);
			Assert.assertEquals("False", str);			
		}
	}
	@Test
	public void testCreateTermDirectConstruction()
	{
		Literal alfa = mFac.createProposition("alfa", false);
		Literal beta = mFac.createProposition("beta", false);
		Literal ceta = mFac.createProposition("ceta", false);

		Expression<LogicalAnd> trm = mFac.createTerm(new ArrayIterable<Literal>(new Literal[] {alfa, beta, ceta}));		

		String str = mFac.getDefaultStringer().stringise(trm);
		Assert.assertEquals("alfa beta ceta", str);
	}
	@Test
	public void testTermEquality()
	{
		Expression<LogicalAnd> term1 = mFac.createTerm();
		Expression<LogicalAnd> term2 = mFac.createTerm();
		
		// Empty but separately created terms are logically equal even when different objects
		Assert.assertTrue(term1 != term2);
		Assert.assertTrue(term1.equals(term2));
		
		Proposition prop1 = mFac.createProposition("alfa", false);
		Proposition prop2 = mFac.createProposition("beta", false);
		
		// When we add a literal to one only they're not equal any more
		term1.addLiteral(prop1);
		Assert.assertTrue(!term1.equals(term2));
		
		// When we add the same literal to the other term they're equal again
		term2.addLiteral(prop1);
		Assert.assertTrue(term1.equals(term2));
		
		// Break again
		term2.addLiteral(prop2);
		Assert.assertTrue(!term1.equals(term2));
	}

	@Test
	public void testCreateClause()
	{
		Expression<LogicalOr> cla = mFac.createClause();
		Assert.assertTrue(cla.addLiteral(mFac.createProposition("a", false)));
		Assert.assertTrue(cla.addLiteral(mFac.createProposition("b", false)));
		
		String str = mFac.getDefaultStringer().stringise(cla);
		Assert.assertEquals("(a + b)", str);
	}
	@Test
	public void testCreateEmptyClause()
	{
		Expression<LogicalOr> cla = mFac.createClause();
		Assert.assertTrue(cla.isFalse());
		Assert.assertFalse(cla.addLiteral(mFac.getFalse()));

		String str = mFac.getDefaultStringer().stringise(cla);
		Assert.assertEquals("(False)", str);
		Assert.assertEquals(0, cla.getLength());
	}
	@Test
	public void testCreateEmptyFalseClause()
	{
		Expression<LogicalOr> cla = mFac.createClause(new ArrayIterable<Literal>(new Literal[] {mFac.getFalse(), mFac.getFalse()}));
		Assert.assertTrue(cla.isFalse());

		String str = mFac.getDefaultStringer().stringise(cla);
		Assert.assertEquals("(False)", str);
	}	
	@Test
	public void testCreateEmptyFalseClause2()
	{
		Literal la = mFac.createProposition("a", false);
		Expression<LogicalOr> cla = mFac.createClause(new ArrayIterable<Literal>(new Literal[] {mFac.getFalse(), la, mFac.getFalse()}));

		String str = mFac.getDefaultStringer().stringise(cla);
		Assert.assertEquals("(a)", str);
	}	
	@Test
	public void testCreateLongerClause()
	{
		Expression<LogicalOr> cla = mFac.createClause();
		cla.addLiteral(mFac.createProposition("alfa", false));
		cla.addLiteral(mFac.createProposition("beta", false));
		cla.addLiteral(mFac.createProposition("ceta", false));
		cla.addLiteral(mFac.createProposition("deta", false));
		
		String str = mFac.getDefaultStringer().stringise(cla);
		Assert.assertEquals("(alfa + beta + ceta + deta)", str);
	}
	@Test
	public void testCreateClauseWithRedundants()
	{
		Expression<LogicalOr> cla = mFac.createClause();
		
		Literal alfa = mFac.createProposition("alfa", false);
		Literal beta = mFac.createProposition("beta", false);
		Literal ceta = mFac.createProposition("ceta", false);

		cla.addLiteral(alfa);
		cla.addLiteral(beta);
		cla.addLiteral(ceta);

		Assert.assertFalse(cla.addLiteral(alfa));
		Assert.assertFalse(cla.addLiteral(beta));
		
		String str = mFac.getDefaultStringer().stringise(cla);
		Assert.assertEquals("(alfa + beta + ceta)", str);
	}
	@Test
	public void testCreateClauseWithNegs()
	{
		Expression<LogicalOr> cla = mFac.createClause();
		
		cla.addLiteral(mFac.createProposition("a", false));
		Assert.assertTrue(cla.addLiteral(mFac.createProposition("b", true)));

		String str = mFac.getDefaultStringer().stringise(cla);
		Assert.assertEquals("(a + ¬b)", str);
	}
	@Test
	public void testCreateClauseWithTrue()
	{
		Expression<LogicalOr> cla = mFac.createClause();
		
		cla.addLiteral(mFac.createProposition("a", false));
		cla.addLiteral(mFac.createProposition("b", true));
		Assert.assertTrue(cla.addLiteral(mFac.getTrue()));
		cla.addLiteral(mFac.createProposition("c", true));

		String str = mFac.getDefaultStringer().stringise(cla);
		Assert.assertEquals("(True)", str);

		Assert.assertFalse(cla.addLiteral(mFac.getTrue()));
	}
	@Test
	public void testCreateClauseWithFalse()
	{
		Expression<LogicalOr> cla = mFac.createClause();
		
		cla.addLiteral(mFac.createProposition("a", false));
		cla.addLiteral(mFac.createProposition("b", true));
		Assert.assertFalse(cla.addLiteral(mFac.getFalse()));
		cla.addLiteral(mFac.createProposition("c", true));

		String str = mFac.getDefaultStringer().stringise(cla);
		Assert.assertEquals("(a + ¬b + ¬c)", str);
	}
	@Test
	public void testCreateClauseWithContradiction()
	{
		Expression<LogicalOr> cla = mFac.createClause();
		
		Literal la = mFac.createProposition("a", false);
		Literal lna = la.cloneNegated();
		
		cla.addLiteral(la);
		cla.addLiteral(mFac.createProposition("b", true));
		Assert.assertTrue(cla.addLiteral(lna));
		Assert.assertFalse(cla.addLiteral(mFac.createProposition("c", true))); 

		String str = mFac.getDefaultStringer().stringise(cla);
		Assert.assertEquals("(True)", str);
	}
	@Test
	public void testCreateClauseWithContradiction2()
	{
		Expression<LogicalOr> cla = mFac.createClause();
		
		Literal la = mFac.createProposition("a", false);
		Literal lna = la.cloneNegated();
		
		cla.addLiteral(la);
		cla.addLiteral(mFac.createProposition("b", true));
		Assert.assertTrue(cla.addLiteral(lna));
		// Clause is True at this point.
		Assert.assertFalse(cla.addLiteral(mFac.createProposition("c", true))); 
		Assert.assertFalse(cla.addLiteral(la));

		String str = mFac.getDefaultStringer().stringise(cla);
		Assert.assertEquals("(True)", str);
	}
	@Test
	public void testCreateClauseDirectConstruction()
	{
		Literal alfa = mFac.createProposition("alfa", false);
		Literal beta = mFac.createProposition("beta", false);
		Literal ceta = mFac.createProposition("ceta", false);

		Expression<LogicalOr> cla = mFac.createClause(new ArrayIterable<Literal>(new Literal[] {alfa, beta, ceta}));		

		String str = mFac.getDefaultStringer().stringise(cla);
		Assert.assertEquals("(alfa + beta + ceta)", str);
	}

	@Test
	public void testClauseEquality()
	{
		Expression<LogicalOr> clause1 = mFac.createClause();
		Expression<LogicalOr> clause2 = mFac.createClause();
		
		// Empty but separately created terms are logically equal even when different objects
		Assert.assertTrue(clause1 != clause2);
		Assert.assertTrue(clause1.equals(clause2));
		
		Proposition prop1 = mFac.createProposition("alfa", false);
		Proposition prop2 = mFac.createProposition("beta", false);
		
		// When we add a literal to one only they're not equal any more
		clause1.addLiteral(prop1);
		Assert.assertTrue(!clause1.equals(clause2));
		
		// When we add the same literal to the other term they're equal again
		clause2.addLiteral(prop1);
		Assert.assertTrue(clause1.equals(clause2));
		
		// Break again
		clause2.addLiteral(prop2);
		Assert.assertTrue(!clause1.equals(clause2));
	}
}
