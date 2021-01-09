package fopas.basics;

import java.util.Map;

import fopas.FOSettings;

public interface FOStructure
{
	FOSet<FOElement> getUniverse();
	FOElement getConstantMapping(FOConstant foconst);
	FOElement setConstantMapping(FOConstant foconst, FOElement elt);
	Iterable<FORelation<FOElement>> getRelations();
	Iterable<FOFunction> getFunctions();
	Iterable<FOConstant> getConstants();

	Iterable<String> getAliases();
	FOFormula getAlias(String name);
	void addAlias(FOAlias formAlias) throws FOConstructionException;
	
	/**
	 * Answer whether this structure is a model of {@code form}.
	 * @param form
	 * @return
	 * @throws FOConstructionException 
	 */
	boolean models(FOFormula form) throws FOConstructionException;
	
	Iterable<Map<FOVariable, FOElement>> getSatisfyingAssignments(FOFormula form) throws FOConstructionException;
	Iterable<Map<FOVariable, FOElement>> getAssignments(FOFormula form) throws FOConstructionException;
	
	FOSettings getSettings();
}
