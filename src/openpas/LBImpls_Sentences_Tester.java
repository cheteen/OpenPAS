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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import openpas.basics.Assumption;
import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SentenceNotUpdatedException;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.Literal;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.PropFactory;
import openpas.basics.Proposition;
import openpas.utils.ArrayIterable;

public class LBImpls_Sentences_Tester{
	
	PropFactory mFac;	
	
	Literal mLa;
	Literal mLna;
	Literal mLb;
	Literal mLnb;
	Literal mLc;
	Literal mLnc;
	Literal mLd;
	Literal mLnd;
	Literal mLe;
	Literal mLne;
	Literal mLf;
	Literal mLnf;
	Literal mLg;
	Literal mLng;
	
	@Before
	public void setUp() throws Exception {
		mFac = new LBImpls.LBImplFactory();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	void setLiterals()
	{
		mLa = mFac.createProposition("a", false);
		mLna = mLa.cloneNegated();
		mLb = mFac.createProposition("b", false);
		mLnb = mLb.cloneNegated();
		mLc = mFac.createProposition("c", false);
		mLnc = mLc.cloneNegated();
		mLd = mFac.createProposition("d", false);
		mLnd = mLd.cloneNegated();		
		mLe = mFac.createProposition("e", false);
		mLne = mLe.cloneNegated();		
		mLf = mFac.createProposition("f", false);
		mLnf = mLf.cloneNegated();		
		mLg = mFac.createProposition("g", false);
		mLng = mLf.cloneNegated();		
	}
	
	@Test
	public void testSpecialSentences()
	{
		Assert.assertTrue(mFac.getTrueCNF().isTrue());
		Assert.assertTrue(mFac.getTrueDNF().isTrue());

		Assert.assertTrue(mFac.getFalseCNF().isFalse());
		Assert.assertTrue(mFac.getFalseDNF().isFalse());
	}

	@Test
	public void testCreateSimpleCNFSentence()
	{
		setLiterals();

		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc}));
		Expression<LogicalOr> elm2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLnd, mLe, mLnf}));
		
		SimpleSentence<LogicalAnd, LogicalOr> sen = mFac.createCNFSentence();
		Assert.assertTrue(sen.addElement(elm1));
		Assert.assertTrue(sen.addElement(elm2));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[(a + ¬b + ¬c)(¬d + e + ¬f)]", str);
	}
	@Test
	public void testCreatePrototypedCNFSentence()
	{
		setLiterals();

		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc}));
		
		SimpleSentence<LogicalAnd, LogicalOr> sen = mFac.createCNFSentence();
		
		SimpleSentence<LogicalAnd, LogicalOr> senFrom = mFac.createSentenceLike(sen);
		
		Assert.assertTrue(senFrom.addElement(elm1));
		
		String str = mFac.getDefaultStringer().stringise(senFrom);
		Assert.assertEquals("[(a + ¬b + ¬c)]", str);
	}
	@Test
	public void testCreateEmptyCNFSentence()
	{
		SimpleSentence<LogicalAnd, LogicalOr> sen = mFac.createCNFSentence();

		Assert.assertTrue(sen.isTrue());
		Assert.assertFalse(sen.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mFac.getTrue()}))));
		Assert.assertFalse(sen.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mFac.getTrue()}))));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[(True)]", str);
	}
	@Test
	public void testCreateLongerCNFSentence()
	{
		setLiterals();

		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalOr> elm2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		Expression<LogicalOr> elm3 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLe, mLnf}));
		Expression<LogicalOr> elm4 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLg}));
		
		SimpleSentence<LogicalAnd, LogicalOr> sen = mFac.createCNFSentence();
		Assert.assertTrue(sen.addElement(elm1));
		Assert.assertTrue(sen.addElement(elm2));
		Assert.assertTrue(sen.addElement(elm3));
		Assert.assertTrue(sen.addElement(elm4));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[(a + ¬b)(c + ¬d)(e + ¬f)(g)]", str);
	}
	@Test
	public void testCreateLongerCNFSentenceWithConstruct()
	{
		setLiterals();

		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalOr> elm2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		Expression<LogicalOr> elm3 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLe, mLnf}));
		Expression<LogicalOr> elm4 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLg}));
		
		@SuppressWarnings("unchecked")
		SimpleSentence<LogicalAnd, LogicalOr> sen = 
			mFac.createCNFSentence(new ArrayIterable<Expression<LogicalOr>>(new Expression[] {elm1, elm2, elm3, elm4}));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[(a + ¬b)(c + ¬d)(e + ¬f)(g)]", str);
	}
	@Test
	public void testCreateSimplifiableCNFSentence()
	{
		setLiterals();

		// These should all reduce to just 'a + ¬b + ¬c'
		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc, mLd, mLne}));
		Expression<LogicalOr> elm2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc, mLd}));
		Expression<LogicalOr> elm3 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc}));
		
		// And this to '¬c + d'
		Expression<LogicalOr> elm4 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLb, mLnc, mLd}));
		Expression<LogicalOr> elm5 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLnc, mLd}));

		SimpleSentence<LogicalAnd, LogicalOr> sen = mFac.createCNFSentence();
		Assert.assertTrue(sen.addElement(elm1));
		Assert.assertTrue(sen.addElement(elm2));
		Assert.assertTrue(sen.addElement(elm3));
		Assert.assertTrue(sen.addElement(elm4));
		Assert.assertTrue(sen.addElement(elm5));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[(a + ¬b + ¬c)(¬c + d)]", str);
	}
	
	@Test
	public void testCreateSimplifiableCNFSentenceReverse()
	{
		setLiterals();

		// These should all reduce to just 'a + ¬b + ¬c'
		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc}));
		Expression<LogicalOr> elm2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc, mLd}));
		Expression<LogicalOr> elm3 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc, mLd, mLne}));
		
		// And this to '¬c + d'
		Expression<LogicalOr> elm4 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLnc, mLd}));
		Expression<LogicalOr> elm5 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLb, mLnc, mLd}));

		SimpleSentence<LogicalAnd, LogicalOr> sen = mFac.createCNFSentence();
		Assert.assertTrue(sen.addElement(elm1));
		Assert.assertFalse(sen.addElement(elm2));
		Assert.assertFalse(sen.addElement(elm3));
		
		Assert.assertTrue(sen.addElement(elm4));
		Assert.assertFalse(sen.addElement(elm5));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[(a + ¬b + ¬c)(¬c + d)]", str);
	}
	@Test
	public void testCreateCNFWithFalse()
	{
		setLiterals();

		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalOr> elm2 = mFac.createClause(); // False
		Expression<LogicalOr> elm3 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		Expression<LogicalOr> elm4 = mFac.createClause(); // False
		
		SimpleSentence<LogicalAnd, LogicalOr> sen = mFac.createCNFSentence();
		Assert.assertTrue(sen.addElement(elm1));
		Assert.assertTrue(sen.addElement(elm2)); // False, changes DNF to True
		Assert.assertFalse(sen.addElement(elm3));
		Assert.assertFalse(sen.addElement(elm4));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[(False)]", str);
	}		
	@Test
	public void testCreateCNFWithTrue()
	{
		setLiterals();

		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalOr> elm2 = mFac.createClause(); // False
		Expression<LogicalOr> elm3 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		
		SimpleSentence<LogicalAnd, LogicalOr> sen = mFac.createCNFSentence();
		Assert.assertTrue(sen.addElement(elm1));
		Assert.assertTrue(sen.addElement(elm2)); // False, falsifies sentence
		Assert.assertFalse(sen.addElement(elm3));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[(False)]", str);
	}
	@Test
	public void testUpdateCNFToFalse()
	{
		setLiterals();

		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalOr> elm2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		Expression<LogicalOr> elm3 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLe, mLnf}));
		Expression<LogicalOr> elm4 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLg}));
		
		@SuppressWarnings("unchecked")
		SimpleSentence<LogicalAnd, LogicalOr> sen = 
			mFac.createCNFSentence(new ArrayIterable<Expression<LogicalOr>>(new Expression[] {elm1, elm2, elm3, elm4}));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[(a + ¬b)(c + ¬d)(e + ¬f)(g)]", str);
		
		// Empty clause elm2 so that it's False now.
		Assert.assertTrue(elm2.removeLiteral(mLc));
		Assert.assertTrue(elm2.removeLiteral(mLnd));

		// See that the sentence needs updating - returns invalid (non-minimal) result:
		str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[(a + ¬b)(False)(e + ¬f)(g)]", str);

		// Now update it to get the minimal view:
		sen.update();
		
		str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[(False)]", str);
	}
	
	@Test
	public void testExceptionFiredBeforeUpdate()
	{
		setLiterals();

		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalOr> elm2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));

		SimpleSentence<LogicalAnd, LogicalOr> sen = mFac.createCNFSentence(Arrays.asList(elm1, elm2));

		// Can see that the sentence needs updating.
		boolean updateExceptionCaught = false;
		try
		{
			Assert.assertTrue(sen.hashCode() != 0); // There's a once in a blue-moon chance this can fail here.
			elm1.removeLiteral(mLa);//modify contained expression w/o telling the sentence.
			Assert.assertTrue(sen.hashCode() != 0); // There's a once in a blue-moon chance this can fail here.
			// The hashCode creation opportunistically checks whether the object changed,
			// and that throws an exception.
		}
		catch(SentenceNotUpdatedException e)
		{
			updateExceptionCaught = true;
		}
		Assert.assertTrue(updateExceptionCaught);		
	}
	
	@Test
	public void testExceptionNotFiredWithUpdate()
	{
		setLiterals();

		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalOr> elm2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));

		SimpleSentence<LogicalAnd, LogicalOr> sen = mFac.createCNFSentence(Arrays.asList(elm1, elm2));

		Assert.assertTrue(sen.hashCode() != 0); // There's a once in a blue-moon chance this can fail here.
		elm1.removeLiteral(mLa);//modify contained expression w/o telling the sentence.

		sen.update(); // call update this time which sorts out the internal structure
		
		Assert.assertTrue(sen.hashCode() != 0); // There's a once in a blue-moon chance this can fail here.
	}
	
	/**
	 * Test that different ordered CNFs are found as equal. Test also the order of completion
	 * and cache consistency underneath.
	 */
	@Test
	public void testCNFEquality()
	{
		setLiterals();
		Expression<LogicalAnd> elm1 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalAnd> elm2 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		Expression<LogicalAnd> elm3 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLe, mLnf}));
		
		// Equal to self
		{
			SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence(Arrays.asList(elm1, elm2));
			Assert.assertEquals(sen1, sen1);			
		}		
		// Create sentences in reverse expression order
		{
			SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence(Arrays.asList(elm1, elm2));
			SimpleSentence<LogicalOr, LogicalAnd> sen2 = mFac.createDNFSentence(Arrays.asList(elm2, elm1));
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen1, sen2); // check again to see caching behaviour works			
		}
		// Create sentences in reverse expression order
		{
			SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence(Arrays.asList(elm1, elm2));
			SimpleSentence<LogicalOr, LogicalAnd> sen2 = mFac.createDNFSentence(Arrays.asList(elm2, elm1));
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen2, sen1); // check in reverse to see caching behaviour works			
		}
		// Test cached vs not cache sentence
		{
			SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence(Arrays.asList(elm1, elm2, elm3));
			SimpleSentence<LogicalOr, LogicalAnd> sen2 = mFac.createDNFSentence(Arrays.asList(elm2, elm1, elm3));
			SimpleSentence<LogicalOr, LogicalAnd> sen3 = mFac.createDNFSentence(Arrays.asList(elm3, elm1, elm2));
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen1, sen3); // this is the real test here - compared to a third sentence which doesn't have a cache
		}
		// Test cached vs not cache sentence in reverse compare order
		{
			SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence(Arrays.asList(elm1, elm2, elm3));
			SimpleSentence<LogicalOr, LogicalAnd> sen2 = mFac.createDNFSentence(Arrays.asList(elm2, elm1, elm3));
			SimpleSentence<LogicalOr, LogicalAnd> sen3 = mFac.createDNFSentence(Arrays.asList(elm3, elm1, elm2));
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen3, sen1); // reverse check order (the other instance's .equals gets called)
		}
		// Test not equals
		{
			SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence(Arrays.asList(elm1, elm2));
			SimpleSentence<LogicalOr, LogicalAnd> sen2 = mFac.createDNFSentence(Arrays.asList(elm2, elm1));
			SimpleSentence<LogicalOr, LogicalAnd> otherSen = mFac.createDNFSentence(Arrays.asList(elm3, elm1));
			Assert.assertNotSame(sen1, otherSen);
			Assert.assertNotSame(sen1, otherSen); //again
			Assert.assertEquals(sen1, sen2); // still works
			Assert.assertEquals(sen1, sen2);
		}
	}
	
	@Test
	public void testCNFAddWithCacheConsistency()
	{
		setLiterals();
		Expression<LogicalAnd> elm1 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalAnd> elm2 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		Expression<LogicalAnd> elm3 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLe}));
		Expression<LogicalAnd> elm4 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLf}));

		// Adding elements
		{
			SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence(Arrays.asList(elm2, elm1));
			SimpleSentence<LogicalOr, LogicalAnd> sen2 = mFac.createDNFSentence(Arrays.asList(elm1, elm2, elm3));
			sen1.addElement(elm3);

			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen2, sen1);
		}
		{
			SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence(Arrays.asList(elm2, elm1));
			SimpleSentence<LogicalOr, LogicalAnd> sen2 = mFac.createDNFSentence(Arrays.asList(elm1, elm2, elm3));
			sen1.addElement(elm3);

			Assert.assertEquals(sen2, sen1);
			Assert.assertEquals(sen2, sen1);
			Assert.assertEquals(sen1, sen2);
		}
		// Check cache sharing effects
		{
			SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence(Arrays.asList(elm1, elm2, elm3));
			SimpleSentence<LogicalOr, LogicalAnd> sen2 = mFac.createDNFSentence(Arrays.asList(elm2, elm1, elm3));
			SimpleSentence<LogicalOr, LogicalAnd> sen3 = mFac.createDNFSentence(Arrays.asList(elm3, elm1, elm2));
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen2, sen3);
			// Now modify sen1
			sen1.addElement(elm4);
			Assert.assertNotSame(sen1, sen2);
			Assert.assertNotSame(sen1, sen2);
			Assert.assertEquals(sen2, sen3);
		}
		{
			SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence(Arrays.asList(elm1, elm2, elm3));
			SimpleSentence<LogicalOr, LogicalAnd> sen2 = mFac.createDNFSentence(Arrays.asList(elm2, elm1, elm3));
			SimpleSentence<LogicalOr, LogicalAnd> sen3 = mFac.createDNFSentence(Arrays.asList(elm3, elm1, elm2));
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen2, sen3);
			// Now modify sen1
			sen1.addElement(elm4);
			Assert.assertNotSame(sen2, sen1);
			Assert.assertNotSame(sen2, sen1);
			Assert.assertEquals(sen2, sen3);
		}
	}

	// Some further cache sharing scenarios are tested with Add only
	@Test
	public void testCNFRemoveWithCacheConsistency()
	{
		setLiterals();
		Expression<LogicalAnd> elm1 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalAnd> elm2 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		Expression<LogicalAnd> elm3 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLe}));
		
		// Deleting elements
		{
			SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence(Arrays.asList(elm1, elm2, elm3));
			sen1.removeElement(elm3);

			SimpleSentence<LogicalOr, LogicalAnd> sen2 = mFac.createDNFSentence(Arrays.asList(elm2, elm1));
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen2, sen1);
		}
		{
			SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence(Arrays.asList(elm1, elm2, elm3));
			sen1.removeElement(elm3);

			SimpleSentence<LogicalOr, LogicalAnd> sen2 = mFac.createDNFSentence(Arrays.asList(elm2, elm1));
			Assert.assertEquals(sen2, sen1);
			Assert.assertEquals(sen2, sen1);
			Assert.assertEquals(sen1, sen2);
		}
	}

	@Test
	public void testCNFClone()
	{
		setLiterals();

		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc}));
		Expression<LogicalOr> elm2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLnd, mLe, mLnf}));
		
		SimpleSentence<LogicalAnd, LogicalOr> sen = mFac.createCNFSentence();
		Assert.assertTrue(sen.addElement(elm1));
		
		SimpleSentence<LogicalAnd, LogicalOr> senClone = sen.cloneSimpleSentence();
		senClone.addElement(elm2);
		
		// Check that the clone and the original diverge as expected.
		{
			String strClone = mFac.getDefaultStringer().stringise(senClone);
			Assert.assertEquals("[(a + ¬b + ¬c)(¬d + e + ¬f)]", strClone);			
		}
		{
			String str = mFac.getDefaultStringer().stringise(sen);
			Assert.assertEquals("[(a + ¬b + ¬c)]", str);			
		}
		
		// Now modify the underlying expressions to check.
		for(Expression<LogicalOr> ex : sen.getElements())
			ex.addLiteral(mLna);
		sen.update(); // this is needed for consistency

		// Check that only the original changed, not the clone.
		Assert.assertTrue(sen.isTrue());
		{
			String strClone = mFac.getDefaultStringer().stringise(senClone);
			Assert.assertEquals("[(a + ¬b + ¬c)(¬d + e + ¬f)]", strClone);			
		}
	}
	
	
	// TODO: Need to test remove operations (Expression and Sentences)
	@Test
	public void testCreateSimpleDNFSentence()
	{
		setLiterals();

		Expression<LogicalAnd> trm1 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc}));
		Expression<LogicalAnd> trm2 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLnd, mLe, mLnf}));
		
		SimpleSentence<LogicalOr, LogicalAnd> sen = mFac.createDNFSentence();
		Assert.assertTrue(sen.addElement(trm1));
		Assert.assertTrue(sen.addElement(trm2));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[a ¬b ¬c + ¬d e ¬f]", str);
	}
	@Test
	public void testCreatePrototypedDNFSentence()
	{
		setLiterals();

		Expression<LogicalAnd> trm1 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc}));
		
		SimpleSentence<LogicalOr, LogicalAnd> sen = mFac.createDNFSentence();
		Assert.assertTrue(sen.addElement(trm1));
		
		SimpleSentence<LogicalOr, LogicalAnd> senFrom = mFac.createSentenceLike(sen);
		Assert.assertTrue(senFrom.isTrue()); // check that it's empty

		Assert.assertTrue(senFrom.addElement(trm1));

		String str = mFac.getDefaultStringer().stringise(senFrom);
		Assert.assertEquals("[a ¬b ¬c]", str);
	}
	@Test
	public void testCreateEmptyDNFSentence()
	{
		SimpleSentence<LogicalOr, LogicalAnd> sen = mFac.createDNFSentence();

		Assert.assertTrue(sen.isFalse());
		Assert.assertFalse(sen.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mFac.getFalse()}))));
		Assert.assertFalse(sen.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mFac.getFalse()}))));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[False]", str);
	}
	@Test
	public void testCreateLongerDNFSentence()
	{
		setLiterals();

		Expression<LogicalAnd> trm1 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalAnd> trm2 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		Expression<LogicalAnd> trm3 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLe, mLnf}));
		Expression<LogicalAnd> trm4 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLg}));
		
		SimpleSentence<LogicalOr, LogicalAnd> sen = mFac.createDNFSentence();
		Assert.assertTrue(sen.addElement(trm1));
		Assert.assertTrue(sen.addElement(trm2));
		Assert.assertTrue(sen.addElement(trm3));
		Assert.assertTrue(sen.addElement(trm4));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[a ¬b + c ¬d + e ¬f + g]", str);
	}
	@Test
	public void testCreateLongerDNFSentenceWithConstruct()
	{
		setLiterals();

		Expression<LogicalAnd> trm1 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalAnd> trm2 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		Expression<LogicalAnd> trm3 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLe, mLnf}));
		Expression<LogicalAnd> trm4 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLg}));
		
		@SuppressWarnings("unchecked")
		SimpleSentence<LogicalOr, LogicalAnd> sen = 
			mFac.createDNFSentence(new ArrayIterable<Expression<LogicalAnd>>(new Expression[] {trm1, trm2, trm3, trm4}));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[a ¬b + c ¬d + e ¬f + g]", str);
	}
	@Test
	public void testCreateSimplifiableDNFSentence()
	{
		setLiterals();

		// These should all reduce to just 'a ¬b ¬c'
		Expression<LogicalAnd> trm1 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc, mLd, mLne}));
		Expression<LogicalAnd> trm2 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc, mLd}));
		Expression<LogicalAnd> trm3 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc}));
		
		// And this to '¬c d'
		Expression<LogicalAnd> trm4 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLnb, mLnc, mLd}));
		Expression<LogicalAnd> trm5 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLnc, mLd}));

		SimpleSentence<LogicalOr, LogicalAnd> sen = mFac.createDNFSentence();
		Assert.assertTrue(sen.addElement(trm1));
		Assert.assertTrue(sen.addElement(trm2));
		Assert.assertTrue(sen.addElement(trm3));
		Assert.assertTrue(sen.addElement(trm4));
		Assert.assertTrue(sen.addElement(trm5));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[a ¬b ¬c + ¬c d]", str);
	}
	
	@Test
	public void testCreateSimplifiableDNFSentenceReverse()
	{
		setLiterals();

		// These should all reduce to just 'a ¬b ¬c'
		Expression<LogicalAnd> trm1 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc}));
		Expression<LogicalAnd> trm2 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc, mLd}));
		Expression<LogicalAnd> trm3 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc, mLd, mLne}));
		
		// And this to '¬c d'
		Expression<LogicalAnd> trm4 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLnc, mLd}));
		Expression<LogicalAnd> trm5 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLnb, mLnc, mLd}));

		SimpleSentence<LogicalOr, LogicalAnd> sen = mFac.createDNFSentence();
		Assert.assertTrue(sen.addElement(trm1));
		Assert.assertFalse(sen.addElement(trm2));
		Assert.assertFalse(sen.addElement(trm3));
		Assert.assertTrue(sen.addElement(trm4));
		Assert.assertFalse(sen.addElement(trm5));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[a ¬b ¬c + ¬c d]", str);
	}
	@Test
	public void testCreateDNFWithTrue()
	{
		setLiterals();

		Expression<LogicalAnd> trm1 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalAnd> trm2 = mFac.createTerm(); // True
		Expression<LogicalAnd> trm3 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		
		SimpleSentence<LogicalOr, LogicalAnd> sen = mFac.createDNFSentence();
		Assert.assertTrue(sen.addElement(trm1));
		Assert.assertTrue(sen.addElement(trm2)); // True, changes DNF to True
		Assert.assertFalse(sen.addElement(trm3));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[True]", str);
		
		Assert.assertFalse(sen.addElement(trm2));		
	}		
	@Test
	public void testCreateDNFWithFalse()
	{
		setLiterals();

		Expression<LogicalAnd> trm1 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalAnd> trm2 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mFac.getFalse()})); // False
		Expression<LogicalAnd> trm3 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		
		SimpleSentence<LogicalOr, LogicalAnd> sen = mFac.createDNFSentence();
		Assert.assertTrue(sen.addElement(trm1));
		Assert.assertFalse(sen.addElement(trm2)); // False, ineffective
		Assert.assertTrue(sen.addElement(trm3));
		
		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[a ¬b + c ¬d]", str);
		
		Assert.assertFalse(sen.addElement(trm2));		
	}
	// TODO: Should do this same for a term.
	@Test
	public void testCreateMixedClause()
	{
		Literal aa = mFac.createAssumption("a", false, 0.1);
		Literal ana = (Assumption) aa.cloneNegated();
		Literal ab = mFac.createAssumption("b", false, 0.2);
		Literal anb = (Assumption) ab.cloneNegated();
		Literal ac = mFac.createAssumption("c", false, 0.3);
		Literal anc = (Assumption) ac.cloneNegated();
		
		Literal px = mFac.createProposition("x", false);
		Literal pnx = (Proposition) px.cloneNegated();		
		Literal py = mFac.createProposition("y", false);
		Literal pny = (Proposition) py.cloneNegated();		
		Literal pz = mFac.createProposition("z", false);
		Literal pnz = (Proposition) pz.cloneNegated();		
		
		Expression<LogicalOr> cla1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{ana, pnx, py}));
		Expression<LogicalOr> cla2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{anb, pz}));
		Expression<LogicalOr> cla3 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{pny, pnz}));
		Expression<LogicalOr> cla4 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{ac, px}));
		Expression<LogicalOr> cla5 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{anc, pnz}));
		
		@SuppressWarnings("unchecked")
		SimpleSentence<LogicalAnd, LogicalOr> cnfKB = mFac.createCNFSentence(
				new ArrayIterable<Expression<LogicalOr>>(
						new Expression[] {cla1, cla2, cla3, cla4, cla5}));
		
		String str = mFac.getDefaultStringer().stringise(cnfKB);

		Assert.assertEquals("[(¬a + ¬x + y)(¬b + z)(¬y + ¬z)(c + x)(¬c + ¬z)]", str);
	}	
	// TODO: Should test that the clone of a sentence is really a deep copy. 
	// eg. keep a ref to an expression and modify it and check the other sentence.

	/**
	 * Test that different ordered DNFs are found as equal. Test also the order of completion
	 * and cache consistency underneath.
	 */
	@Test
	public void testDNFEquality()
	{
		setLiterals();
		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalOr> elm2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		Expression<LogicalOr> elm3 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLe, mLnf}));
		
		// Equal to self
		{
			SimpleSentence<LogicalAnd, LogicalOr> sen1 = mFac.createCNFSentence(Arrays.asList(elm1, elm2));
			Assert.assertEquals(sen1, sen1);			
		}		
		// Create sentences in reverse expression order
		{
			SimpleSentence<LogicalAnd, LogicalOr> sen1 = mFac.createCNFSentence(Arrays.asList(elm1, elm2));
			SimpleSentence<LogicalAnd, LogicalOr> sen2 = mFac.createCNFSentence(Arrays.asList(elm2, elm1));
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen1, sen2); // check again to see caching behaviour works			
		}
		// Create sentences in reverse expression order
		{
			SimpleSentence<LogicalAnd, LogicalOr> sen1 = mFac.createCNFSentence(Arrays.asList(elm1, elm2));
			SimpleSentence<LogicalAnd, LogicalOr> sen2 = mFac.createCNFSentence(Arrays.asList(elm2, elm1));
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen2, sen1); // check in reverse to see caching behaviour works			
		}
		// Test cached vs not cache sentence
		{
			SimpleSentence<LogicalAnd, LogicalOr> sen1 = mFac.createCNFSentence(Arrays.asList(elm1, elm2, elm3));
			SimpleSentence<LogicalAnd, LogicalOr> sen2 = mFac.createCNFSentence(Arrays.asList(elm2, elm1, elm3));
			SimpleSentence<LogicalAnd, LogicalOr> sen3 = mFac.createCNFSentence(Arrays.asList(elm3, elm1, elm2));
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen1, sen3); // this is the real test here - compared to a third sentence which doesn't have a cache
		}
		// Test cached vs not cache sentence in reverse compare order
		{
			SimpleSentence<LogicalAnd, LogicalOr> sen1 = mFac.createCNFSentence(Arrays.asList(elm1, elm2, elm3));
			SimpleSentence<LogicalAnd, LogicalOr> sen2 = mFac.createCNFSentence(Arrays.asList(elm2, elm1, elm3));
			SimpleSentence<LogicalAnd, LogicalOr> sen3 = mFac.createCNFSentence(Arrays.asList(elm3, elm1, elm2));
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen3, sen1); // reverse check order (the other instance's .equals gets called)
		}
		// Test not equals
		{
			SimpleSentence<LogicalAnd, LogicalOr> sen1 = mFac.createCNFSentence(Arrays.asList(elm1, elm2));
			SimpleSentence<LogicalAnd, LogicalOr> sen2 = mFac.createCNFSentence(Arrays.asList(elm2, elm1));
			SimpleSentence<LogicalAnd, LogicalOr> otherSen = mFac.createCNFSentence(Arrays.asList(elm3, elm1));
			Assert.assertNotSame(sen1, otherSen);
			Assert.assertNotSame(sen1, otherSen); //again
			Assert.assertEquals(sen1, sen2); // still works
			Assert.assertEquals(sen1, sen2);
		}
	}
	
	@Test
	public void testDNFAddWithCacheConsistency()
	{
		setLiterals();
		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalOr> elm2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		Expression<LogicalOr> elm3 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLe}));
		Expression<LogicalOr> elm4 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLf}));

		// Adding elements
		{
			SimpleSentence<LogicalAnd, LogicalOr> sen1 = mFac.createCNFSentence(Arrays.asList(elm2, elm1));
			SimpleSentence<LogicalAnd, LogicalOr> sen2 = mFac.createCNFSentence(Arrays.asList(elm1, elm2, elm3));
			sen1.addElement(elm3);

			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen2, sen1);
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> sen1 = mFac.createCNFSentence(Arrays.asList(elm2, elm1));
			SimpleSentence<LogicalAnd, LogicalOr> sen2 = mFac.createCNFSentence(Arrays.asList(elm1, elm2, elm3));
			sen1.addElement(elm3);

			Assert.assertEquals(sen2, sen1);
			Assert.assertEquals(sen2, sen1);
			Assert.assertEquals(sen1, sen2);
		}
		// Check cache sharing effects
		{
			SimpleSentence<LogicalAnd, LogicalOr> sen1 = mFac.createCNFSentence(Arrays.asList(elm1, elm2, elm3));
			SimpleSentence<LogicalAnd, LogicalOr> sen2 = mFac.createCNFSentence(Arrays.asList(elm2, elm1, elm3));
			SimpleSentence<LogicalAnd, LogicalOr> sen3 = mFac.createCNFSentence(Arrays.asList(elm3, elm1, elm2));
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen2, sen3);
			// Now modify sen1
			sen1.addElement(elm4);
			Assert.assertNotSame(sen1, sen2);
			Assert.assertNotSame(sen1, sen2);
			Assert.assertEquals(sen2, sen3);
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> sen1 = mFac.createCNFSentence(Arrays.asList(elm1, elm2, elm3));
			SimpleSentence<LogicalAnd, LogicalOr> sen2 = mFac.createCNFSentence(Arrays.asList(elm2, elm1, elm3));
			SimpleSentence<LogicalAnd, LogicalOr> sen3 = mFac.createCNFSentence(Arrays.asList(elm3, elm1, elm2));
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen2, sen3);
			// Now modify sen1
			sen1.addElement(elm4);
			Assert.assertNotSame(sen2, sen1);
			Assert.assertNotSame(sen2, sen1);
			Assert.assertEquals(sen2, sen3);
		}
	}

	// Some further cache sharing scenarios are tested with Add only
	@Test
	public void testDNFRemoveWithCacheConsistency()
	{
		setLiterals();
		Expression<LogicalOr> elm1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb}));
		Expression<LogicalOr> elm2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLc, mLnd}));
		Expression<LogicalOr> elm3 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mLe}));
		
		// Deleting elements
		{
			SimpleSentence<LogicalAnd, LogicalOr> sen1 = mFac.createCNFSentence(Arrays.asList(elm1, elm2, elm3));
			sen1.removeElement(elm3);

			SimpleSentence<LogicalAnd, LogicalOr> sen2 = mFac.createCNFSentence(Arrays.asList(elm2, elm1));
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen1, sen2);
			Assert.assertEquals(sen2, sen1);
		}
		{
			SimpleSentence<LogicalAnd, LogicalOr> sen1 = mFac.createCNFSentence(Arrays.asList(elm1, elm2, elm3));
			sen1.removeElement(elm3);

			SimpleSentence<LogicalAnd, LogicalOr> sen2 = mFac.createCNFSentence(Arrays.asList(elm2, elm1));
			Assert.assertEquals(sen2, sen1);
			Assert.assertEquals(sen2, sen1);
			Assert.assertEquals(sen1, sen2);
		}
	}

	@Test
	public void testDNFClone()
	{
		setLiterals();

		Expression<LogicalAnd> trm1 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLa, mLnb, mLnc}));
		Expression<LogicalAnd> trm2 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mLnd, mLe, mLnf}));
		
		SimpleSentence<LogicalOr, LogicalAnd> sen = mFac.createDNFSentence();
		Assert.assertTrue(sen.addElement(trm1));
		
		SimpleSentence<LogicalOr, LogicalAnd> senClone = sen.cloneSimpleSentence();
		senClone.addElement(trm2);
		
		// Test that the clone DNF and the original diverge.
		{
			String strClone = mFac.getDefaultStringer().stringise(senClone);
			Assert.assertEquals("[a ¬b ¬c + ¬d e ¬f]", strClone);			
		}

		String str = mFac.getDefaultStringer().stringise(sen);
		Assert.assertEquals("[a ¬b ¬c]", str);
		
		// Test also the existing expressions diverge as needed.
		for(Expression<LogicalAnd> ex : sen.getElements())
			ex.addLiteral(mLna); // and with ¬a
		sen.update();
		
		Assert.assertTrue(sen.isFalse());

		// Shouldn't have changed.
		{
			String strClone = mFac.getDefaultStringer().stringise(senClone);
			Assert.assertEquals("[a ¬b ¬c + ¬d e ¬f]", strClone);			
		}
	}
}
