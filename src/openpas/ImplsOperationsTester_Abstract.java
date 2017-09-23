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
