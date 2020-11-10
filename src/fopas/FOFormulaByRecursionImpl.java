package fopas;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOVariable;

abstract class FOFormulaByRecursionImpl implements FOFormula {
	
	final protected boolean mNegated;
	
	FOFormulaByRecursionImpl(boolean isNegated)
	{
		mNegated = isNegated;
	}
	
	enum FormulaType
	{
		RELATION,
		OR,
		FOR_ALL
	}
	abstract FormulaType getType(); 
	
	@Override
	public boolean models(FOStructure structure) throws FORuntimeException
	{	
		Map<FOVariable, FOElement> assignment = new HashMap<FOVariable, FOElement>();
		return checkAssignment(structure, assignment);
	}
	
	@Override
	public boolean isNegated()
	{
		return mNegated;
	}
	
	abstract void analyseVars(Set<FOVariable> setVarsInScope, Set<FOVariable> setVarsSeenInScope,
			Set<FOVariable> setFreeVars, List<String> listWarnings) throws FOConstructionException;
	
	static class FOFormulaBRRelation extends FOFormulaByRecursionImpl
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
		public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment) throws FORuntimeException
		{
			FOElement[] args = new FOElement[mTerms.size()]; 
			for(int i = 0; i < mTerms.size(); i++)
			{
				FOTerm term = mTerms.get(i);
				term.assignVariables(structure, assignment);
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
	}

	static class FOFormulaBROr extends FOFormulaByRecursionImpl
	{
		final protected List<FOFormula> mFormulas;
		FOFormulaBROr(boolean isNegated, List<FOFormula> formulas)
		{
			super(isNegated);
			mFormulas = formulas;
		}
		
		@Override
		public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment) throws FORuntimeException
		{
			for(FOFormula form : mFormulas)
				if(form.checkAssignment(structure, assignment))
					return !mNegated; // If we find satisfaction at any point we can quit.
			
			return mNegated;
		}
		
		Iterable<FOFormula> getFormulas()
		{
			return mFormulas;
		}

		@Override
		FormulaType getType() { return FormulaType.OR; }

		@Override
		void analyseVars(Set<FOVariable> setVarsInScope, Set<FOVariable> setVarsSeenInScope,
				Set<FOVariable> setFreeVars, List<String> listWarnings) throws FOConstructionException
		{
			for(FOFormula form : mFormulas)
				((FOFormulaByRecursionImpl) form).analyseVars(setVarsInScope, setVarsSeenInScope, setFreeVars, listWarnings);
		}
	}
	
	static class FOFormulaBRForAll extends FOFormulaByRecursionImpl
	{
		final protected FOVariable mVar;
		final protected FOFormula mScopeFormula;
		final protected boolean mIsFreeVariable;
		FOFormulaBRForAll(boolean isNegated, FOVariable var, FOFormula scopeFormula, boolean isFreVariable)
		{
			super(isNegated);
			mVar = var;
			mScopeFormula = scopeFormula;
			mIsFreeVariable = isFreVariable;
		}
		
		@Override
		public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment) throws FORuntimeException
		{
			assert !assignment.containsKey(mVar); // variable collision from earlier scope, this is illegal.
			
			boolean failed = false;
			for(FOElement elt : structure.getUniverse())
			{
				assignment.put(mVar, elt);
				failed |= !mScopeFormula.checkAssignment(structure, assignment);
				
				if(failed)
					break; // no point going further we know not all subformulas are satified.
			}
			assignment.remove(mVar); // we need to remove the variable assignment either way.
			
			return mNegated ^ !failed;
		}
		
		FOVariable getVariable()
		{
			return mVar;
		}
		FOFormula getScopeFormula()
		{
			return mScopeFormula;
		}
		
		@Override
		FormulaType getType() { return FormulaType.FOR_ALL; }

		@Override
		void analyseVars(Set<FOVariable> setVarsForScope, Set<FOVariable> setVarsSeenInScope,
				Set<FOVariable> setFreeVars, List<String> listWarnings) throws FOConstructionException
		{
			if(setVarsForScope.contains(mVar))
				throw new FOConstructionException(String.format("Variable name collision with variable for scope: %s", mVar.getName()));
			if(setVarsSeenInScope.contains(mVar))
				throw new FOConstructionException(String.format("Variable name collision between variable seen vs variable for scope: %s", mVar.getName()));
		
			// Start of scope
			setVarsForScope.add(mVar);
			Set<FOVariable> setVarsInMyScope = new LinkedHashSet<>();
			((FOFormulaByRecursionImpl) mScopeFormula).analyseVars(setVarsForScope, setVarsInMyScope, setFreeVars, listWarnings);
			
			if(!setVarsInMyScope.contains(mVar))
				listWarnings.add(String.format("Scope variable not used in scope: %s", mVar.getName()));			
			
			// Anything that I saw in my scope but that wasn't part of my set for scope has to be a free variable.
			setVarsInMyScope.removeAll(setVarsForScope);
			setFreeVars.addAll(setVarsInMyScope);
			
			setVarsForScope.remove(mVar);
			// End of scope
		}
	}
}
