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
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOVariable;

/**
 * Alias allows a formula to be named, and possibly used multiple time in another formula.
 * Also some simple sanity checks for the alias to make sure the arg variables are in the formula.
 */
public class FOAliasByRecursionImpl extends FOFormulaByRecursionImpl implements FOAlias
{
	protected FOFormulaByRecursionImpl mScopeForm;
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
		
		mScopeForm = (FOFormulaByRecursionImpl) form;
	}
	
	static class FOAliasBindingByRecursionImpl extends FOFormulaByRecursionImpl implements FOAlias
	{
		final List<FOTerm> mTerms;
		final FOAliasByRecursionImpl mBoundFormula;
		final Map<FOVariable, FOElement> mMappedAssignment;
		
		FOAliasBindingByRecursionImpl(boolean negated, FOAliasByRecursionImpl boundFormula, List<FOTerm> terms) throws FOConstructionException
		{
			super(negated);
			mTerms = terms;
			mBoundFormula = boundFormula;
			mMappedAssignment = new HashMap<FOVariable, FOElement>();
			
			if(terms.size() != mBoundFormula.getCardinality())
				throw new FOConstructionException("Attempt to call alias with wrong number of args.");
		}

		@Override
		public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment)
		{
			for(int i = 0; i < mTerms.size(); i++)
			{
				FOTerm term = mTerms.get(i);
				term.assignVariables(structure, assignment);
				FOElement asg = term.getAssignment();
				assert asg != null; // All variables should be assigned by this point.
				mMappedAssignment.put(mBoundFormula.getListArgs().get(i), asg);
			}
			
			boolean satisfied = mBoundFormula.checkAssignment(structure, mMappedAssignment);
			
			return mNegated ^ satisfied;
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
				Set<FOVariable> setFreeVars, List<String> listWarnings) throws FOConstructionException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getCardinality()
		{
			return mTerms.size();
		}
	}
}
