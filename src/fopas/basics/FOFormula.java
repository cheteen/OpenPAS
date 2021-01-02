package fopas.basics;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.FOFormulaBRImpl;
import openpas.basics.Expressions.Expression;
import openpas.basics.LogicalOps.LogicalAnd;
import openpas.basics.LogicalOps.LogicalOr;
import openpas.utils.SizedIterable;

public interface FOFormula
{
	boolean isNegated();
	boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment);
	boolean models(FOStructure structure) throws FOConstructionException;
	void checkFormula(FOStructure structure) throws FOConstructionException;
	Iterable<Map<FOVariable, FOElement>> getSatisfyingAssignments(FOStructure structure) throws FOConstructionException;
	FOFormula negate() throws FOConstructionException;
	FOSet<FOElement> eliminateTrue(FOStructure structure, FOSet<FOElement> universe, FOVariable var, Map<FOVariable, FOElement> assignment);
}
