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
import openpas.basics.NumericResolver;
import openpas.basics.PAS;
import openpas.basics.PAS.KBException;
import openpas.basics.ProbabilityComputer;
import openpas.basics.PropFactory;
import openpas.basics.Proposition;
import openpas.basics.SymbolicResolver;
import openpas.utils.ArrayIterable;

public abstract class NumericResolverImpResAbstractTester {
	
	final static double DOUBLE_COMPARE_DELTA = 1e-6;

	PropFactory mFac;
	PropFactory mPrevFac;
	
	Proposition mPv;
	Proposition mPnv;
	Proposition mPw;
	Proposition mPnw;
	Proposition mPx;
	Proposition mPnx;
	Proposition mPy;
	Proposition mPny;

	Assumption mAa;
	Assumption mAna;
	double mPra;
	Assumption mAb;
	Assumption mAnb;
	double mPrb;
	Assumption mAc;
	Assumption mAnc;
	double mPrc;
	Assumption mAd;
	Assumption mAnd;
	double mPrd;
	
	abstract SymbolicResolver createSymResolver(PAS kb);
	abstract ProbabilityComputer createProbabilityComputer();
	
	void setLiterals()
	{
		mPra = 0.1;
		mAa = mFac.createAssumption("a", false, mPra);
		mAna = (Assumption) mAa.cloneNegated();
		mPrb = 0.2;
		mAb = mFac.createAssumption("b", false, mPrb);
		mAnb = (Assumption) mAb.cloneNegated();
		mPrc = 0.3;
		mAc = mFac.createAssumption("c", false, mPrc);
		mAnc = (Assumption) mAc.cloneNegated();
		mPrd = 0.4;
		mAd = mFac.createAssumption("d", false, mPrd);
		mAnd = (Assumption) mAd.cloneNegated();
		
		mPv = mFac.createProposition("v", false);
		mPnv = (Proposition) mPv.cloneNegated();		
		mPx = mFac.createProposition("x", false);
		mPnx = (Proposition) mPx.cloneNegated();		
		mPy = mFac.createProposition("y", false);
		mPny = (Proposition) mPy.cloneNegated();		
		mPw = mFac.createProposition("w", false);
		mPnw = (Proposition) mPw.cloneNegated();		
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
	public void testSimpleAnd() throws KBException
	{
		setLiterals();
		
		// kb: (a b) -> v
		SimpleSentence<LogicalAnd, LogicalOr> cnf = mFac.createCNFSentence();
		cnf.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mAna, mAnb, mPv})));
		PAS kb = new PASImpl(cnf, mFac);

		// h: v
		SimpleSentence<LogicalAnd, LogicalOr> hypo = mFac.createCNFSentence();
		hypo.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mPv})));
		
		NumericResolver nr = new NumericResolverImpl(createSymResolver(kb), createProbabilityComputer(), mFac);
		double dqs = nr.calcDQS(hypo);
		double expected = mPra * mPrb;
		
		Assert.assertEquals(expected, dqs, DOUBLE_COMPARE_DELTA);
	}
	
	@Test
	public void testSimpleOr() throws KBException
	{
		setLiterals();
		
		// kb: (a + b) -> v = (¬a + v)(¬b + v) = (a -> v)(b -> v)
		SimpleSentence<LogicalAnd, LogicalOr> cnf = mFac.createCNFSentence();
		cnf.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mAna, mPv})));
		cnf.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mAnb, mPv})));
		PAS kb = new PASImpl(cnf, mFac);

		// h: v
		SimpleSentence<LogicalAnd, LogicalOr> hypo = mFac.createCNFSentence();
		hypo.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mPv})));
		
		NumericResolver nr = new NumericResolverImpl(createSymResolver(kb), createProbabilityComputer(), mFac);
		double dqs = nr.calcDQS(hypo);
		
		double expect = 1-(1-mPra)*(1-mPrb);
		Assert.assertEquals(expect, dqs, DOUBLE_COMPARE_DELTA);
	}

	@Test
	public void testAndExpressions() throws KBException
	{
		setLiterals();
		
		// kb: (a b c)+(d) -> v = ((a b c)-> v)(d -> v) = (¬a + ¬b + ¬c + v)(¬d + v)
		SimpleSentence<LogicalAnd, LogicalOr> cnf = mFac.createCNFSentence();
		cnf.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mAna, mAnb, mAnc, mPv})));
		cnf.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mAnd, mPv})));
		PAS kb = new PASImpl(cnf, mFac);

		// h: v
		SimpleSentence<LogicalAnd, LogicalOr> hypo = mFac.createCNFSentence();
		hypo.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mPv})));
		
		NumericResolver nr = new NumericResolverImpl(createSymResolver(kb), createProbabilityComputer(), mFac);

		// qs: (a b c) + d
		double dqs = nr.calcDQS(hypo);
		
		double expected = 1 - (1 - mPra * mPrb * mPrc)*(1 - mPrd);
		
		Assert.assertEquals(expected, dqs, DOUBLE_COMPARE_DELTA);
	}
	
	@Test
	public void testLongHypothesis() throws KBException
	{
		setLiterals();
		
		// kb: (a -> v)+(b -> w)+(c -> x)
		SimpleSentence<LogicalAnd, LogicalOr> cnf = mFac.createCNFSentence();
		cnf.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mAna, mPv})));
		cnf.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mAnb, mPw})));
		cnf.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mAnc, mPx})));
		PAS kb = new PASImpl(cnf, mFac);

		// h: v + w + x
		SimpleSentence<LogicalAnd, LogicalOr> hypo = mFac.createCNFSentence();
		hypo.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{mPv, mPw, mPx})));
		
		NumericResolver nr = new NumericResolverImpl(createSymResolver(kb), createProbabilityComputer(), mFac);

		// qs: a + b + c
		double dqs = nr.calcDQS(hypo);
		
		double expected = 1 - (1 - mPra) * (1 - mPrb) * (1 - mPrc);
		
		Assert.assertEquals(expected, dqs, DOUBLE_COMPARE_DELTA);
	}
	// TODO: Need to test SP and normalised SP etc. Need contradictory kb.
	
	@Test
	public void testSentenceWithIndependentAssumptions()
	{
		double prax1 = 0.1;
		double prax2 = 0.2;
		double prax3 = 0.3;
		double prax4 = 0.4;
		double prax5 = 0.5;
		double prax6 = 0.6;
		double prax7 = 0.7;
		double prax8 = 0.8;
		
		Assumption ax1 = mFac.createAssumption("x1", false, prax1);
		Assumption ax2 = mFac.createAssumption("x2", false, prax2);
		Assumption ax3 = mFac.createAssumption("x3", false, prax3);
		Assumption ax4 = mFac.createAssumption("x4", false, prax4);
		Assumption ax5 = mFac.createAssumption("x5", false, prax5);
		Assumption ax6 = mFac.createAssumption("x6", false, prax6);
		Assumption ax7 = mFac.createAssumption("x7", false, prax7);
		Assumption ax8 = mFac.createAssumption("x8", false, prax8);		

		SimpleSentence<LogicalOr, LogicalAnd> dnf = mFac.createDNFSentence();
		dnf.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{ax1, ax2})));
		dnf.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{ax3, ax4})));
		dnf.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{ax5, ax6})));
		dnf.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{ax7, ax8})));
		
		double expected = 1 - (1 - prax1 * prax2) * (1 - prax3 * prax4) * (1 - prax5 * prax6) * (1 - prax7 * prax8);
		double pr = createProbabilityComputer().computeDNFProbability(dnf);
		
		Assert.assertEquals(expected, pr, DOUBLE_COMPARE_DELTA);
	}
	
	@Test
	public void testContradictorySupport() throws KBException
	{
		// A -> x, B -> ¬x, h = x
		//
		//  A	B	x	   pr
		//  0	0 	P      0.32
		//  0	1	R      0.08
		//  1	0   QS     0.48
		//	1	1 	QS,I   0.12
		//
		// P: possibly supporting, R: refuting, S: supporting, I: inconsistent
		//
		// dqs(QS_I) = dqs(I_A) = dqs(\bot) = 0.12
		// dqs(QS_C) = dqs(C_A) = dqs(\top) = 0.88
		// dqs(h) = 0.48 + 0.12 = 0.6
		// dsp(h) = {dqs(h) - dqs(\bot)} / {1 - dqs(\bot)}
		//        = {0.6 - 0.12} / {1 - 0.12}
		//		  = 0.48 / 0.88 = 0.545
		Assumption aA = mFac.createAssumption("A", false, 0.6);
		Assumption anA = (Assumption) aA.cloneNegated();
		Assumption aB = mFac.createAssumption("B", false, 0.2);
		Assumption anB = (Assumption) aB.cloneNegated();

		Proposition pX = mFac.createProposition("x", false);
		Proposition pnX = (Proposition) pX.cloneNegated();
		
		Expression<LogicalOr> clause1 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{anA, pX}));
		Expression<LogicalOr> clause2 = mFac.createClause(new ArrayIterable<Literal>(new Literal[]{anB, pnX}));
		
		SimpleSentence<LogicalAnd, LogicalOr> cnfKB = mFac.createCNFSentence();
		cnfKB.addElement(clause1);
		cnfKB.addElement(clause2);
		
		SimpleSentence<LogicalAnd, LogicalOr> cnfH = mFac.createCNFSentence();
		Expression<LogicalOr> clH = mFac.createClause();
		clH.addLiteral(pX);
		cnfH.addElement(clH);

		PAS kb = new PASImpl(cnfKB, mFac); // Use a PAS instance to extract the props and assumptions.

		NumericResolver nr = new NumericResolverImpl(createSymResolver(kb), createProbabilityComputer(), mFac);
		double dqsH = nr.calcNormalisedDSP(cnfH);
		Assert.assertEquals(0.545, dqsH, 1e-3);
		Assert.assertEquals(0.12, nr.calcDQS_I(), 1e-3);
	}

	@Test
	public void testRai1995Fig1()
	{
		// Rai1995A Survey of Efficient Reliability Computation Using Disjoint Products Approach
		// Fig. 1
		// SP_{ST} = ab + cd + aed + ceb
		//
		Assumption a_a = mFac.createAssumption("a", false, 0.9);
		Assumption a_b = mFac.createAssumption("b", false, 0.9);
		Assumption a_c = mFac.createAssumption("c", false, 0.9);
		Assumption a_d = mFac.createAssumption("d", false, 0.9);
		Assumption a_e = mFac.createAssumption("e", false, 0.9);
		
		SimpleSentence<LogicalOr, LogicalAnd> dnf = mFac.createDNFSentence();
		dnf.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{a_a, a_b})));
		dnf.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{a_c, a_d})));
		dnf.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{a_a, a_e, a_d})));
		dnf.addElement(mFac.createTerm(new ArrayIterable<Literal>(new Literal[]{a_c, a_e, a_b})));
		
		double pr = createProbabilityComputer().computeDNFProbability(dnf);
		
		//Answer as seen on p150:
		Assert.assertEquals(0.97848, pr, 1e-5);
	}
	
	// Need to keep this here as a valid sym+num example, and take 1 to a prob computer dedicated test.
	@Test
	public void testRai1995Fig1_take2() throws KBException
	{
		// Rai1995A Survey of Efficient Reliability Computation Using Disjoint Products Approach
		// Fig. 1
		// Below is a re-write of the network in Fig.1 as follows:
		//          U
		//    -a->  >  -b->
		// S        e       T
		//    -c->  <  -d->
		//          D 
		// where nodes U and D are introduced where they weren't defined before.
		// The sym resolver walks through the links and works out the paths itself.
		Assumption a_a = mFac.createAssumption("a", false, 0.9);
		Assumption a_na = (Assumption) a_a.cloneNegated();		
		Assumption a_b = mFac.createAssumption("b", false, 0.9);
		Assumption a_nb = (Assumption) a_b.cloneNegated();		
		Assumption a_c = mFac.createAssumption("c", false, 0.9);
		Assumption a_nc = (Assumption) a_c.cloneNegated();		
		Assumption a_d = mFac.createAssumption("d", false, 0.9);
		Assumption a_nd = (Assumption) a_d.cloneNegated();		
		Assumption a_e = mFac.createAssumption("e", false, 0.9);
		Assumption a_ne = (Assumption) a_e.cloneNegated();
		
		Proposition p_S = mFac.createProposition("S", false);
		Proposition p_nS = (Proposition) p_S.cloneNegated();
		Proposition p_T = mFac.createProposition("T", false);
		@SuppressWarnings("unused")
		Proposition p_nT = (Proposition) p_T.cloneNegated();
		Proposition p_U = mFac.createProposition("U", false);
		Proposition p_nU = (Proposition) p_U.cloneNegated();
		Proposition p_D = mFac.createProposition("D", false);
		Proposition p_nD = (Proposition) p_D.cloneNegated();
		
		SimpleSentence<LogicalAnd, LogicalOr> cnf = mFac.createCNFSentence();
		// S a -> U = ~S + ~a + U
		cnf.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{p_nS, a_na, p_U})));
		// S c -> D = ~S + ~c + D
		cnf.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{p_nS, a_nc, p_D})));
		// U e -> D = ~U + ~e + D
		cnf.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{p_nU, a_ne, p_D})));
		// D e -> U = ~D + ~e + U
		cnf.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{p_nD, a_ne, p_U})));
		// U b -> T = ~U + ~b + T
		cnf.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{p_nU, a_nb, p_T})));
		// D d -> T = ~D + ~d + T
		cnf.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{p_nD, a_nd, p_T})));

		PAS pas = new PASImpl(cnf, mFac);
		
		NumericResolver nr = new NumericResolverImpl(createSymResolver(pas), createProbabilityComputer(), mFac);

		//h = S -> T = ~S + T
		SimpleSentence<LogicalAnd, LogicalOr> hypo = mFac.createCNFSentence();
		hypo.addElement(mFac.createClause(new ArrayIterable<Literal>(new Literal[]{p_nS, p_T})));
		// An alternative would be to add S as part of the kb and resolve for T.
		
		double dqs = nr.calcDQS(hypo);
		
		//Answer as seein on p150:
		Assert.assertEquals(0.97848, dqs, 1e-5);
	}
}
