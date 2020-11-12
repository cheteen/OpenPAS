package fopas.basics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.FOFormulaByRecursionImpl;
import openpas.basics.Expressions.Expression;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.utils.SizedIterable;

public interface FOFormula
{
	boolean isNegated();
	boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment);
	boolean models(FOStructure structure) throws FOConstructionException;
	Iterable<Map<FOVariable, FOElement>> getSatisfyingAssignments(FOStructure structure) throws FOConstructionException;

//	Iterator<Expression<LogicalOr>> iterateAsCNF();
//	Iterator<Expression<LogicalAnd>> iterateAsDNF();
//	Iterator<FOElement> iterateAssumptions();
//	Iterator<FOElement> iteratePropositions();
//	Iterator<FOElement> iterateElements();
}
