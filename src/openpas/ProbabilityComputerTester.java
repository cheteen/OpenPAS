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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import openpas.basics.Assumption;
import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.ProbabilityComputer;
import openpas.basics.PropFactory;
import openpas.basics.Proposition;

public class ProbabilityComputerTester {

	PropFactory mFac;
	PropFactory mPrevFac;

	final static double DOUBLE_COMPARE_DELTA = 1e-6;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
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
	public void testInvalidDNF()
	{
		Proposition p = mFac.createProposition("prop1", false);
		Assumption a = mFac.createAssumption("asm", false, 0.5);
		
		// Create a DNF that has a proposition and an assumption in it.
		SimpleSentence<LogicalOr, LogicalAnd> dnf = mFac.createDNFSentence(Arrays.asList(mFac.createTerm(Arrays.asList(a, p))));
		
		comparePCs(dnf, Double.NaN);
	}

	@Test
	public void testSpecialCases()
	{
		comparePCs(mFac.getFalseDNF(), 0.0);
		comparePCs(mFac.getTrueDNF(), 1.0);
	}
	
	@Test
	public void testLongIndependentDNF() {
		// Sentences of the type:
		// A + B + C + D + ... up to n elements
		Assumption assumptions[] = new Assumption[10];// Adjust here for longer terms
		String start = "A";
		int startVal = start.charAt(0);
		for(int i = 0; i < assumptions.length; ++i)
		{
			String aname = String.valueOf( (char) (startVal + i));
			assumptions[i] = mFac.createAssumption(aname, false, i * 1.0f / assumptions.length + 0.5f / assumptions.length);
		}
		
		for(int i = 1; i < assumptions.length; ++i)
		{
			List<Expression<LogicalAnd>> terms = new ArrayList<>();
			for(int j = 0; j < i; ++j)
				terms.add(mFac.createTerm(Arrays.asList(assumptions[j])));
			comparePCs(mFac.createDNFSentence(terms));
		}
	}

	@Test
	public void testLongAndWideIndependentDNF() {
		// Sentences of the type AA AB .. BA CA + ...
		
		// Let's get 100 assumptions that we'll mix and match
		Assumption assumptions[][] = new Assumption[10][10]; // Adjust here for longer terms
		String start = "A";
		int startVal = start.charAt(0);
		int numAsm = assumptions.length * assumptions[0].length;
		for(int i = 0; i < assumptions.length; ++i)
		{
			for(int j = 0; j < assumptions[0].length; ++j)
			{
				String aname = String.valueOf( (char) (startVal + i)) + String.valueOf( (char) (startVal + j));
				int index = i * assumptions[0].length + j;
				assumptions[i][j] = mFac.createAssumption(aname, false, index * 1.0f / numAsm + 0.5f / numAsm);				
			}
		}
		
		// This traverses the edge of a square progressively getting smaller.
		SimpleSentence<LogicalOr, LogicalAnd> dnf = mFac.createDNFSentence();
		for(int i = 0; i < assumptions.length; ++i)
		{
			Expression<LogicalAnd> term = mFac.createTerm();
			for(int j = i; j < assumptions.length; ++j)
				term.addLiteral(assumptions[i][j]);
			for(int j = i; j < assumptions.length; ++j)
				term.addLiteral(assumptions[j][i]);
			dnf.addElement(term);
		}
		comparePCs(dnf);
	}
	
	@Test
	public void testDNFWithNegatedTerms()
	{
		Assumption aA = mFac.createAssumption("A", false, 0.1);
		Assumption aNA = (Assumption) aA.cloneNegated();

		Assumption aB = mFac.createAssumption("B", false, 0.2);
		Assumption aNB = (Assumption) aB.cloneNegated();
		
		Assumption aC = mFac.createAssumption("C", false, 0.3);
		Assumption aNC = (Assumption) aC.cloneNegated();

		// Simple case: A + ¬A = True
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = mFac.createDNFSentence();
			dnf.addElement(mFac.createTerm(Arrays.asList(aA)));
			dnf.addElement(mFac.createTerm(Arrays.asList(aNA)));
			
			comparePCs(dnf, 1.0);
		}
		
		// Add more variety: A + ¬A ¬B
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = mFac.createDNFSentence();
			dnf.addElement(mFac.createTerm(Arrays.asList(aA)));
			dnf.addElement(mFac.createTerm(Arrays.asList(aNB, aNA)));
			
			comparePCs(dnf);			
		}
		
		// Just neg A
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = mFac.createDNFSentence();
			dnf.addElement(mFac.createTerm(Arrays.asList(aNA)));
			
			comparePCs(dnf, 0.9);
		}

		// neg A neg B
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = mFac.createDNFSentence();
			dnf.addElement(mFac.createTerm(Arrays.asList(aNA, aNB)));
			
			comparePCs(dnf, 0.9 * 0.8);
		}

		// All negs: ¬A ¬B + ¬C
		{
			SimpleSentence<LogicalOr, LogicalAnd> dnf = mFac.createDNFSentence();
			dnf.addElement(mFac.createTerm(Arrays.asList(aNA, aNB)));
			dnf.addElement(mFac.createTerm(Arrays.asList(aNC)));
			
			comparePCs(dnf);			
		}
	}
	
	@Test
	public void testInclusionExclusion()
	{
		// Test that for the independent terms of the type:
		// dnf = A B C + D E F + ...
		// the probabilities are correct.
		// We can get the correct result here as:
		// p(dnf) = 1 - (1 - p_A p_B pC) (1 - p_D p_E pF) ...
		// by repeatedly applying the inclusion-exclusion rule:
		// p(T_A + T_B) = 1 - (1 - p_TA)(1 - p_TB)
		// if T_A and T_B are stochastically independent (which in our case means disjunct terms).
		
		// Create 3*7 = 21 assumptions
		// A , B , C , D , ...
		Assumption assumptions[] = new Assumption[21];
		String start = "A";
		int startVal = start.charAt(0);
		for(int i = 0; i < assumptions.length; ++i)
		{
			String aname = String.valueOf( (char) (startVal + i));
			assumptions[i] = mFac.createAssumption(aname, false, i * 1.0f / assumptions.length + 0.5f / assumptions.length);
		}
		
		// Create 7 terms each with 3 consecutive assumptions:
		SimpleSentence<LogicalOr, LogicalAnd> dnf = mFac.createDNFSentence();
		double resultIE = 0;
		for(int i = 0; i < assumptions.length; i += 3)
		{
			Expression<LogicalAnd> term = mFac.createTerm(Arrays.asList(assumptions[i], assumptions[i + 1], assumptions[i + 2]));
			dnf.addElement(term);

			// Apply the inclusion-exclusion rule:
			resultIE = 1 - (1- resultIE) * (1- term.computeProbability());
		}
		
		comparePCs(dnf, resultIE);		
	}
	
	@Test
	public void testLongAndIntertwinedDNF() {
		doTestLongAndIntertwinedDNF(false);
	}
	@Test
	public void testLongAndIntertwinedDNFWithNegation() {
		doTestLongAndIntertwinedDNF(true);
	}

	public void doTestLongAndIntertwinedDNF(boolean doNegation) {
		// Let's get n^2 assumptions that we'll overlay on each
		// This will add terms of size n where half as many literals are shared with the next term.
		int numAsmRoot = 6; // Adjust here for longer terms
		Assumption assumptions[] = new Assumption[numAsmRoot * numAsmRoot];
		String start = "A";
		int startVal = start.charAt(0);
		for(int i = 0; i < numAsmRoot; ++i)
		{
			for(int j = 0; j < numAsmRoot; ++j)
			{
				String aname = String.valueOf( (char) (startVal + i)) + String.valueOf( (char) (startVal + j));
				int index = i * numAsmRoot + j;
				assumptions[index] = mFac.createAssumption(aname, false, index * 1.0f / assumptions.length + 0.5f / assumptions.length);				
			}
		}
		
		SimpleSentence<LogicalOr, LogicalAnd> dnf = mFac.createDNFSentence();
		for(int i = 0; i < numAsmRoot * 2; ++i)
		{
			Expression<LogicalAnd> term = mFac.createTerm();
			int termStart = i * numAsmRoot / 2;
			for(int j = termStart; j < termStart + numAsmRoot; ++j)
			{
				// If using inversion negate every other literal in every other term
				if(doNegation && i % 2 == 1)
					term.addLiteral(j % 2 == 0 ? 	assumptions[j % assumptions.length] 
												  :	assumptions[j % assumptions.length].cloneNegated());
				else
					term.addLiteral(assumptions[j % assumptions.length]);
			}
			dnf.addElement(term);
		}
		comparePCs(dnf);
	}

	private void comparePCs(SimpleSentence<LogicalOr, LogicalAnd> dnf)
	{
		comparePCs(dnf, null);
	}

	private void comparePCs(SimpleSentence<LogicalOr, LogicalAnd> dnf, Double expected)
	{
		ProbabilityComputer pcBDD = new ProbabilityComputer_BDD(1024);
		double resBDD = pcBDD.computeDNFProbability(dnf);
		if(expected != null)
			Assert.assertEquals(expected, resBDD, DOUBLE_COMPARE_DELTA);
		
		ProbabilityComputer pcSPX = new ProbabilityComputer_SPExpansion(mFac);
		double resSPX = pcSPX.computeDNFProbability(dnf);
		if(expected != null)
			Assert.assertEquals(expected, resSPX, DOUBLE_COMPARE_DELTA);
		
		// Check with each other if we don't know what to expect
		if(expected == null)
			Assert.assertEquals(resBDD, resSPX, DOUBLE_COMPARE_DELTA);
	}
}
