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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import openpas.basics.Expressions.Expression;
import openpas.basics.Literal;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.PropFactory;
import openpas.utils.ArrayIterable;

public abstract class ImplsOperationsTester_Abstract {	
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

	abstract PropFactory createFactory();
	
	@Before
	public void setUp() throws Exception {
		mFac = createFactory();
	}

	@After
	public void tearDown() throws Exception {
	}
	
	void setLiterals()
	{
		mLa = mFac.createProposition("a", false);
		mLna = mLa.getNegated();
		mLb = mFac.createProposition("b", false);
		mLnb = mLb.getNegated();
		mLc = mFac.createProposition("c", false);
		mLnc = mLc.getNegated();
		mLd = mFac.createProposition("d", false);
		mLnd = mLd.getNegated();		
		mLe = mFac.createProposition("e", false);
		mLne = mLe.getNegated();		
		mLf = mFac.createProposition("f", false);
		mLnf = mLf.getNegated();		
		mLg = mFac.createProposition("g", false);
		mLng = mLf.getNegated();		
	}
	
	// TODO: Need the whole bonanza here obviously.
	
	@Test
	public void testAndTerms()
	{
		setLiterals();
		
		Expression<LogicalAnd> trm1 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[] {mLa, mLnb}));		
		Expression<LogicalAnd> trm2 = mFac.createTerm(new ArrayIterable<Literal>(new Literal[] {mLc, mLa}));
		
		Expression<LogicalAnd> anded = mFac.getAnd().and(trm1, trm2);
		
		String str = mFac.getDefaultStringer().stringise(anded);
		Assert.assertEquals("a ¬b c", str);
		
	}
}
