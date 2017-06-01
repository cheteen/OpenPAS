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

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.FluentIterable;

import openpas.StringOps.LogicalStringer;
import openpas.basics.Assumption;
import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.NumericResolver;
import openpas.basics.PAS;
import openpas.basics.PAS.KBException;
import openpas.basics.PropFactory;
import openpas.basics.Proposition;

public class PASImplTester {
	
	final static boolean VERBOSE = false;
	final static double DOUBLE_COMPARE_DELTA = 1e-6;
	
	PropFactory mFac;
	PropFactory mPrevFac;

	@Before
	public void setUp() {
		mFac = new LBImpls.LBImplFactory();
		mPrevFac = LBImpls.setTestFactory(mFac);
	}

	@After
	public void tearDown() {
		// re-instantiate existing factory
		LBImpls.setTestFactory(mPrevFac);
	}
	
	private NumericResolver createNumResolver(PAS pas) {
		NumericResolver nr = new NumericResolverImpl(new ImplicateResolver(pas.getKB(), pas.getPropositions(), pas.getAssumptions(), mFac), 
				new ProbabilityComputer_BDD(1024), mFac);
		return nr;
	}
	
	static PAS createBasePAS(PropFactory fac) throws KBException
	{
		List<Assumption> asmts = Arrays.asList(
				fac.createAssumption("A", false, 0.1),
				fac.createAssumption("B", false, 0.2),
				fac.createAssumption("C", false, 0.3)
				);
		List<Proposition> props = Arrays.asList(
				fac.createProposition("x", false),
				fac.createProposition("y", false),
				fac.createProposition("z", false)
				);
		PAS pas = new PASImpl(asmts, props, fac);
		return pas;
	}

	static PAS createAltPAS(PropFactory fac) throws KBException
	{
		List<Assumption> asmts = Arrays.asList(
				fac.createAssumption("Alfa{Xea,Mika}", false, 0.1),
				fac.createAssumption("Beta{yeah}", false, 0.2),
				fac.createAssumption("Ceta{Zika}", false, 0.3)
				);
		List<Proposition> props = Arrays.asList(
				fac.createProposition("x{norma}", false),
				fac.createProposition("y{zippa}", false),
				fac.createProposition("z{kippa}", false)
				);
		PAS pas = new PASImpl(asmts, props, fac);
		return pas;
	}
	
	@Test(expected = KBException.class)
	public void testInvalidAssumption() throws KBException
	{
		PAS pas = new PASImpl(mFac);
		pas.createAssumption("bla", false, Double.NaN);
	}

	@Test
	public void testAddAssumptions() throws KBException
	{
		PAS pas = new PASImpl(mFac);

		Assumption aFirst = pas.createAssumption("MyAssumption", false, 0.5);
		Assert.assertNotNull(aFirst);
		
		Assumption aSecond = pas.createAssumption("MySecondAssumption", false, 0.2);
		Assert.assertNotNull(aSecond);
		Assert.assertNotEquals(aFirst, aSecond);
		
		Assumption aFirstAgain = pas.createAssumption("MyAssumption", false, 0.5);
		Assert.assertSame(aFirst, aFirstAgain);
		
		//Check that we can get it back correctly
		Assumption aFirstGot = pas.getAssumption(aFirst.getName(), true);
		Assert.assertEquals(aFirst, aFirstGot);
		
		//Check we get the negated correctly
		Assumption aFirstGotNeg = pas.getAssumption(aFirst.getName(), false);
		Assert.assertTrue(aFirstGotNeg.getNeg());
		Assert.assertEquals(aFirst.getIndex(), aFirstGotNeg.getIndex());
		
		Assumption aNone = pas.getAssumption("NoneHere", true);
		Assert.assertNull(aNone);

		{
			KBException gotException = null;
			try {
				pas.createAssumption("MyAssumption", false, 0.1);
			} catch(KBException e)
			{
				gotException = e;
			}
			Assert.assertNotNull(gotException);
			Assert.assertTrue(gotException.getMessage().contains("probability different"));
		}

		{
			KBException gotException = null;
			try {
				pas.createProposition("MyAssumption", false);
			} catch(KBException e)
			{
				gotException = e;
			}
			Assert.assertNotNull(gotException);
			Assert.assertTrue(gotException.getMessage().contains("literal already exists"));
		}
	}

	@Test
	public void testAddPropositions() throws KBException
	{
		PAS pas = new PASImpl(mFac);

		Proposition pFirst = pas.createProposition("MyProp", false);
		Assert.assertNotNull(pFirst);
		
		Proposition pSecond = pas.createProposition("MySecondProp", false);
		Assert.assertNotNull(pSecond);
		Assert.assertNotEquals(pFirst, pSecond);
		
		Proposition pFirstAgain = pas.createProposition(pFirst.getName(), false);
		Assert.assertSame(pFirst, pFirstAgain);
		
		// Check we get correctly
		Proposition pFirstGot = pas.getProposition(pFirst.getName(), true);
		Assert.assertEquals(pFirst, pFirstGot);

		// Check we get the neg correctly
		Proposition pFirstGotNeg = pas.getProposition(pFirst.getName(), false);
		Assert.assertTrue(pFirstGotNeg.getNeg());
		Assert.assertEquals(pFirst.getIndex(), pFirstGotNeg.getIndex());
		
		Proposition pNone = pas.getProposition("NoneHere", true);
		Assert.assertNull(pNone);

		{
			KBException gotException = null;
			try {
				pas.createAssumption("MyProp", false, 0.1);
			} catch(KBException e)
			{
				gotException = e;
			}
			Assert.assertNotNull(gotException);
			Assert.assertTrue(gotException.getMessage().contains("literal already exists"));
		}
	}

	@Test
	public void testGetAssumptions() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		Assert.assertEquals(6, FluentIterable.from(pas.getAssumptions()).size());
		Assert.assertEquals(3, FluentIterable.from(pas.getAssumptions(true)).size());
		Assert.assertTrue(FluentIterable.from(pas.getAssumptions(true)).allMatch(a -> !a.getNeg()));
		Assert.assertEquals(3, FluentIterable.from(pas.getAssumptions(false)).size());
		Assert.assertTrue(FluentIterable.from(pas.getAssumptions(false)).allMatch(a -> a.getNeg()));
	}

	@Test
	public void testGetPropositions() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		Assert.assertEquals(6, FluentIterable.from(pas.getPropositions()).size());
		Assert.assertEquals(3, FluentIterable.from(pas.getPropositions(true)).size());
		Assert.assertTrue(FluentIterable.from(pas.getPropositions(true)).allMatch(p -> !p.getNeg()));
		Assert.assertEquals(3, FluentIterable.from(pas.getPropositions(false)).size());
		Assert.assertTrue(FluentIterable.from(pas.getPropositions(false)).allMatch(p -> p.getNeg()));
	}

	// Because the notation here is so much more compact we can do a quite large amount of testing here,
	// some of which is being repeated, but good to have end to end also here.
	@Test
	public void testConstructTerm() throws KBException
	{
		PAS pas = createBasePAS(mFac);

		// Empty term
		{
			Expression<LogicalAnd> term = pas.constructTerm("  ");
			Assert.assertTrue(term.isTrue());			
		}
		// Regular terms
		{
			Expression<LogicalAnd> term = pas.constructTerm("C x A");
			Assert.assertEquals("A C x", term.toString());
		}
		{
			Expression<LogicalAnd> term = pas.constructTerm(" A  C   B  ");
			Assert.assertEquals("A B C", term.toString());
		}
		{
			Expression<LogicalAnd> term = pas.constructTerm(" ¬A  C   ¬B  ");
			Assert.assertEquals("¬A ¬B C", term.toString());
		}
		// Simplifying terms
		{
			Expression<LogicalAnd> term = pas.constructTerm(" A x x y A ");
			Assert.assertEquals("A x y", term.toString());
		}
		{
			Expression<LogicalAnd> term = pas.constructTerm(" x A ¬x ");
			Assert.assertTrue(term.isFalse());			
		}
		// Specials
		{
			Expression<LogicalAnd> term = pas.constructTerm(" x False ¬y ");
			Assert.assertTrue(term.isFalse());
			 // This is not empty, it contains the special \bot "False" literal
			Assert.assertEquals(1, term.getLength());
		}
		{
			Expression<LogicalAnd> term = pas.constructTerm(" ¬A True B ");
			Assert.assertEquals("¬A B", term.toString());
		}
		{
			Expression<LogicalAnd> term = pas.constructTerm("  True ");
			Assert.assertTrue(term.isTrue());
			Assert.assertEquals(0, term.getLength());
		}
		{
			Expression<LogicalAnd> term = pas.constructTerm("  ¬True A  ");
			Assert.assertTrue(term.isFalse());
		}
		{
			Expression<LogicalAnd> term = pas.constructTerm("  ¬False B C ");
			Assert.assertEquals("B C", term.toString());
		}
	}
	
	@Test(expected = KBException.class)
	public void testInvalidTermAsClause() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		pas.constructTerm("A + B");
	}
	@Test(expected = KBException.class)
	public void testInvalidTermWithGarbage() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		pas.constructTerm("A B ,");
	}
	@Test(expected = KBException.class)
	public void testInvalidTermAsSentence() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		pas.constructTerm("[A B]");
	}
	@Test(expected = KBException.class)
	public void testInvalidLit() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		pas.constructTerm("A Z");
	}

	@Test
	public void testConstructClause() throws KBException
	{
		PAS pas = createBasePAS(mFac);

		// Empty clause
		{
			Expression<LogicalOr> clause = pas.constructClause("()");
			Assert.assertTrue(clause.isFalse());			
		}
		{
			Expression<LogicalOr> clause = pas.constructClause(" ( )  ");
			Assert.assertTrue(clause.isFalse());			
		}
		// Regular clauses
		{
			Expression<LogicalOr> clause = pas.constructClause("C + x + A");
			Assert.assertEquals("(A + C + x)", clause.toString());
		}
		{
			Expression<LogicalOr> clause = pas.constructClause("(C + x + A)");
			Assert.assertEquals("(A + C + x)", clause.toString());
		}
		{
			Expression<LogicalOr> clause = pas.constructClause(" A +  ¬C +   B  ");
			Assert.assertEquals("(A + B + ¬C)", clause.toString());
		}
		{
			Expression<LogicalOr> clause = pas.constructClause(" ( ¬A +  C +   B  )  ");
			Assert.assertEquals("(¬A + B + C)", clause.toString());
		}
		// Simplifying terms
		{
			Expression<LogicalOr> clause = pas.constructClause(" A + x + x + y + A ");
			Assert.assertEquals("(A + x + y)", clause.toString());
		}
		// Specials
		{
			Expression<LogicalOr> clause = pas.constructClause(" A + x + False ");
			Assert.assertEquals("(A + x)", clause.toString());
		}
		{
			Expression<LogicalOr> clause = pas.constructClause(" ¬A + True + B ");
			Assert.assertTrue(clause.isTrue());
			Assert.assertEquals(1, clause.getLength());
		}
		{
			Expression<LogicalOr> clause = pas.constructClause("  False ");
			Assert.assertTrue(clause.isFalse());
		}
		{
			Expression<LogicalOr> clause = pas.constructClause("  ¬True + A  ");
			Assert.assertEquals("(A)", clause.toString());
		}
		{
			Expression<LogicalOr> clause = pas.constructClause("  ¬False + B + C ");
			Assert.assertTrue(clause.isTrue());
		}	
	}

	@Test(expected = KBException.class)
	public void testInvalidClauseAsTerm() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		pas.constructClause("A B");
	}
	@Test(expected = KBException.class)
	public void testInvalidClauseWithGarbage() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		pas.constructClause("A + B %");
	}
	@Test(expected = KBException.class)
	public void testInvalidClauseAsSentence() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		pas.constructClause("[A + B]");
	}
	@Test(expected = KBException.class)
	public void testInvalidClauseWithLit() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		pas.constructClause("A + Z");
	}
	@Test(expected = KBException.class)
	public void testInvalidClauseMissingBracket() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		pas.constructClause("(A + B");
	}

	@Test
	public void testConstructDNF() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		
		//Empty DNFs
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF("");
			Assert.assertTrue(dnf.isFalse());
		}
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF("  ");
			Assert.assertTrue(dnf.isFalse());
		}
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF("  [ ] ");
			Assert.assertTrue(dnf.isFalse());
		}
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF("[]");
			Assert.assertTrue(dnf.isFalse());
		}
		//Regular DNFs
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF("[A x + A y]");
			Assert.assertEquals("[A x + A y]", dnf.toString());
		}
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF("A x   + A y");
			Assert.assertEquals("[A x + A y]", dnf.toString());
		}
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF("[  A x  + A z y  ]");
			Assert.assertEquals("[A x + A y z]", dnf.toString());
		}
		// Simplifying DNFs
		// Again, strictly speaking these don't belong here but they provide a good end to end test.
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF("[A x + A y + A]");
			Assert.assertEquals("[A]", dnf.toString());
		}
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF("[B x A + A y B + A B]");
			Assert.assertEquals("[A B]", dnf.toString());
		}
		// Specials
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF("True");
			Assert.assertTrue(dnf.isTrue());
		}
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF("False");
			Assert.assertTrue(dnf.isFalse());
		}
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF("[A x + A y + True]");
			Assert.assertTrue(dnf.isTrue());
			Assert.assertEquals(1, dnf.getLength()); // contains the special True term
		}
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF("[A x + False + A y]");
			Assert.assertEquals("[A x + A y]", dnf.toString());
		}
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = pas.constructDNF("[A x + ¬False + A y]");
			Assert.assertTrue(dnf.isTrue());
		}
	}
	
	@Test(expected = KBException.class)
	public void testInvalidDNFAsCNF() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		pas.constructDNF("(A + B)");
	}
	@Test(expected = KBException.class)
	public void testInvalidDNFWithBracketMismatch() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		pas.constructDNF("[A B + x");
	}

	@Test
	public void testConstructCNF() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		
		//Empty CNFs
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF("()");
			Assert.assertTrue(cnf.isTrue());
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF("[()]");
			Assert.assertTrue(cnf.isTrue());
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF("( ) ");
			Assert.assertTrue(cnf.isTrue());
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF(" [  ()]");
			Assert.assertTrue(cnf.isTrue());
		}
		//Regular CNFs
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF("[(A + x)(A + y)]");
			Assert.assertEquals("[(A + x)(A + y)]", cnf.toString());
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF("(A + x)(A + y)");
			Assert.assertEquals("[(A + x)(A + y)]", cnf.toString());
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF(" (  A + x  ) (  A + y)  ");
			Assert.assertEquals("[(A + x)(A + y)]", cnf.toString());
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF("[(A + x  )  ( A + y)]");
			Assert.assertEquals("[(A + x)(A + y)]", cnf.toString());
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF("[ (  A + x)  (A +  z +  y )  ]");
			Assert.assertEquals("[(A + x)(A + y + z)]", cnf.toString());
		}
		// Simplifying CNFs
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF("[(A + x)(A + y)(A)]");
			Assert.assertEquals("[(A)]", cnf.toString());
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF("[(B + x + A) (A + y + B) (A + B)]");
			Assert.assertEquals("[(A + B)]", cnf.toString());
		}
		// Specials
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF("(False)");
			Assert.assertTrue(cnf.isFalse());					
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF("(True)");
			Assert.assertTrue(cnf.isTrue());					
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF("(A + x)(True)(A + y)");
			Assert.assertEquals("[(A + x)(A + y)]", cnf.toString());
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF("(A + x)(False)(A + y)");
			Assert.assertTrue(cnf.isFalse());
			Assert.assertEquals(1, cnf.getLength()); // contains special False clause
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> cnf = pas.constructCNF("(A + x)(¬True)(A + y)");
			Assert.assertTrue(cnf.isFalse());
		}
	}
	@Test(expected = KBException.class)
	public void testInvalidCNFAsDNF() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		pas.constructCNF("A B");
	}
	@Test(expected = KBException.class)
	public void testInvalidCNFWithBracketMismatch() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		pas.constructDNF("[(A + B) (x)");
	}

	@Test
	public void testHCConstruction() throws KBException 
	{
		{
			PAS pas = createBasePAS(mFac);
			pas.addHornClause("A -> x");
			Assert.assertEquals("[(¬A + x)]", pas.getKB().toString());
			Assert.assertEquals("[(A -> x)]", mFac.getHornStringer().stringise(pas.getKB()));
		}
		{
			PAS pas = createBasePAS(mFac);
			pas.addHornClause("y A B -> x");
			Assert.assertEquals("[(¬A + ¬B + x + ¬y)]", pas.getKB().toString());
			Assert.assertEquals("[(A B y -> x)]", mFac.getHornStringer().stringise(pas.getKB()));
		}
		{
			PAS pas = createBasePAS(mFac);
			pas.addHornClause(" ¬A B  ->   ¬x ");
			Assert.assertEquals("[(A + ¬B + ¬x)]", pas.getKB().toString());
			Assert.assertEquals("[(¬A B -> ¬x)]", mFac.getHornStringer().stringise(pas.getKB()));
		}
		{
			PAS pas = createBasePAS(mFac);
			pas.addHornClause("A -> x");
			pas.addHornClause("B -> x");
			pas.addHornClause("A C B -> ¬y"); // no the lexical order of the assumptions is mixed.
			Assert.assertEquals("[(A -> x),(B -> x),(A B C -> ¬y)]", mFac.getHornStringer().stringise(pas.getKB()));
		}
		{
			PAS pas = createBasePAS(mFac);
			pas.addHornClause("(A -> x)");
			pas.addHornClause("(B -> y)");
			Assert.assertEquals("[(A -> x),(B -> y)]", mFac.getHornStringer().stringise(pas.getKB()));
		}
		{
			PAS pas = createBasePAS(mFac);
			pas.addHornClause("  (   A   -> x  )  ");
			pas.addHornClause("( A  B -> z)");
			Assert.assertEquals("[(A -> x),(A B -> z)]", mFac.getHornStringer().stringise(pas.getKB()));
		}
		// Finally let's test the stringiser here a little. We create a custom stringiser that suppresses the default
		// sorting, and see that we get just what we put back.
		{
			LogicalStringer mHornStringNoSort = StringOps.createHornStringer(mFac.getDefaultSymboliser(), null, 1000);
			PAS pas = createBasePAS(mFac);
			pas.addHornClause("y A B -> x");
			pas.addHornClause("C B A -> ¬y");
			Assert.assertEquals("[(y A B -> x),(C B A -> ¬y)]", mHornStringNoSort.stringise(pas.getKB()));
		}
	}

	@Test(expected = KBException.class)
	public void testHCBadTerm() throws KBException 
	{
		PAS pas = createBasePAS(mFac);
		pas.addHornClause("A + B -> x");		
	}
	@Test(expected = KBException.class)
	public void testHCWithGarbage() throws KBException 
	{
		PAS pas = createBasePAS(mFac);
		pas.addHornClause("(A B -> x");		
	}
	@Test(expected = KBException.class)
	public void testHCBadStructure() throws KBException 
	{
		PAS pas = createBasePAS(mFac);
		pas.addHornClause("A + B + x");		
	}
	@Test(expected = KBException.class)
	public void testHCUnknownLitInBody() throws KBException 
	{
		PAS pas = createBasePAS(mFac);
		pas.addHornClause("A + Z -> x");		
	}
	@Test(expected = KBException.class)
	public void testHCUnknownLitInHead() throws KBException 
	{
		PAS pas = createBasePAS(mFac);
		pas.addHornClause("A + B -> Z");		
	}

	// Test use of more complex assumption and proposition names
	@Test
	public void testAltKBConstruction() throws KBException
	{
		{
			PAS pas = createAltPAS(mFac);
			pas.addHornClause("Alfa{Xea,Mika} -> x{norma}");
			Assert.assertEquals("[(¬Alfa{Xea,Mika} + x{norma})]", pas.getKB().toString());
		}
		{
			PAS pas = createAltPAS(mFac);
			pas.addHornClause("(Alfa{Xea,Mika} -> x{norma})");
			Assert.assertEquals("[(¬Alfa{Xea,Mika} + x{norma})]", pas.getKB().toString());
		}
		{
			PAS pas = createAltPAS(mFac);
			pas.addHornClause(" y{zippa}  ->  z{kippa}");
			Assert.assertEquals("[(¬y{zippa} + z{kippa})]", pas.getKB().toString());
		}
		{
			PAS pas = createAltPAS(mFac);
			pas.addHornClause(" (  y{zippa}  ->  z{kippa}  )  ");
			Assert.assertEquals("[(¬y{zippa} + z{kippa})]", pas.getKB().toString());
		}
		{
			PAS pas = createAltPAS(mFac);
			pas.addHornClause("Alfa{Xea,Mika} x{norma} ¬y{zippa} -> ¬z{kippa}");
			Assert.assertEquals("[(¬Alfa{Xea,Mika} + ¬x{norma} + y{zippa} + ¬z{kippa})]", pas.getKB().toString());
		}
		// This goes into an infinite loop?
//		{
//			PAS pas = createAltPAS(mFac);
//			pas.addHornClause("  (   Alfa{Xea,Mika} x{norma} ¬y{zippa}  )   -> ¬z{kippa}");
//			Assert.assertEquals("[(¬Alfa{Xea,Mika} + ¬x{norma} + y{zippa} + ¬z{kippa})]", pas.getKB().toString());
//		}
	}
	
	@Test
	public void testKBBasics() throws KBException 
	{
		PAS pas = createBasePAS(mFac);
		pas.addHornClause("A -> x");
		pas.addHornClause("A -> ¬y");
		pas.addHornClause("A -> ¬z");

		pas.addHornClause("B -> ¬x");
		pas.addHornClause("B -> y");
		
		pas.addHornClause("C -> z");
		
		NumericResolver nr = createNumResolver(pas);
		
		// Check inconsistency
		{
			Expression<LogicalOr> falsity = mFac.createClause();
			SimpleSentence<LogicalOr, LogicalAnd> senQS = nr.findQS(falsity);

			// A and B don't agree on x
			// A and C don't agree on z
			// y doesn't involved inconsistency right now.
			// TODO: Need to do this using the equalness between a clause and this.
			// and for that the clause needs to have proper equalness implemented.
			Assert.assertEquals("[A C + A B]", senQS.toString());
		}
		
		// Check a simple hypothesis
		{
			SimpleSentence<LogicalAnd, LogicalOr> hcnf = pas.constructCNF("[(x)]");
			SimpleSentence<LogicalOr, LogicalAnd> senQS = nr.findQS(hcnf);
			SimpleSentence<LogicalOr, LogicalAnd> senSP = nr.findSP(hcnf);
			
			// This is A + inconsistency, which is still A (minimal argument reduction).
			Assert.assertEquals("[A]", senQS.toString());
			// This is a subset of A that avoid inconsistency
			Assert.assertEquals("[A ¬B ¬C]", senSP.toString());
			
			// The unnormalised DSP calculated using dqs only should be equal to the computation of the probability
			// using the symbolically worked out SP. (dsp calculation doesn't every need explicit
			// calculation of SP which is more expensive compared to doing QS).
			double unnormDSP = nr.calcNonNormalisedDSP(hcnf);
			double unnormDSP_direct = nr.computeDNFProbability(senSP);
			Assert.assertEquals(unnormDSP, unnormDSP_direct, DOUBLE_COMPARE_DELTA);
		}
		
		// Check impossible hypothesis
		{
			SimpleSentence<LogicalAnd, LogicalOr> hcnf = pas.constructCNF("[(x)(y)]");
			// The only "real" hypothesis here is [A B], but that's inconsistent.
			// The QS also contains all the inconsistent scenarios, so we get
			// [A C] in addition giving all together:
			// [A C + A B]
			SimpleSentence<LogicalOr, LogicalAnd> senQS = nr.findQS(hcnf);
			Assert.assertEquals("[A C + A B]", senQS.toString());
			SimpleSentence<LogicalOr, LogicalAnd> senSP = nr.findSP(hcnf);			
			Assert.assertEquals("[False]", senSP.toString());
		}

		// Now check more complex hypothesis
		{
			SimpleSentence<LogicalAnd, LogicalOr> hcnf = pas.constructCNF("[(x + y)(z)]");
			// The "real" hypotheses are 
			// [A C + B C]
			// [A C] is inconsistent already.
			// Additionally we get [A B] as an added inconsistent scenario:
			// [B C + A C + A B]
			SimpleSentence<LogicalOr, LogicalAnd> senQS = nr.findQS(hcnf);
			Assert.assertEquals("[B C + A C + A B]", senQS.toString());
			
			SimpleSentence<LogicalOr, LogicalAnd> senSP = nr.findSP(hcnf);
			// B C otherwise is the only consistent argument.
			// But A can't be along side B as that's inconsistent.
			Assert.assertEquals("[¬A B C]", senSP.toString());
		}
	}

	@Test
	public void testLongKB() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		pas.addLiteral(mFac.createProposition("x2", false));
		pas.addLiteral(mFac.createProposition("y2", false));
		pas.addLiteral(mFac.createProposition("z2", false));
	
		pas.addLiteral(mFac.createProposition("x3", false));
		pas.addLiteral(mFac.createProposition("y3", false));
		pas.addLiteral(mFac.createProposition("z3", false));

		pas.addLiteral(mFac.createProposition("t", false));
		
		pas.addHornClause("A -> x");
		pas.addHornClause("x -> y");
		pas.addHornClause("y -> z");
		pas.addHornClause("z -> t");
		
		pas.addHornClause("B -> x2");
		pas.addHornClause("x2 -> y2");
		pas.addHornClause("y2 -> z2");
		pas.addHornClause("z2 -> t");

		pas.addHornClause("C -> x3");
		pas.addHornClause("x3 -> y3");
		pas.addHornClause("y3 -> z3");
		pas.addHornClause("z3 -> t");

		SimpleSentence<LogicalAnd, LogicalOr> h = pas.constructCNF("(t)");
		
		// Check the KB w/o any contradictions
		{
			NumericResolver nr = createNumResolver(pas);
			SimpleSentence<LogicalOr, LogicalAnd> senQS = nr.findQS(h);
			Assert.assertEquals("[C + B + A]", senQS.toString());

			SimpleSentence<LogicalOr, LogicalAnd> senSP = nr.findSP(h);
			Assert.assertEquals("[C + B + A]", senSP.toString());			
		}
		
		{
			// Add a clause that says "Either A or B is incorrect.",
			// see how that affects our argumentation.
			pas.addHornClause("A -> ¬B");
			
			NumericResolver nr = createNumResolver(pas);
			SimpleSentence<LogicalOr, LogicalAnd> senQS = nr.findQS(h);
			Assert.assertEquals("[C + B + A]", senQS.toString());

			SimpleSentence<LogicalOr, LogicalAnd> senSP = nr.findSP(h);
			Assert.assertEquals("[¬A C + ¬B C + ¬A B + A ¬B]", senSP.toString());			
		}
	}
	
	@Test
	public void testKBWithAssumptions() throws KBException 
	{
		PAS pas = createBasePAS(mFac);
		
		pas.addHornClause("A -> B");
		pas.addHornClause("B -> C");
		pas.addHornClause("C -> x");
		
		NumericResolver nr = createNumResolver(pas);
		Assert.assertEquals(nr.findQS(pas.constructCNF("(x)")), pas.constructDNF("[A + B + C]"));
	}
	
	// This is a toy scenario simulating the conditions for the care a new born baby in a family. The idea is to give
	// the developer tangible propositions and assumptions from real life to help develop an intuition on how PAS
	// works.
	@Test
	public void testFamilyContradictions() throws KBException
	{
		PAS pas = new PASImpl(mFac);
		//Assumptions
		pas.createAssumption("Baby_may_cry_much", false, 0.4);
		pas.createAssumption("Mother_will_find_permy_job_soon", false, 0.85);
		pas.createAssumption("Mother_perm_job_may_afford_day_care", false, 0.95);
		pas.createAssumption("Mother_will_find_free_lance", false, 0.85);
		pas.createAssumption("Mother_free_lance_may_afford_day_care", false, 0.30);
		pas.createAssumption("Mother_may_be_ill_post_birth", false, 0.1);
		pas.createAssumption("May_find_daycare", false, 0.9);
		pas.createAssumption("Father_income_may_pay_for_day_care", false, 0.4);
		// Propositions
		pas.createProposition("mother_works", false);
		pas.createProposition("daycare_afforded", false);
		pas.createProposition("day_care_needed", false);
		pas.createProposition("baby_cared_for", false);
		
		// KB
		pas.addHornClause("mother_works -> day_care_needed");
		pas.addHornClause("day_care_needed May_find_daycare daycare_afforded -> baby_cared_for");
		pas.addHornClause("Father_income_may_pay_for_day_care -> daycare_afforded");
		pas.addHornClause("Mother_will_find_permy_job_soon Mother_perm_job_may_afford_day_care -> daycare_afforded");
		pas.addHornClause("Mother_will_find_free_lance Mother_free_lance_may_afford_day_care -> daycare_afforded");
		pas.addHornClause("Mother_will_find_permy_job_soon -> mother_works");
		pas.addHornClause("Mother_will_find_free_lance -> mother_works");
		pas.addHornClause("Mother_may_be_ill_post_birth -> ¬mother_works");
		pas.addHornClause("¬Mother_may_be_ill_post_birth ¬mother_works -> baby_cared_for");
		pas.addHornClause("Baby_may_cry_much -> ¬mother_works");
		pas.addHornClause("¬baby_cared_for -> ¬mother_works");
		pas.addHornClause("Mother_will_find_permy_job_soon -> ¬Mother_will_find_free_lance");
		pas.addHornClause("Mother_will_find_free_lance -> ¬Mother_will_find_permy_job_soon");
		
		// h
		SimpleSentence<LogicalAnd, LogicalOr> hBabyCaredFor = pas.constructCNF("(baby_cared_for)");
		
		NumericResolver nr = createNumResolver(pas);
		
		if(VERBOSE)
		{
			System.out.println("h: " + hBabyCaredFor);
			System.out.println("QS:" + nr.findQS(hBabyCaredFor) + " = " + nr.calcDQS(hBabyCaredFor));
			System.out.println("SP:" + nr.findSP(hBabyCaredFor));
			System.out.println("Inconsistent:" + nr.findQS(mFac.createClause()));
			System.out.println("dsp (unnormalised):" + nr.calcNonNormalisedDSP(hBabyCaredFor));
			System.out.println("dsp (normalised):" + nr.calcNormalisedDSP(hBabyCaredFor));
			System.out.println("dqs_I:" + nr.calcDQS_I());			
		}
		
		Assert.assertEquals(0.15794999999999992, nr.calcNonNormalisedDSP(hBabyCaredFor), DOUBLE_COMPARE_DELTA);
		Assert.assertEquals(0.9859550561797754, nr.calcNormalisedDSP(hBabyCaredFor), DOUBLE_COMPARE_DELTA);
		Assert.assertEquals(0.8398000000000001, nr.calcDQS_I(), DOUBLE_COMPARE_DELTA);
	}
	
	@Test
	public void testConstructHornClause() throws KBException 
	{
		PAS pas = createBasePAS(mFac);
		
		Expression<LogicalOr> cl = pas.constructHornClause("A -> ¬x");
		Assert.assertEquals("Unable to consturct correct horn clause", "(¬A + ¬x)", cl.toString());
	}
	
	@Test
	public void testFactory() throws KBException
	{
		PAS pas = createBasePAS(mFac);
		Assert.assertEquals(mFac, pas.getFactory());
	}
}
