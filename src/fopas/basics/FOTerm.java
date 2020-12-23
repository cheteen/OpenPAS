package fopas.basics;

import java.util.Map;

public interface FOTerm {

	/**
	 * 
	 * @param assignment
	 * @return True if the term accepts assignments.
	 */
	void assignVariables(FOStructure structure, Map<FOVariable, FOElement> assignment, boolean isPartial);
	
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
