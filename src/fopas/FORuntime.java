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
					+ ", numL1ElimTrueRel=" + numL1ElimTrueRel + ", numL0ElimTrueRepeatCall=" + numL1ElimTrueRepeatCall
					+ ", numL1ElimTrueSuccess=" + numL1ElimTrueSuccess + ", numL1ElimTrueSuccess1="
					+ numL1ElimTrueSuccess1 + ", numL1ElimTrueSuccess0=" + numL1ElimTrueSuccess0 + "]";
		}

		int numL1CheckAsgIntoAlias;
		int numL1CheckAsgOr;
		int numL1CheckAsgAll;
		int numL1CheckAsgAllSub; // How many times the subformula was evaluated?
		int numL1CheckAsgAllSubFail; // How many times subformula eval succeeded (ie. continued)?
		int numL1CheckAsgRel;
		int numL1ElimTrueRel;
		int numL1ElimTrueRepeatCall;
		int numL1ElimTrueSuccess;
		int numL1ElimTrueSuccess1; // How many times was a universe set reduced to a set of size 1 (but not its complement)?
		int numL1ElimTrueSuccess0; // How many times was a universe set reduced to empty set (but not its complement)?
		
		void reset()
		{
			numL1CheckAsgIntoAlias = 0;
			numL1CheckAsgOr = 0;
			numL1CheckAsgAll = 0;
			numL1CheckAsgAllSub = 0;
			numL1CheckAsgAllSubFail = 0;
			numL1CheckAsgRel = 0;
			numL1ElimTrueRel = 0;
			numL1ElimTrueRepeatCall = 0;
			numL1ElimTrueSuccess = 0;
			numL1ElimTrueSuccess1 = 0;
			numL1ElimTrueSuccess0 = 0;
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
			formatln(ps, "ElimTrueRel: %d", numL1ElimTrueRel);
			formatln(ps, "ElimTrueRepeatCall: %d", numL1ElimTrueRepeatCall);
			formatln(ps, "ElimTrueSuccess: %d", numL1ElimTrueSuccess);
			formatln(ps, "ElimTrueSuccess1: %d", numL1ElimTrueSuccess1);
			formatln(ps, "ElimTrueSuccess0: %d", numL1ElimTrueSuccess0);
		}
	}

	protected final int mDefaultStringLen = 100;
	protected final FOLanguage mLang = new FOLanguage();
	protected final FOByRecursionStringiser mSgiser = new FOByRecursionStringiser(mLang, mDefaultStringLen);
	protected int mTraceLevel =  2; // Increase this to (2) to turn on debug tracing.

	protected final int mFormTraceLen = 100;
	protected final String mEmptyForm = Strings.repeat(" ", mFormTraceLen);
	
	protected final FOStats mStats = new FOStats();
	
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
