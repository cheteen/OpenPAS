package fopas;

import fopas.basics.FOStringiser;

public class FOSettings
{
	protected final int mDefaultStringLen = 100;
	protected final FOLanguage mLang = new FOLanguage();
	protected final FOByRecursionStringiser mSgiser = new FOByRecursionStringiser(mLang, mDefaultStringLen);
	protected int mTraceLevel =  2;
	
	int getConstrainLookAheadLimit() { return 4; }
	
	// Of course these really belong here, and I need a kind of runtime context to house them.
	int getTraceLevel() { return mTraceLevel; }
	FOStringiser getDefaultStringiser() { return mSgiser; }
	
	void trace(int level, String className, int instanceHash, String methodName, String format, Object... args)
	{
		if(mTraceLevel < level)
			return;
		
		// Formatting it here means a complex string isn't constructed unnecessarily when trace is turned off.
		String message = String.format(format, args);
		String line = String.format("%d#%s(%s).%s#%s", level, className, Integer.toHexString(instanceHash), methodName, message);
		System.err.println(line);
	}
}
