package fopas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.FOFormulaBRImpl.FormulaType;
import fopas.basics.FOAlias;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOVariable;

class FOAliasBindingByRecursionImpl extends FOFormulaBRImpl implements FOAlias
{
	protected final List<FOTerm> mTerms;
	protected final FOAliasByRecursionImpl mBoundFormula;
	protected final String mName;
	
	FOAliasBindingByRecursionImpl(boolean negated, String name, FOAliasByRecursionImpl boundFormula, List<FOTerm> terms) throws FOConstructionException
	{
		super(negated);
		mName = name;
		mTerms = terms;
		mBoundFormula = boundFormula;
		
		if(terms.size() != mBoundFormula.getCardinality())
			throw new FOConstructionException("Attempt to call alias with wrong number of args.");
	}

	@Override
	public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment)
	{
		Map<FOVariable, FOElement> mappedAssignment = mapAssignments(structure, assignment, false);
		
		//TODO: Need to start a new assignment round here with the free variables given an assignment.
		//TODO: Should cache the free variables during the bind creation since this is root level at that point.
		
		boolean satisfied = mBoundFormula.checkAssignment(structure, mappedAssignment);
		
		return mNegated ^ satisfied;
	}

	protected Map<FOVariable, FOElement> mapAssignments(FOStructure structure,
			Map<FOVariable, FOElement> assignment, boolean isPartial)
	{
		Map<FOVariable, FOElement> mappedAssignment = new HashMap<FOVariable, FOElement>();

		for(int i = 0; i < mTerms.size(); i++)
		{
			FOTerm term = mTerms.get(i);
			term.assignVariables(structure, assignment, isPartial);
			FOElement asg = term.getAssignment();
			if(asg == null)
			{
				assert isPartial; // All variables should be assigned by this point if it's not partial.					
				if(!isPartial)
					throw new FORuntimeException("Expected assignment not found for alias.");
			}
			else
				mappedAssignment.put(mBoundFormula.getListArgs().get(i), asg);
		}
		return mappedAssignment;
	}

	@Override
	public Iterable<FOVariable> getArgs()
	{
		return mBoundFormula.getArgs();
	}

	@Override
	FormulaType getType()
	{
		return FormulaType.ALIAS_BINDING;
	}

	@Override
	void analyseVars(Set<FOVariable> setVarsInScope, Set<FOVariable> setVarsSeenInScope,
			Set<FOVariable> setFreeVars, List<String> listWarnings) throws FOConstructionException
	{
		// This only looks at the terms in the args since variables of the alias are mapped (ie. not used).
		for(FOTerm term : mTerms)
			((FOTermByRecursionImpl) term).analyseScope(setVarsSeenInScope);
	}

	@Override
	public int getCardinality()
	{
		return mTerms.size();
	}

	@Override
	public String getName()
	{
		return mName;
	}
	
	List<FOTerm> getBoundTerms()
	{
		return mTerms;
	}

	@Override
	public FOFormula negate() throws FOConstructionException
	{
		return new FOAliasBindingByRecursionImpl(!mNegated, mName, mBoundFormula, mTerms);
	}

	@Override
	public FOSet<FOElement> eliminateTrue(FOStructure structure, FOSet<FOElement> universe, FOVariable var,
			Map<FOVariable, FOElement> assignment)
	{
		Map<FOVariable, FOElement> mappedAssignment = mapAssignments(structure, assignment, true);
		return mBoundFormula.eliminateTrue(structure, universe, var, mappedAssignment);
	}
}