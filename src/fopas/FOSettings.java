package fopas;

import com.google.common.base.Function;
import com.google.common.base.Strings;

import fopas.basics.FOFormula;
import fopas.basics.FOStringiser;

public class FOSettings
{
	protected final int mDefaultStringLen = 100;
	protected final FOLanguage mLang = new FOLanguage();
	protected final FOByRecursionStringiser mSgiser = new FOByRecursionStringiser(mLang, mDefaultStringLen);
	protected int mTraceLevel =  2; // Increase this to (2) to turn on debug tracing.

	protected final int mFormTraceLen = 20;
	protected final String mEmptyForm = Strings.repeat(" ", mFormTraceLen);
	
	// Of course these really belong here, and I need a kind of runtime context to house them.
	int getTraceLevel() { return mTraceLevel; }
	FOStringiser getDefaultStringiser() { return mSgiser; }
	
	void trace(int level, int depth, FOFormula form, String className, int instanceHash, String methodName, String format, Object... args)
	{		
		if(mTraceLevel < level)
			return;
		
		String formString;
		if(form != null)
			formString = Strings.repeat(".", depth) + Strings.padEnd(mSgiser.stringiseFormula(form, mFormTraceLen), mFormTraceLen, ' ');
		else
			formString = mEmptyForm;
		
		// Formatting it here means a complex string isn't constructed unnecessarily when trace is turned off.
		String message = String.format(format, args);
		String line = String.format("%d#%s#%s(%s).%s#%s", level, formString,
				className, Integer.toHexString(instanceHash), methodName, message);
		System.err.println(line);
	}
}
