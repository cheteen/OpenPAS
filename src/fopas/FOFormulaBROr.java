package fopas;

import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.FOFormulaBRImpl.FormulaType;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOVariable;

class FOFormulaBROr extends FOFormulaBRImpl
{
	static enum OrSubType
	{
		OR,
		AND,
		IMP // implication
	}
	
	final protected List<FOFormula> mFormulas;
	final FOFormulaBROr.OrSubType mSubType;
	
	FOFormulaBROr(boolean isNegated, List<FOFormula> formulas, FOFormulaBROr.OrSubType type)
	{
		super(isNegated);
		mFormulas = formulas;
		mSubType = type;
	}

	FOFormulaBROr(boolean isNegated, List<FOFormula> formulas)
	{
		this(isNegated, formulas, OrSubType.OR);
	}

	@Override
	public boolean checkAssignment(int depth, FOStructure structure, Map<FOVariable, FOElement> assignment)
	{
		FORuntime settings = structure.getRuntime();
		int trace = settings.getTraceLevel();
		if(trace >= 1)
		{
			settings.getStats().numL1CheckAsgOr++;
			settings.trace(-5, depth, this, "FOFormulaBROr", hashCode(), "checkAssignment", "%s", trace >= 5 ? stringiseAssignments(assignment) : "");			
		}
		
		for(FOFormula form : mFormulas)
		{
			FOFormulaBRImpl formimpl = (FOFormulaBRImpl)form; 
			if(formimpl.checkAssignment(depth + 1, structure, assignment))
			{
				if(trace >= 5)
					settings.trace(5, depth, this, "FOFormulaBROr", hashCode(), "checkAssignment", "Satisfied (return: %s).", !mNegated);
				return !mNegated; // If we find satisfaction at any point we can quit.
			}
		}
		
		settings.trace(5, depth, this, "FOFormulaBROr", hashCode(), "checkAssignment", "Not satisfied (return: %s).", mNegated);
		
		return mNegated;
	}

	@Override
	FormulaType getType() { return FormulaType.OR; }

	@Override
	void analyseVars(Set<FOVariable> setVarsInScope, Set<FOVariable> setVarsSeenInScope,
			Set<FOVariable> setFreeVars, List<String> listWarnings) throws FOConstructionException
	{
		for(FOFormula form : mFormulas)
			((FOFormulaBRImpl) form).analyseVars(setVarsInScope, setVarsSeenInScope, setFreeVars, listWarnings);
	}

	@Override
	public FOFormula negate()
	{
		return new FOFormulaBROr(!mNegated, mFormulas);
	}
	
	/**
	 * The type this formula wants to be when presented (e.g. turned to string) is this.
	 * Subclass this to present other types, e.g. an AND sentence or a HORN clause.
	 */
	FOFormulaBROr.OrSubType getOriginalSubType()
	{
		return mSubType;
	}
	
	/**
	 * The type the interface variables work right now is like this. All implementations are presenting as ORs
	 * in this class since OR is the only implementation defined.
	 */
	FOFormulaBROr.OrSubType presentSubType()
	{
		return OrSubType.OR;
	}
	
	Iterable<FOFormula> presentFormulas()
	{
		return mFormulas;
	}

	@Override
	public FOSet<FOElement> eliminateTrue(int depth, FOStructure structure, FOSet<FOElement> universe, FOVariable var,
			boolean complement, Map<FOVariable, FOElement> assignment, Set<FOAliasBindingByRecursionImpl.AliasEntry> aliasCalls)
	{
		FORuntime settings = structure.getRuntime();
		if(settings.getTraceLevel() >= 5)
		{
			settings.trace(-5, depth, this, "FOFormulaBROr", hashCode(), "eliminateTrue", "(partial for %s) %s", var.getName(), stringiseAssignments(assignment));
			settings.trace( 5, depth, this, "FOFormulaBROr", hashCode(), "eliminateTrue", "variable: %s, complement: %s, universe: %s", var.getName(), complement, universe.getName());			
		}
		
		int elimTarget = settings.getTargetElimTrue();
		
		// There are three possible strategies that I can think of:
		// (1) Simple O(N): iteratively constrain the universe subset one by one. As long as there's one order and ordered sets this should create the ideal solution.
		// (2) Greedy O(NxN): This can pick up one layer find out the best, take the best, and repeat.
		// (3) Exhaustive O(N!): Follow any possible lineage to find the absolute best combination.
		
		// Not sure where (2) and (3) would be needed at this point.
		
		// Simple strategy:
		FOSet<FOElement> fosetSubset = universe;
		for(FOFormula form : mFormulas)
		{
			FOFormulaBRImpl formimpl = (FOFormulaBRImpl) form; 
			fosetSubset = formimpl.eliminateTrue(depth + 1, structure, fosetSubset, var, mNegated, assignment, aliasCalls);
			if(fosetSubset.size() <= elimTarget)
				break;
		}
		// Should we count something here?
		
		settings.trace(5, depth, this, "FOFormulaBROr", hashCode(), "eliminateTrue", 
				"Elimination variable: %s, success: %s, smallest subset: %s", var.getName(), fosetSubset != universe, fosetSubset.getName());
		
		return fosetSubset;
	}
}
