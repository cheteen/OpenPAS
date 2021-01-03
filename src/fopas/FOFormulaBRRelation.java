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
	public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment)
	{
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
	public FOSet<FOElement> eliminateTrue(FOStructure structure, FOSet<FOElement> universe, FOVariable var,
			Map<FOVariable, FOElement> assignment)
	{
		// Do a partial assignment to the terms.
		for(FOTerm term : mTerms)
			term.assignVariables(structure, assignment, true);
		
		// Let's see if the relation can contrain its universe from here.
		// Constrain tries to return elements of the universe where the relation is true.
		// We complement this set to eliminate elements that are known to be true.
		// We don't need to complement it if this formula has a negation of course.
		return mRel.tryConstrain(var, universe, mTerms, !mNegated);
	}
}
