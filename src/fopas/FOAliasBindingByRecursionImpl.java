package fopas;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.FOFormulaBRImpl.FormulaType;
import fopas.FOFormulaBRRelation.AliasEntry;
import fopas.FOFormulaBRRelation.AliasTracker;
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
	// The hashCode and equals of this class is intentionally left unimplemented as each instance should be unique.
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
	public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment,
			Map<FOFormulaBRRelation.AliasEntry, FOFormulaBRRelation.AliasTracker> aliasCalls)
	{
		Map<FOVariable, FOElement> mappedAssignment = mapAssignments(structure, assignment, false);
		
		//TODO: Need to start a new assignment round here with the free variables given an assignment.
		//TODO: Should cache the free variables during the bind creation since this is root level at that point.
		
		boolean satisfied = mBoundFormula.checkAssignment(structure, mappedAssignment, aliasCalls);
		
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
	public FOSet<FOElement> eliminateTrue(FOStructure structure, FOSet<FOElement> universeSubset, FOVariable var, boolean complement,
			Map<FOVariable, FOElement> assignment, Map<FOFormulaBRRelation.AliasEntry, FOFormulaBRRelation.AliasTracker> aliasCalls)
	{
		FOSettings settings = structure.getSettings();

		Map<FOVariable, FOElement> mappedAssignment = mapAssignments(structure, assignment, true);
		
		// When we're trying to constrain for a variable in a potentially infinite forall formula,
		// we track the variable and the aliases we pass through. If we pass throug the same alias with the same
		// variable over the limit times, then we terminate the recursive calls here.
		//
		// This is a rudimentary effort to stop infinite recursions in the constraining effort, the more sophisticated
		// version of this will come with the cut relation to be implemented.
		
		AliasEntry ae = new AliasEntry(var, this); 
		AliasTracker at = aliasCalls.get(ae);
		if(at == null)
		{
			at = new AliasTracker();
			at.alias = this;
			at.count = 0;
			at.limit = settings.getConstrainLookAheadLimit(); // TODO: Add tests that'll exercise this!
			
			aliasCalls.put(ae, at);
		}
		else
		{
			at.count++;
			if(at.count >= at.limit)
			{
				aliasCalls.remove(ae);
				return universeSubset;
			}
		}
		
		return mBoundFormula.eliminateTrue(structure, universeSubset, var, complement ^ mNegated, mappedAssignment, aliasCalls);
	}
}