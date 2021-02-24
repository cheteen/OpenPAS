package fopas;

import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.FOFormulaBRImpl.FormulaType;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORelation;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOVariable;

class FOFormulaBRRelation extends FOFormulaBRImpl
{
	final protected FORelation<FOElement> mRel;
	final protected List<FOTerm> mTerms;
	FOFormulaBRRelation(boolean isNegated, FORelation<FOElement> rel, List<FOTerm> terms)
	{
		super(isNegated);
		mRel = rel;
		mTerms = terms;
	}
	
	@Override
	public boolean checkAssignment(int depth, FOStructure structure, Map<FOVariable, FOElement> assignment)
	{
		FORuntime settings = structure.getRuntime();
		int trace = settings.getTraceLevel();
		if(trace >= 1)
		{
			settings.getStats().numL1CheckAsgRel++;
			settings.trace(-5, depth, this, "FOFormulaBRRelation", hashCode(), "checkAssignment", "%s", trace >= 5 ? stringiseAssignments(assignment) : "");
		}

		FOElement[] args = new FOElement[mTerms.size()]; 
		for(int i = 0; i < mTerms.size(); i++)
		{
			FOTerm term = mTerms.get(i);
			term.assignVariables(structure, assignment, false);
			FOElement asg = term.getAssignment();
			assert asg != null; // All variables should be assigned by this point.
			args[i] = asg;
		}
		
		boolean satisfied = mRel.satisfies(args);
		settings.trace(5, depth, this, "FOFormulaBRRelation", hashCode(), "checkAssignment", "satisfaction: %s (return: %s)", satisfied, mNegated ^ satisfied);
		
		return mNegated ^ satisfied;
	}

	@Override
	FormulaType getType() { return FormulaType.RELATION; }
	
	FORelation<FOElement> getRelation()
	{
		return mRel;
	}
	
	Iterable<FOTerm> getTerms()
	{
		return mTerms;
	}

	@Override
	void analyseVars(Set<FOVariable> setVarsInScope, Set<FOVariable> setVarsSeenInScope,
			Set<FOVariable> setFreeVars, List<String> listWarnings) 
	{
		for(FOTerm term : mTerms)
			((FOTermByRecursionImpl) term).analyseScope(setVarsSeenInScope);
	}

	@Override
	public FOFormula negate()
	{
		return new FOFormulaBRRelation(!mNegated, mRel, mTerms);
	}

	@Override
	public FOSet<FOElement> eliminateTrue(int depth, FOStructure structure, FOSet<FOElement> universe, FOVariable var,
			boolean complement, Map<FOVariable, FOElement> assignment, Set<FOAliasBindingByRecursionImpl.AliasEntry> aliasCalls)
	{
		FORuntime settings = structure.getRuntime();
		if(settings.getTraceLevel() >= 1)
		{
			settings.getStats().numL1ElimTrueRel++; // We only count this here since it's the only place that truly does elimination.
			if(settings.getTraceLevel() >= 5)
			{
				settings.trace(-5, depth, this, "FOFormulaBRRelation", hashCode(), "eliminateTrue", "(partial for %s) %s", var.getName(), stringiseAssignments(assignment));
				settings.trace( 5, depth, this, "FOFormulaBRRelation", hashCode(), "eliminateTrue", "variable: %s, complement: %s, universe: %s", var.getName(), complement, universe.getName());				
			}
		}

		// Do a partial assignment to the terms.
		for(FOTerm term : mTerms)
			term.assignVariables(structure, assignment, true);
		
		// Let's see if the relation can constrain its universe from here.
		// Constrain tries to return elements of the universe where the relation is true.
		// We complement this set to eliminate elements that are known to be true.
		// We don't need to complement it if this formula has a negation of course.
		FOSet<FOElement> constrained = mRel.tryConstrain(var, universe, mTerms, complement ^ !mNegated);

		settings.trace(5, depth, this, "FOFormulaBRRelation", hashCode(), "eliminateTrue", 
				"Elimination variable: %s, success: %s, subset: %s", var.getName(), constrained != universe, constrained.getName());
	
		return constrained;
	}
}
