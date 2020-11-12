package fopas.basics;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import openpas.basics.Expressions.Expression;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.utils.SizedIterable;

public interface FOFormula
{
	boolean isNegated();
	boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment);
	boolean models(FOStructure structure, Set<FOVariable> setFreeVars) throws FOConstructionException;
	Iterable<Map<FOVariable, FOElement>> getSatisfyingAssignments(FOStructure structure, Set<FOVariable> setFreeVars) throws FOConstructionException;

//	Iterator<Expression<LogicalOr>> iterateAsCNF();
//	Iterator<Expression<LogicalAnd>> iterateAsDNF();
//	Iterator<FOElement> iterateAssumptions();
//	Iterator<FOElement> iteratePropositions();
//	Iterator<FOElement> iterateElements();
}
