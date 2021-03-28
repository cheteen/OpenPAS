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
