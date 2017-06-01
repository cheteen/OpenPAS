package openpas.utils;

import java.io.PrintStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

public class Notifier {
	
	public static final Notifier NULL_NOTIFIER = new Notifier(null);

	PrintStream mPS;
	public Notifier(PrintStream ps)
	{
		mPS = ps;
	}
	
	public void printfln(String message)
	{
		printfln(message, null);
	}
	
	public void printfln(String fmt, List<String> args)
	{
		if(mPS == null)
			return;
		mPS.print("[");
		mPS.print(new Timestamp(new Date().getTime()));
		mPS.print("] ");
		mPS.printf(fmt, args);
		mPS.println();
	}	
}
