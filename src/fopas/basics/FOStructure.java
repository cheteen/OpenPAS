package fopas.basics;

import java.util.Map;

public interface FOStructure
{
	FOUniverse getUniverse();
	FOElement getConstantMapping(FOConstant foconst);
	FOElement setConstantMapping(FOConstant foconst, FOElement elt);
	Iterable<FORelation<FOElement>> getRelations();
	Iterable<FOFunction> getFunctions();
	Iterable<FOConstant> getConstants();
	
	/**
	 * Answer whether this structure is a model of {@code form}.
	 * @param form
	 * @return
	 * @throws FOConstructionException 
	 */
	boolean models(FOFormula form) throws FOConstructionException;
	
	Iterable<Map<FOVariable, FOElement>> getSatisfyingAssignments(FOFormula form) throws FOConstructionException;
	Iterable<Map<FOVariable, FOElement>> getAssignments(FOFormula form) throws FOConstructionException;
}
