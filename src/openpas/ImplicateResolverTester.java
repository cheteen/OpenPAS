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

import openpas.basics.Assumption;
import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.Literal;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.PAS;
import openpas.basics.PAS.KBException;
import openpas.basics.PropFactory;
import openpas.basics.Proposition;
import openpas.basics.SymbolicResolver;
import openpas.utils.ArrayIterable;

public class ImplicateResolverTester {
	
	PropFactory mFac;
	PropFactory mPrevFac;
		
	Proposition mPx;
	Proposition mPnx;
	Proposition mPy;
	Proposition mPny;
	Proposition mPz;
	Proposition mPnz;

	Assumption mAa;
	Assumption mAna;
	Assumption mAb;
	Assumption mAnb;
	Assumption mAc;
	Assumption mAnc;

	void setLiterals()
	{
		mAa = mFac.createAssumption("a", false, 0.1);
		mAna = (Assumption) mAa.getNegated();
		mAb = mFac.createAssumption("b", false, 0.2);
		mAnb = (Assumption) mAb.getNegated();
		mAc = mFac.createAssumption("c", false, 0.3);
		mAnc = (Assumption) mAc.getNegated();
		
		mPx = mFac.createProposition("x", false);
		mPnx = (Proposition) mPx.getNegated();		
		mPy = mFac.createProposition("y", false);
		mPny = (Proposition) mPy.getNegated();		
		mPz = mFac.createProposition("z", false);
		mPnz = (Proposition) mPz.getNegated();		
	}
	
	SimpleSentence<LogicalAnd, LogicalOr> getKB1()
	{
		// HKL00, example 3.2 p33
		Expression<LogicalOr> cla1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mAna, mPnx, mPy}));
		Expression<LogicalOr> cla2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mAnb, mPz}));
		Expression<LogicalOr> cla3 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mPny, mPnz}));
		Expression<LogicalOr> cla4 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mAc, mPx}));
		Expression<LogicalOr> cla5 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mAnc, mPnz}));

		@SuppressWarnings("unchecked")
		SimpleSentence<LogicalAnd, LogicalOr> cnfKB = mFac.createCNFSentence(
				new ArrayIterable<Expression<LogicalOr>>(
						new Expression[] {cla1, cla2, cla3, cla4, cla5}));

		return cnfKB;
	}

	@Before
	public void setUp() throws Exception {
		mFac = new LBImpls.LBImplFactory();
		mPrevFac = LBImpls.setTestFactory(mFac);
	}

	@After
	public void tearDown() throws Exception {
		// re-instantiate existing factory
		LBImpls.setTestFactory(mPrevFac);
	}
	
	@Test
	public void testResolveQSClause() throws KBException
	{
		setLiterals();
		SimpleSentence<LogicalAnd, LogicalOr> kbCNF = getKB1();
		PAS pas = new PASImpl(kbCNF, mFac); 
		ImplicateResolver ir = new ImplicateResolver(kbCNF, pas.getPropositions(), pas.getAssumptions(), mFac);
		
		Expression<LogicalOr> hclause = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mPy}));

		SimpleSentence<LogicalOr, LogicalAnd> qs = ir.findQS(hclause);
//		System.out.println(qs);
		
		String str = mFac.getDefaultStringer().stringise(qs);
		Assert.assertEquals("[b c + a ¬c + a b]", str);
	}
	@Test
	public void testResolveQSContradiction() throws KBException
	{
		setLiterals();
		SimpleSentence<LogicalAnd, LogicalOr> kbCNF = getKB1();
		PAS pas = new PASImpl(kbCNF, mFac);
		ImplicateResolver ir = new ImplicateResolver(kbCNF, pas.getPropositions(), pas.getAssumptions(), mFac);
		
		Expression<LogicalOr> hclause = mFac.createClause(); // empty clause for contradictions in kb

		SimpleSentence<LogicalOr, LogicalAnd> qs = ir.findQS(hclause);
//		System.out.println(qs);
	
		String str = mFac.getDefaultStringer().stringise(qs);
		Assert.assertEquals("[b c + a b]", str);			
	}
	
	@Test
	public void testComplement()
	{
		setLiterals();
	
		SimpleSentence<LogicalOr, LogicalAnd> sen = mFac.createDNFSentence();
		sen.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mAb, mAc})));
		sen.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mAa, mAb})));		
		
		// sen = [b c + a b] = b ( a + c) = ¬b + ¬a ¬c
		SimpleSentence<LogicalOr, LogicalAnd> comp = ImplicateResolver.calcComplement(mFac, sen);

		String str = mFac.getDefaultStringer().stringise(comp);
		Assert.assertEquals("[¬b + ¬a ¬c]", str);			
	}
	
	@Test
	public void testTruesComplement()
	{
		setLiterals();
	
		SimpleSentence<LogicalOr, LogicalAnd> sen = mFac.getTrueDNF();
		SimpleSentence<LogicalOr, LogicalAnd> comp = ImplicateResolver.calcComplement(mFac, sen);

		Assert.assertTrue(comp.isFalse());	
	}

	@Test
	public void testFalsesComplement()
	{
		setLiterals();
	
		SimpleSentence<LogicalOr, LogicalAnd> sen = mFac.getFalseDNF();
		SimpleSentence<LogicalOr, LogicalAnd> comp = ImplicateResolver.calcComplement(mFac, sen);

		Assert.assertTrue(comp.isTrue());	
	}

	@Test
	public void testIntersection()
	{
		setLiterals();
	
		//(¬b)+(¬a ¬c)
		SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence();
		sen1.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mAnb})));
		sen1.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mAna, mAnc})));		

		//(b c)+(a ¬c)+(a b)
		SimpleSentence<LogicalOr, LogicalAnd> sen2 = mFac.createDNFSentence();
		sen2.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mAb, mAc})));
		sen2.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mAa, mAnc})));		
		sen2.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mAa, mAb})));		
		
		SimpleSentence<LogicalOr, LogicalAnd> insec = ImplicateResolver.calcIntersection(mFac, sen1, sen2);

		String str = mFac.getDefaultStringer().stringise(insec);
		Assert.assertEquals("[a ¬b ¬c]", str);			
	}
	
	
	@Test
	public void testTruesIntersection()
	{
		setLiterals();
	
		//(¬b)+(¬a ¬c)
		SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence();
		sen1.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mAnb})));
		sen1.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mAna, mAnc})));		

		//True
		SimpleSentence<LogicalOr, LogicalAnd> sen2 = mFac.getTrueDNF();
		
		{
			SimpleSentence<LogicalOr, LogicalAnd> insec = ImplicateResolver.calcIntersection(mFac, sen1, sen2);
			String str = mFac.getDefaultStringer().stringise(insec);
			Assert.assertEquals("[¬b + ¬a ¬c]", str);						
		}

		{
			// Same but reverse order
			SimpleSentence<LogicalOr, LogicalAnd> insec = ImplicateResolver.calcIntersection(mFac, sen2, sen1);
			String str = mFac.getDefaultStringer().stringise(insec);
			Assert.assertEquals("[¬b + ¬a ¬c]", str);						
		}
	}

	@Test
	public void testFalsesIntersection()
	{
		setLiterals();
	
		//(¬b)+(¬a ¬c)
		SimpleSentence<LogicalOr, LogicalAnd> sen1 = mFac.createDNFSentence();
		sen1.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mAnb})));
		sen1.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{mAna, mAnc})));		

		//True
		SimpleSentence<LogicalOr, LogicalAnd> sen2 = mFac.getFalseDNF();
		
		{
			SimpleSentence<LogicalOr, LogicalAnd> insec = ImplicateResolver.calcIntersection(mFac, sen1, sen2);
			Assert.assertTrue(insec.isFalse());
		}

		{
			// Same but reverse order
			SimpleSentence<LogicalOr, LogicalAnd> insec = ImplicateResolver.calcIntersection(mFac, sen2, sen1);
			Assert.assertTrue(insec.isFalse());
		}
	}

	@Test
	public void testResolveQSCNF() throws KBException
	{
		setLiterals();
		SimpleSentence<LogicalAnd, LogicalOr> kbCNF = getKB1();
		PAS pas = new PASImpl(kbCNF, mFac);
		SymbolicResolver ir = new ImplicateResolver(kbCNF, pas.getPropositions(), pas.getAssumptions(), mFac);
		
		SimpleSentence<LogicalAnd, LogicalOr> hypothesis = mFac.createCNFSentence();
		hypothesis.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mPy})));

		SimpleSentence<LogicalOr, LogicalAnd> qs = ir.findQS(hypothesis);
//		System.out.println(qs);
		
		String str = mFac.getDefaultStringer().stringise(qs);
		Assert.assertEquals("[b c + a ¬c + a b]", str);
	}
	@Test
	public void testResolveSPCNF() throws KBException
	{
		setLiterals();
		SimpleSentence<LogicalAnd, LogicalOr> kbCNF = getKB1();
		PAS pas = new PASImpl(kbCNF, mFac);
		SymbolicResolver ir = new ImplicateResolver(kbCNF, pas.getPropositions(), pas.getAssumptions(), mFac);
		
		SimpleSentence<LogicalAnd, LogicalOr> hypothesis = mFac.createCNFSentence();
		hypothesis.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mPy})));

		SimpleSentence<LogicalOr, LogicalAnd> sp = ir.findSP(hypothesis);
//		System.out.println(sp);
		
		String str = mFac.getDefaultStringer().stringise(sp);
		Assert.assertEquals("[a ¬b ¬c]", str);			
	}
}

