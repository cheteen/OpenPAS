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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import openpas.basics.Assumption;
import openpas.basics.Literal;
import openpas.basics.Literal.LiteralType;
import openpas.basics.PropFactory;
import openpas.basics.Proposition;

public abstract class ImplsPrimitivesTester_Abstract {

	final static double DOUBLE_COMPARE_DELTA = 1e-6;

	PropFactory mFac;

	abstract PropFactory createFactory();
	
	@Before
	public void setUp() throws Exception {
		mFac = createFactory();
	}
	
	@Test
	public void testAssumptionCreation() {
		String name = "Testo";
		double probability = 0.2;
		Assumption a = mFac.createAssumption(name, false, probability);
		Assumption aB = mFac.createAssumption("Testo2", true, probability);
		
		Assert.assertEquals(name, a.getName());
		Assert.assertEquals(probability, a.getProbability(), DOUBLE_COMPARE_DELTA);
		Assert.assertEquals(false, a.getNeg());
		Assert.assertEquals(LiteralType.Assumption, a.getType());
		
		Assert.assertNotEquals(aB.getIndex(), a.getIndex());
		Assert.assertEquals(true, aB.getNeg());
	}

	@Test
	public void testAssumptionCloneNegated() {
		Assumption lit = mFac.createAssumption("Testo", false, 0.5);
		Assumption litNeg = (Assumption) lit.getNegated();
		
		Assert.assertEquals(lit.getIndex(), litNeg.getIndex());
		Assert.assertEquals(lit.getName(), litNeg.getName());
		Assert.assertEquals(lit.getProbability(), 1 - litNeg.getProbability(), DOUBLE_COMPARE_DELTA);
		Assert.assertEquals(lit.getNeg(), !litNeg.getNeg());
	}

	@Test
	public void testPropositionCreation() {
		String name = "Zitto";
		Proposition lit = mFac.createProposition(name, false);
		Proposition litB = mFac.createProposition("Kesto", true);
		
		Assert.assertEquals(name, lit.getName());
		Assert.assertEquals(false, lit.getNeg());
		Assert.assertEquals(LiteralType.Proposition, lit.getType());
		
		Assert.assertNotEquals(litB.getIndex(), lit.getIndex());
		Assert.assertEquals(true, litB.getNeg());
	}
	
	@Test
	public void testPropositionCloneNegated() {
		Proposition lit = mFac.createProposition("Testo", false);
		Proposition litNeg = (Proposition) lit.getNegated();
		
		Assert.assertEquals(lit.getIndex(), litNeg.getIndex());
		Assert.assertEquals(lit.getName(), litNeg.getName());
		Assert.assertEquals(lit.getNeg(), !litNeg.getNeg());
	}

	@Test
	public void testSpecials() {
		Literal litTrue = mFac.getTrue();
		Assert.assertTrue(!litTrue.getNeg());
		Assert.assertEquals(litTrue.getName(), mFac.getDefaultSymboliser().getTrue());
	
		Literal litFalse = mFac.getFalse();
		Assert.assertTrue(litFalse.getNeg());
		Assert.assertEquals(litFalse.getName(), mFac.getDefaultSymboliser().getFalse());
		
		Assert.assertEquals(litTrue.getIndex(), litFalse.getIndex());
	}

	@Test
	public void testSpecialsNegation() {
		Literal litNotFalse = mFac.getFalse().getNegated();
		Assert.assertTrue(!litNotFalse.getNeg());
		Assert.assertEquals(litNotFalse.getName(), mFac.getDefaultSymboliser().getTrue());
	
		Literal litNotTrue = mFac.getTrue().getNegated();
		Assert.assertTrue(litNotTrue.getNeg());
		Assert.assertEquals(litNotTrue.getName(), mFac.getDefaultSymboliser().getFalse());
		
		Assert.assertEquals(litNotFalse.getIndex(), mFac.getTrue().getIndex());
		Assert.assertEquals(litNotTrue.getIndex(), mFac.getFalse().getIndex());
	}
}

