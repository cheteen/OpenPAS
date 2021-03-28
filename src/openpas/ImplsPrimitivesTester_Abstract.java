//    Copyright (c) 2017, 2021 Burak Cetin
//
//    This file is part of OpenPAS.
//
//    OpenPAS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    OpenPAS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with OpenPAS.  If not, see <https://www.gnu.org/licenses/>.

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

