package fopas.basics;

import java.util.Iterator;
import java.util.Map;

import openpas.basics.Expressions.Expression;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.utils.SizedIterable;

public interface FOFormula
{
	boolean isNegated();
	boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment) throws FORuntimeException;
	boolean models(FOStructure structure) throws FORuntimeException;	
//	Iterator<Expression<LogicalOr>> iterateAsCNF();
//	Iterator<Expression<LogicalAnd>> iterateAsDNF();
//	Iterator<FOElement> iterateAssumptions();
//	Iterator<FOElement> iteratePropositions();
//	Iterator<FOElement> iterateElements();
}
