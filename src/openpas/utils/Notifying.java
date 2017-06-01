package openpas.utils;

import java.io.PrintStream;

public interface Notifying {
	/**
	 * Nominates a PrintStream for an object to print notifications to. This is normally for getting
	 * timing and feedback for long running operations.
	 * @param ps
	 */
	void setNotifier(PrintStream ps);
}
