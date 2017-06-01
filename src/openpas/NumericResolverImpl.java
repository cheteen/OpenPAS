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

import java.io.PrintStream;

import openpas.basics.Expressions.Expression;
import openpas.basics.Expressions.SimpleSentence;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.basics.NumericResolver;
import openpas.basics.ProbabilityComputer;
import openpas.basics.PropFactory;
import openpas.basics.SymbolicResolver;
import openpas.utils.Notifier;
import openpas.utils.Notifying;

class NumericResolverImpl implements NumericResolver, Notifying
{
	PropFactory mFac;
	SymbolicResolver mSr;
	ProbabilityComputer mPC;
	
	double mDQSI = Double.NaN;
	
	Notifier mNotifier = Notifier.NULL_NOTIFIER;// to get verbose output
	
	// Once the NR is created the underlying KB shouldn't be changed.
	protected NumericResolverImpl(SymbolicResolver sr, ProbabilityComputer pc, PropFactory fac)
	{
		mFac = fac;
		mSr = sr;
		mPC = pc;
	}

	@Override
	public double calcDQS(SimpleSentence<LogicalAnd, LogicalOr> hypothesis) 
	{
		mNotifier.printfln("calcDQS: Finding QS for h.");
		SimpleSentence<LogicalOr, LogicalAnd> qs = mSr.findQS(hypothesis);
		
		mNotifier.printfln("calcDQS: Calculating DQS for hypothesis...");
		double dqs = mPC.computeDNFProbability(qs);
		mNotifier.printfln("calcDQS: Done.");
		
		return dqs;
	}

	@Override
	public double calcNormalisedDSP(SimpleSentence<LogicalAnd, LogicalOr> hypothesis) {
		return calcNonNormalisedDSP(hypothesis)/(1 - mDQSI);
	}

	@Override
	public double calcDQS_I() 
	{
		mNotifier.printfln("calcDQS_I: Finding QS for inconsistency.");
		// Find \mu QS_I - quasi-support for inconsistent scenarios.
		SimpleSentence<LogicalOr, LogicalAnd> qsi = mSr.findQS(mFac.createClause());

		mNotifier.printfln("calcDQS_I: Calculating probability for inconsistency...");
		double dqsi = mPC.computeDNFProbability(qsi);
		mNotifier.printfln("calcDQS_I: Done.");
		
		return dqsi;
	}

	@Override
	public double calcNonNormalisedDSP(
			SimpleSentence<LogicalAnd, LogicalOr> hypothesis) 
	{
		if(Double.isNaN(mDQSI))
			mDQSI = calcDQS_I();
		
		double dqs = calcDQS(hypothesis);
		return dqs - mDQSI;
	}

	// From SymbolicResolver interface - relay the results from the impl object.
	@Override
	public SimpleSentence<LogicalOr, LogicalAnd> findQS(SimpleSentence<LogicalAnd, LogicalOr> hypothesis) {
		return mSr.findQS(hypothesis);
	}
	@Override
	public SimpleSentence<LogicalOr, LogicalAnd> findSP(SimpleSentence<LogicalAnd, LogicalOr> hypothesis) {
		return mSr.findSP(hypothesis);
	}
	@Override
	public SimpleSentence<LogicalOr, LogicalAnd> findQS(Expression<LogicalOr> hclause) {
		return mSr.findQS(hclause);
	}
	// From Probability computing interface
	@Override
	public double computeDNFProbability(SimpleSentence<LogicalOr, LogicalAnd> dnf) {
		return mPC.computeDNFProbability(dnf);
	}
	@Override
	public void setNotifier(PrintStream ps) {
		if(mSr instanceof Notifying)
			((Notifying) mSr).setNotifier(ps);
		mNotifier = new Notifier(ps);
	}
}
