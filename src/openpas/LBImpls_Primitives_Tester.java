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
import openpas.basics.Literal.LiteralType;
import openpas.basics.PropFactory;

public class LBImpls_Primitives_Tester {

	final static double DOUBLE_COMPARE_DELTA = 1e-6;

	PropFactory mFac;

	@Before
	public void setUp() throws Exception {
		mFac = new LBImpls.LBImplFactory();
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
	public void testAssumptionClone() {
		Assumption a = mFac.createAssumption("Testo", false, 0.3);
		Assumption aNeg = (Assumption) a.cloneLiteral();
		
		Assert.assertEquals(a.getIndex(), aNeg.getIndex());
		Assert.assertEquals(a.getName(), aNeg.getName());
		Assert.assertEquals(a.getProbability(), aNeg.getProbability(), DOUBLE_COMPARE_DELTA);
		Assert.assertEquals(a.getNeg(), aNeg.getNeg());
	}

	@Test
	public void testAssumptionCloneNegated() {
		Assumption a = mFac.createAssumption("Testo", false, 0.5);
		Assumption aNeg = (Assumption) a.cloneNegated();
		
		Assert.assertEquals(a.getIndex(), aNeg.getIndex());
		Assert.assertEquals(a.getName(), aNeg.getName());
		Assert.assertEquals(a.getProbability(), 1 - aNeg.getProbability(), DOUBLE_COMPARE_DELTA);
		Assert.assertEquals(a.getNeg(), !aNeg.getNeg());
	}

	//TODO: We need to do more testing on Propositions and Special literals.
}

