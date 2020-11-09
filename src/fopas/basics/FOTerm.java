package fopas.basics;

import java.util.Map;

public interface FOTerm {

	/**
	 * 
	 * @param assignment
	 * @return True if the term accepts assignments.
	 * @throws FORuntimeException 
	 */
	boolean assignVariables(FOStructure structure, Map<FOVariable, FOElement> assignment) throws FORuntimeException;
	
	FOElement getAssignment();
	
	enum TermType
	{
		VARIABLE,
		CONSTANT,
		FUNCTION
	}
	TermType getType();
}
