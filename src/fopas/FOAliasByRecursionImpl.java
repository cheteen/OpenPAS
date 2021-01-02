package fopas;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.basics.FOAlias;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOVariable;

/**
 * Alias allows a formula to be named, and possibly used multiple time in another formula.
 * Also some simple sanity checks for the alias to make sure the arg variables are in the formula.
 */
// Definition of an "alias" is on Friendly p.127
public class FOAliasByRecursionImpl extends FOFormulaBRImpl implements FOAlias
{
	protected FOFormulaBRImpl mScopeForm;
	protected final List<FOVariable> mArgs;
	protected final String mName;
	FOAliasByRecursionImpl(String name, List<FOVariable> args)
	{
		super(false); // no point in negating the alias itself.
		mArgs = args;
		mName = name;
	}
	
	@Override
	public Iterable<FOVariable> getArgs()
	{
		return mArgs;
	}
	
	List<FOVariable> getListArgs()
	{
		return mArgs;
	}

	@Override
	public int getCardinality()
	{
		return mArgs.size();
	}

	@Override
	public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment)
	{
		// Add caching here.
		return mNegated ^ mScopeForm.checkAssignment(structure, assignment);
	}

	@Override
	FormulaType getType()
	{
		return FormulaType.ALIAS;
	}

	@Override
	void analyseVars(Set<FOVariable> setVarsInScope, Set<FOVariable> setVarsSeenInScope, Set<FOVariable> setFreeVars,
			List<String> listWarnings) throws FOConstructionException
	{
		// TODO: This needs to check that this formula doesn't contain undeclared variables.
		if(mScopeForm == null)
			throw new FOConstructionException("Scoped formula undefined alias: " + mName);
		
		Set<FOVariable> setVarsInMyScope = new LinkedHashSet<>();
		mScopeForm.analyseVars(setVarsInScope, setVarsInMyScope, setFreeVars, listWarnings);
		
		for(FOVariable var : mArgs)
			if(!setVarsInMyScope.contains(var))
				listWarnings.add(String.format("Variable specified %s not seen in alias %s.", var.getName(), mName));
		
		setVarsSeenInScope.addAll(setVarsInMyScope);
	}

	public void setScopeFormula(FOFormula form) throws FOConstructionException
	{
		if(mScopeForm != null)
			throw new FOConstructionException("Scoped formula already defined for alias: " + mName);
		
		mScopeForm = (FOFormulaBRImpl) form;
	}
	
	@Override
	public String getName()
	{
		return mName;
	}
	
	@Override
	public FOFormula negate()
	{
		assert false; // should never happen.
		return null; // not possible to negate this.
	}

	@Override
	public FOSet<FOElement> eliminateTrue(FOStructure structure, FOSet<FOElement> universe, FOVariable var,
			Map<FOVariable, FOElement> assignment)
	{
		// The only thing at this level to do is to handover the decision to the contained formula.
		return mScopeForm.eliminateTrue(structure, universe, var, assignment);
	}

	static class FOAliasBindingByRecursionImpl extends FOFormulaBRImpl implements FOAlias
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
}
