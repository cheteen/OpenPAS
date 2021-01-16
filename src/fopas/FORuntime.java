package fopas;

import com.google.common.base.Strings;

import fopas.basics.FOFormula;
import fopas.basics.FOStringiser;

public class FORuntime
{
	static class FOStats
	{
		int numL1CheckAsgIntoAlias;
		int numL1CheckAsgOr;
		int numL1CheckAsgAll;
		int numL1CheckAsgAllSub; // How many times the subformula was evaluated?
		int numL1CheckAsgAllSubFail; // How many times subformula eval succeeded (ie. continued)?
		int numL1CheckAsgRel;
		int numL1ElimTrueRel;
		int numL0ElimTrueRepeatCall;
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
			numL0ElimTrueRepeatCall = 0;
			numL1ElimTrueSuccess = 0;
			numL1ElimTrueSuccess1 = 0;
			numL1ElimTrueSuccess0 = 0;
		}
	}

	protected final int mDefaultStringLen = 100;
	protected final FOLanguage mLang = new FOLanguage();
	protected final FOByRecursionStringiser mSgiser = new FOByRecursionStringiser(mLang, mDefaultStringLen);
	protected int mTraceLevel =  5; // Increase this to (2) to turn on debug tracing.

	protected final int mFormTraceLen = 100;
	protected final String mEmptyForm = Strings.repeat(" ", mFormTraceLen);
	
	protected final FOStats mStats = new FOStats();
	
	// Of course these really belong here, and I need a kind of runtime context to house them.
	int getTraceLevel() { return mTraceLevel; }
	FOStringiser getDefaultStringiser() { return mSgiser; }
	FOStats getStats() { return mStats; }
	
	void trace(int level, int depth, FOFormula form, String className, int instanceHash, String methodName, String format, Object... args)
	{		
		if(mTraceLevel < level || level < 0 && mTraceLevel < -level)
			return;
		
		// Formatting it here means a complex string isn't constructed unnecessarily when trace is turned off.
		String message = String.format(format, args);
		String line;
		if(level > 0)
			line = String.format("%d#%s(%s).%s#%s", level,
				className, Integer.toHexString(instanceHash), methodName, message);
		else
			line = String.format("%d#%s(%s).%s", -level,
					className, Integer.toHexString(instanceHash), methodName);
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
}
