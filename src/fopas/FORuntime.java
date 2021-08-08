//    Copyright (c) 2021 Burak Cetin
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

package fopas;

import java.io.PrintStream;

import com.google.common.base.Strings;

import fopas.basics.FOFormula;
import fopas.basics.FOStringiser;

public class FORuntime
{
	static class FOStats
	{
		@Override
		public String toString() {
			return "FOStats [numL1CheckAsgIntoAlias=" + numL1CheckAsgIntoAlias + ", numL1CheckAsgOr=" + numL1CheckAsgOr
					+ ", numL1CheckAsgAll=" + numL1CheckAsgAll + ", numL1CheckAsgAllSub=" + numL1CheckAsgAllSub
					+ ", numL1CheckAsgAllSubFail=" + numL1CheckAsgAllSubFail + ", numL1CheckAsgRel=" + numL1CheckAsgRel
					+ ", numL1ElimTrueRelAttempts=" + numL1ElimTrueRelAttempts + ", numL1ElimTrueRelSuccess=" + numL1ElimTrueRelSuccess + ", numL0ElimTrueRepeatCall=" + numL1ElimTrueRepeatCall
					+ ", numL1ElimTrueSuccess=" + numL1ElimTrueForallSuccess + ", numL1ElimTrueSuccess1="
					+ numL1ElimTrueForallSuccess1 + ", numL1ElimTrueSuccess0=" + numL1ElimTrueForallSuccess0 + "]";
		}

		int numL1CheckAsgIntoAlias;
		int numL1CheckAsgOr;
		int numL1CheckAsgAll;
		int numL1CheckAsgAllSub; // How many times the subformula was evaluated?
		int numL1CheckAsgAllSubFail; // How many times subformula eval succeeded (ie. continued)?
		int numL1CheckAsgRel;
		int numL1ElimTrueRelAttempts;
		int numL1ElimTrueRelSuccess;
		int numL1ElimTrueRepeatCall;
		int numL1ElimTrueForallSuccess;
		int numL1ElimTrueForallSuccess1; // How many times was a universe set reduced to a set of size 1 (but not its complement)?
		int numL1ElimTrueForallSuccess0; // How many times was a universe set reduced to empty set (but not its complement)?
		int numL1ElimTrueForallFail;
		int numL1ElimTrueOrSuccess;
		int numL1ElimTrueOrFail;
		int numL1ElimTrueOrSubSuccess;
		int numL1ElimTrueOrSubFail;
		int numL1ElimTrueOrSuccessTarget;
		
		protected FORuntime mRuntime;
		public FOStats(FORuntime runtime)
		{
			mRuntime = runtime;
		}
		
		void reset()
		{
			numL1CheckAsgIntoAlias = 0;
			numL1CheckAsgOr = 0;
			numL1CheckAsgAll = 0;
			numL1CheckAsgAllSub = 0;
			numL1CheckAsgAllSubFail = 0;
			numL1CheckAsgRel = 0;
			numL1ElimTrueRelAttempts = 0;
			numL1ElimTrueRelSuccess = 0;
			numL1ElimTrueRepeatCall = 0;
			numL1ElimTrueForallSuccess = 0;
			numL1ElimTrueForallSuccess1 = 0;
			numL1ElimTrueForallSuccess0 = 0;
			numL1ElimTrueForallFail = 0;
			numL1ElimTrueOrSuccess = 0;
			numL1ElimTrueOrFail = 0;
			numL1ElimTrueOrSubSuccess = 0;
			numL1ElimTrueOrSubFail = 0;
			numL1ElimTrueOrSuccessTarget = 0;
		}
		
		private void formatln(PrintStream ps, String format, Object...args)
		{
			ps.println(String.format(format, args));
		}
		
		void printStats(PrintStream ps)
		{
			formatln(ps, "CheckAsgIntoAlias: %d", numL1CheckAsgIntoAlias);
			formatln(ps, "CheckAsgOr: %d", numL1CheckAsgOr);
			formatln(ps, "CheckAsgAll: %d", numL1CheckAsgAll);
			formatln(ps, "CheckAsgAllSub: %d", numL1CheckAsgAllSub);
			formatln(ps, "CheckAsgAllSubFail: %d", numL1CheckAsgAllSubFail);
			formatln(ps, "CheckAsgRel: %d", numL1CheckAsgRel);
			formatln(ps, "ElimTrueRelAttempts: %d", numL1ElimTrueRelAttempts);
			formatln(ps, "ElimTrueRelSuccess: %d", numL1ElimTrueRelSuccess);
			formatln(ps, "ElimTrueRepeatCall: %d", numL1ElimTrueRepeatCall);
			formatln(ps, "ElimTrueForallSuccess: %d", numL1ElimTrueForallSuccess);
			formatln(ps, "ElimTrueForallSuccess1: %d", numL1ElimTrueForallSuccess1);
			formatln(ps, "ElimTrueForallSuccess0: %d", numL1ElimTrueForallSuccess0);
			formatln(ps, "ElimTrueForallFail: %d", numL1ElimTrueForallFail);
			formatln(ps, "ElimTrueOrSuccess: %d", numL1ElimTrueOrSuccess);
			formatln(ps, "ElimTrueOrFail: %d", numL1ElimTrueOrFail);
			formatln(ps, "ElimTrueOrSubSuccess: %d", numL1ElimTrueOrSubSuccess);
			formatln(ps, "ElimTrueOrSubFail: %d", numL1ElimTrueOrSubFail);
			formatln(ps, "ElimTrueOrSuccessTarget: %d", numL1ElimTrueOrSuccessTarget);
		}
		
		void incrementedStat(String metricName, int newValue, int currentTraceLevel, FOFormula form)
		{
			if(currentTraceLevel > 2)
			{
				String strForm = mRuntime.stringiseFormulaForTrace(currentTraceLevel, form);
				formatln(System.err, "Incremented %s to %d in %s", metricName, newValue, strForm);
			}
		}
	}

	protected final int mDefaultStringLen = 100;
	protected final FOLanguage mLang = new FOLanguage();
	protected final FOByRecursionStringiser mSgiser = new FOByRecursionStringiser(mLang, mDefaultStringLen);
	protected int mTraceLevel =  1; // Increase this to (2) to turn on debug tracing.

	protected final int mFormTraceLen = 100;
	protected final String mEmptyForm = Strings.repeat(" ", mFormTraceLen);
	
	protected final FOStats mStats = new FOStats(this);
	
	protected final int mTargetElimTrue; // default 1

	FORuntime()
	{
		this(1);
	}

	FORuntime(int targetElimTrue)
	{
		mTargetElimTrue = targetElimTrue;
	}
	
	// Of course these really belong here, and I need a kind of runtime context to house them.
	int getTraceLevel() { return mTraceLevel; }
	FOStringiser getDefaultStringiser() { return mSgiser; }
	FOStats getStats() { return mStats; }
	
	String stringiseFormulaForTrace(int level, FOFormula form)
	{
		if(mTraceLevel < level || level < 0 && mTraceLevel < -level)
			return "";
		return mSgiser.stringiseFormula(form, mFormTraceLen);
	}
	
	void trace(int level, int depth, FOFormula form, String className, int instanceHash, String methodName, String format, Object... args)
	{		
		if(mTraceLevel < level || level < 0 && mTraceLevel < -level)
			return;
		
		// Formatting it here means a complex string isn't constructed unnecessarily when trace is turned off.
		String message = String.format(format, args);
		String line;
		if(level > 0)
			line = String.format("%d#%s(%s).%s#%03d#%s", level,
				className, Integer.toHexString(instanceHash), methodName, depth, message);
		else
			line = String.format("%d#%s(%s).%s#%03d", -level,
					className, Integer.toHexString(instanceHash), methodName, depth);
		System.err.println(line);

		// Level <0 asks for this line to be printed
		if(level < 0)
		{
			String formString;
			if(form != null)
				formString = Strings.repeat(".", depth) + Strings.padEnd(mSgiser.stringiseFormula(form, mFormTraceLen), mFormTraceLen, ' ');
			else
				formString = mEmptyForm;
			System.err.println("# " + formString);
			System.err.println("# " + message);
		}		
	}
	
	public int getTargetElimTrue()
	{
		return mTargetElimTrue;
	}
}
