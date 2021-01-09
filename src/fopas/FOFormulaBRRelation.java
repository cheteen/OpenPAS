package fopas;

import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.FOFormulaBRImpl.FormulaType;
import fopas.FOFormulaBRRelation.AliasTracker;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORelation;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOVariable;

class FOFormulaBRRelation extends FOFormulaBRImpl
{
	static class AliasTracker
	{
		FOAliasBindingByRecursionImpl alias;
		FOVariable var;
		int count;
		int limit;
	}
	static class AliasEntry
	{
		final FOVariable var;
		final FOAliasBindingByRecursionImpl alias;
		
		AliasEntry(FOVariable var, FOAliasBindingByRecursionImpl alias)
		{
			this.var = var;
			this.alias = alias;
		}
		
		@Override
		public int hashCode() {
			final int prime = 37;
			int result = 1;
			result = prime * result + ((alias == null) ? 0 : alias.hashCode());
			result = prime * result + ((var == null) ? 0 : var.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			AliasEntry other = (AliasEntry) obj;
			if (alias == null) {
				if (other.alias != null)
					return false;
			} else if (!alias.equals(other.alias))
				return false;
			if (var == null) {
				if (other.var != null)
					return false;
			} else if (!var.equals(other.var))
				return false;
			return true;
		}
	}

	final protected FORelation<FOElement> mRel;
	final protected List<FOTerm> mTerms;
	FOFormulaBRRelation(boolean isNegated, FORelation<FOElement> rel, List<FOTerm> terms)
	{
		super(isNegated);
		mRel = rel;
		mTerms = terms;
	}
	
	@Override
	public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment,
			Map<FOFormulaBRRelation.AliasEntry, FOFormulaBRRelation.AliasTracker> aliasCalls)
	{
		FOSettings settings = structure.getSettings();
		if(settings.getTraceLevel() >= 2)
			settings.trace(2, "FOFormulaBRRelation", hashCode(), "checkAssignment", "formula: %s", settings.getDefaultStringiser().stringiseFormula(this));

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
		settings.trace(2, "FOFormulaBRRelation", hashCode(), "checkAssignment", "satisfaction: %s (return: %s)", satisfied, mNegated ^ satisfied);
		
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
	public FOSet<FOElement> eliminateTrue(FOStructure structure, FOSet<FOElement> universe, FOVariable var, boolean complement,
			Map<FOVariable, FOElement> assignment, Map<FOFormulaBRRelation.AliasEntry, FOFormulaBRRelation.AliasTracker> aliasCalls)
	{
		FOSettings settings = structure.getSettings();
		if(settings.getTraceLevel() >= 2)
			settings.trace(2, "FOFormulaBRRelation", hashCode(), "eliminateTrue", "variable: %s, complement: %s, formula: %s, universe: %s",
					var.getName(), complement, settings.getDefaultStringiser().stringiseFormula(this), universe.getName());

		// Do a partial assignment to the terms.
		for(FOTerm term : mTerms)
			term.assignVariables(structure, assignment, true);
		
		// Let's see if the relation can constrain its universe from here.
		// Constrain tries to return elements of the universe where the relation is true.
		// We complement this set to eliminate elements that are known to be true.
		// We don't need to complement it if this formula has a negation of course.
		FOSet<FOElement> constrained = mRel.tryConstrain(var, universe, mTerms, complement ^ !mNegated);

		settings.trace(2, "FOFormulaBRRelation", hashCode(), "eliminateTrue", "Elimination success: %s, subset: %s", 
				constrained != universe, constrained.getName());
	
		return constrained;
	}
}
