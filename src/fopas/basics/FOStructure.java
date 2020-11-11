package fopas.basics;

import java.util.Map;

public interface FOStructure
{
	FOSet<FOElement> getUniverse();
	FOElement getConstantMapping(FOConstant foconst);
	FOElement setConstantMapping(FOConstant foconst, FOElement elt);
	
	/**
	 * Answer whether this structure is a model of {@code form}.
	 * @param form
	 * @return
	 * @throws FORuntimeException 
	 * @throws FOConstructionException 
	 */
	boolean models(FOFormula form) throws FORuntimeException, FOConstructionException;
	
	Iterable<Map<FOVariable, FOElement>> getSatisfyingAssignments(FOFormula form) throws FOConstructionException;
}
