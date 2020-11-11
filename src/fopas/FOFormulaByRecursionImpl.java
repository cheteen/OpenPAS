package fopas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

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
	public boolean models(FOStructure structure, Set<FOVariable> setFreeVars) throws FORuntimeException
	{	
		if(setFreeVars.size() > 0)
		{
			List<FOVariable> vars = new ArrayList<>(setFreeVars);
			HashMap<FOVariable, FOElement> pickings = new HashMap<>();
			List<FOSet<FOElement>> pickables = Collections.nCopies(setFreeVars.size(), structure.getUniverse());
			List<Iterator<FOElement>> pickers = new ArrayList<>(setFreeVars.size());
			for(int i = 0; i < pickables.size(); i++) // Initialise the first set of pickers.
				pickers.add(pickables.get(i).iterator());

			// The formula needs to hold for _all_ the possible assignments.
			boolean failed = false;
			while(!failed && pickElements(pickers, 0, vars, pickings, pickables))
				failed |= !checkAssignment(structure, pickings);

			return !failed;			
		}
		else
		{
			HashMap<FOVariable, FOElement> assignment = new HashMap<>();			
			return checkAssignment(structure, assignment);
		}
	}
	
	@Override
	public Iterable<Map<FOVariable, FOElement>> getSatisfyingAssignments(FOStructure structure, Set<FOVariable> setFreeVars)
			throws FOConstructionException
	{
		final List<FOVariable> vars = new ArrayList<>(setFreeVars);
		final HashMap<FOVariable, FOElement> pickings = new HashMap<>();
		final List<FOSet<FOElement>> pickables = Collections.nCopies(setFreeVars.size(), structure.getUniverse());
		final List<Iterator<FOElement>> pickers = new ArrayList<>(setFreeVars.size());
		for(int i = 0; i < pickables.size(); i++) // Initialise the first set of pickers.
			pickers.add(pickables.get(i).iterator());

		return null; // TODO: Redesign this.
	}
	
	/**
	 * This picks M^N combinations of elements for M variables from a universe set with N elements.
	 * @param <IterableFOElts>
	 * @param pickers
	 * @param ixPick
	 * @param vars
	 * @param pickings
	 * @param pickables
	 * @return true if there's another combination of variable to pick yet, false when done.
	 */
	<IterableFOElts extends Iterable<FOElement>> boolean pickElements(List<Iterator<FOElement>> pickers, int ixPick, 
			List<FOVariable> vars, Map<FOVariable, FOElement> pickings, List<IterableFOElts> pickables)
	{
		FOVariable var = vars.get(ixPick);
		Iterator<FOElement> it = pickers.get(ixPick);

		// Middle node
		if(ixPick + 1 < pickers.size())
		{
			if(!pickings.containsKey(var))
			{
				if(it.hasNext())
					pickings.put(var, it.next());
				else
					return false; // empty pickable?
			}

			if(pickElements(pickers, ixPick + 1, vars, pickings, pickables))
				return true;
			
			while(it.hasNext()) // this is "while" (and not "if") just in case we have an empty pickable at the next level
			{
				pickings.put(var, it.next());
				
				pickers.set(ixPick + 1, pickables.get(ixPick + 1).iterator());
				if(pickElements(pickers, ixPick + 1, vars, pickings, pickables))
					return true;
			}
			
			// Can't pickElements, don't have next, time to fail.
			pickings.remove(var);
			return false;
		}
		else // Leaf node
		{
			if(it.hasNext())
			{
				pickings.put(var, it.next());
				return true;
			}
			else
			{
				pickings.remove(var); // this is just nice to have for consistency really.
				return false;
			}
		}
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
			
			//TODO: Call into the structure here to do variable value selection (which will use and iterable hopefully).
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
