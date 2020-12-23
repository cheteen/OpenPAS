package fopas.basics;

import java.util.Map;

public interface FOTerm {

	/**
	 * 
	 * @param assignment
	 * @return True if the term accepts assignments.
	 */
	boolean assignVariables(FOStructure structure, Map<FOVariable, FOElement> assignment);
	
	FOElement getAssignment();
	void resetAssignment();
	
	enum TermType
	{
		VARIABLE,
		CONSTANT,
		FUNCTION
	}
	TermType getType();
}
