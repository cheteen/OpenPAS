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

public abstract class FOFormulaByRecursionImpl implements FOFormula {
	
	final protected boolean mNegated;
	
	FOFormulaByRecursionImpl(boolean isNegated)
	{
		mNegated = isNegated;
	}
	
	enum FormulaType
	{
		UNKNOWN,
		RELATION,
		OR,
		FOR_ALL
	}
	abstract FormulaType getType(); 
	
	@Override
	public boolean models(FOStructure structure) throws FOConstructionException
	{	
		Set<FOVariable> setFreeVars = findFreeVars();
		if(setFreeVars.size() > 0)
		{
			boolean failed = false;
			for(Map<FOVariable, FOElement> pickings : getAssignments(structure))
			{
				failed |= !checkAssignment(structure, pickings);
				if(failed)
					break;
			}

			return !failed;			
		}
		else
		{
			HashMap<FOVariable, FOElement> assignment = new HashMap<>();			
			return checkAssignment(structure, assignment);
		}
	}
	
	@Override
	public Iterable<Map<FOVariable, FOElement>> getSatisfyingAssignments(FOStructure structure)
			throws FOConstructionException
	{
		return FluentIterable.from(getAssignments(structure)).filter(pickings -> checkAssignment(structure, pickings));
	}

	public Iterable<Map<FOVariable, FOElement>> getAssignments(FOStructure structure)
			throws FOConstructionException
	{
		Set<FOVariable> setFreeVars = findFreeVars();
		return new Iterable<Map<FOVariable,FOElement>>() {
			@Override
			public Iterator<Map<FOVariable, FOElement>> iterator()
			{
				if(setFreeVars.isEmpty())
					return Collections.emptyIterator();
				return new FOSatisfactionIterator(structure, setFreeVars);
			}
		};
	}

	static class FOSatisfactionIterator implements Iterator<Map<FOVariable, FOElement>>
	{
		protected final List<FOVariable> vars;
		protected final HashMap<FOVariable, FOElement> pickings;
		protected final List<Iterable<FOElement>> pickables;
		protected final List<Iterator<FOElement>> pickers;
		protected Iterator<FOElement> focusIterator;
		protected int focusIteratorIx; 
		
		FOSatisfactionIterator(FOStructure structure, Set<FOVariable> setFreeVars)
		{
			vars = new ArrayList<>(setFreeVars);
			pickings = new HashMap<>();
			pickables = Collections.nCopies(setFreeVars.size(), structure.getUniverse());
			pickers = new ArrayList<>(setFreeVars.size());
			for(int i = 0; i < pickables.size(); i++)
				pickers.add(Collections.emptyIterator());
			
			// Need to init the internal structure for things to start.
			Iterator<FOElement> it = pickables.get(0).iterator();
			pickers.set(0, it);
			if(it.hasNext())
				pickings.put(vars.get(0), it.next());
			
			// Point the focus to the above.
			focusIteratorIx = 0;
			focusIterator = pickers.get(focusIteratorIx);
		}

		@Override
		public boolean hasNext()
		{
			//Keep a direct reference to the last iterator keeps a tight(er) inner loop.
			if(focusIterator.hasNext())
				return true;
			
			// Move to the next iterator if there's one:
			while(focusIteratorIx + 1 < pickers.size())
			{
				focusIterator = pickers.get(++focusIteratorIx);
				if(focusIterator.hasNext())
					return true;
			}
			return false;
		}

		/**
		 * This always returns the same Map object (don't modify it!) but it's updated each time next called.
		 */
		@Override
		public Map<FOVariable, FOElement> next()
		{
			pickElements(pickers, 0, vars, pickings, pickables);
			return pickings;
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
			// Not fond of this implementation below, need something cleaner and less error prone if possible.
			
			FOVariable var = vars.get(ixPick);
			Iterator<FOElement> it = pickers.get(ixPick);
			
			// Starting fresh new iteration on this level.
			if(!pickings.containsKey(var))
			{
				//Initialise picker if we're starting anew.
				it = pickables.get(ixPick).iterator();
				pickers.set(ixPick, it);
				
				// Fresh start has empty iterator.
				if(!it.hasNext())
					return false;

				// First picking comes free.
				pickings.put(var, it.next());
				
				// Free is good enough for leaf node.
				if(ixPick + 1 == pickers.size())
					return true;
			}
			
			// Leaf node.
			if(ixPick + 1 == pickers.size())
			{
				if(!it.hasNext())
				{
					pickings.remove(var);
					return false;					
				}
				pickings.put(var, it.next());
				return true;
			}

			// Check if next level is out.
			if(!pickElements(pickers, ixPick + 1, vars, pickings, pickables))
			{
				if(!it.hasNext() || // We are out too, or,
						!pickElements(pickers, ixPick + 1, vars, pickings, pickables)) // tried to refresh next level and failed (empty next level picker).
				{
					pickings.remove(var);
					return false;					
				}

				// Next level was out, and we refreshed it. We can advance this level, so we should do it now.
				pickings.put(var, it.next());
			}
			
			return true;
		}
	}
	
	@Override
	public boolean isNegated()
	{
		return mNegated;
	}
	
	abstract void analyseVars(Set<FOVariable> setVarsInScope, Set<FOVariable> setVarsSeenInScope,
			Set<FOVariable> setFreeVars, List<String> listWarnings) throws FOConstructionException;
	
	
	Set<FOVariable> findFreeVars() throws FOConstructionException
	{
		Set<FOVariable> setVarsInScope = new LinkedHashSet<>(); // use linked hash set here so we get consistent results each time.
		Set<FOVariable> setVarsSeenInScope = new LinkedHashSet<>();
		Set<FOVariable> setFreeVars = new LinkedHashSet<>();
		List<String> listWarnings = new ArrayList<>();
		
		analyseVars(setVarsInScope, setVarsSeenInScope, setFreeVars, listWarnings);
		
		// Anything that's in my scope here is a free variable, since we're really in a kind of hidden scope here
		// for all the free variables.
		assert setVarsInScope.isEmpty();
		setVarsSeenInScope.addAll(setFreeVars); // use own scope variables first
		setFreeVars = setVarsSeenInScope;
		return setFreeVars;
	}
	
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
		public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment)
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
		public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment)
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
		FOFormulaBRForAll(boolean isNegated, FOVariable var, FOFormula scopeFormula)
		{
			super(isNegated);
			mVar = var;
			mScopeFormula = scopeFormula;
		}
		
		@Override
		public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment)
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
