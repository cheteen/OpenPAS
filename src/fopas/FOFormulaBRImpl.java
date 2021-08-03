//    Copyright (c) 2021 Burak Cetin
//
//    This file is part of OpenPAS.
//
//    OpenPAS is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    OpenPAS is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with OpenPAS.  If not, see <https://www.gnu.org/licenses/>.

package fopas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import fopas.basics.FOEnumerableSet;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOVariable;

public abstract class FOFormulaBRImpl implements FOFormula {

	final protected boolean mNegated;
	
	FOFormulaBRImpl(boolean isNegated)
	{
		mNegated = isNegated;
	}
	
	enum FormulaType
	{
		UNKNOWN,
		RELATION,
		OR,
		FOR_ALL,
		ALIAS,
		ALIAS_BINDING
	}
	abstract FormulaType getType(); 
	abstract <TI extends FOElement> FOSet<? extends FOElement> tryEliminateTrue(int depth, FOStructure structure, FOSet<TI> universeSubset, FOVariable var,
			boolean complement, Map<FOVariable, FOElement> assignment, Set<FOAliasBindingByRecursionImpl.AliasEntry> aliasCalls);
	abstract boolean checkAssignment(int depth, FOStructure structure,
			Map<FOVariable, FOElement> assignment);
	
	@Override
	public boolean models(FOStructure structure) throws FOConstructionException
	{	
		Set<FOVariable> setFreeVars = findFreeVars();
		if(setFreeVars.size() > 0)
		{
			boolean failed = false;
			for(Map<FOVariable, FOElement> pickings : getAssignments(structure))
			{
				failed |= !checkAssignment(0, structure, pickings);
				if(failed)
					break;
			}

			return !failed;			
		}
		else
		{
			HashMap<FOVariable, FOElement> assignment = new LinkedHashMap<>();			
			return checkAssignment(0, structure, assignment);
		}
	}
	
	@Override
	public Iterable<Map<FOVariable, FOElement>> getSatisfyingAssignments(FOStructure structure)
			throws FOConstructionException
	{
		return FluentIterable.from(
				getAssignments(structure)).filter(pickings -> checkAssignment(0, structure, pickings)
						);
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
	
	@Override
	public void checkFormula(FOStructure structure) throws FOConstructionException
	{
		// This triggers all the checks needed.
		findFreeVars();
	}

	static class FOSatisfactionIterator implements Iterator<Map<FOVariable, FOElement>>
	{
		protected final List<FOVariable> vars;
		protected final HashMap<FOVariable, FOElement> pickings;
		protected final List<FOEnumerableSet<FOElement>> pickables;
		protected final List<Iterator<FOElement>> pickers;
		protected Iterator<FOElement> focusIterator;
		protected int focusIteratorIx; 
		
		FOSatisfactionIterator(FOStructure structure, Set<FOVariable> setFreeVars)
		{
			vars = new ArrayList<>(setFreeVars);
			pickings = new LinkedHashMap<>();
			pickables = Collections.nCopies(setFreeVars.size(), (FOEnumerableSet<FOElement>) structure.getUniverse()); //the cast here may fail for non-enumerable universe and should be prevented earlier by the flow.
			pickers = new ArrayList<>(setFreeVars.size());
			for(int i = 0; i < pickables.size(); i++)
				pickers.add(Collections.emptyIterator());
			
			// Need to init the internal structure for things to start.
			// We need to create the iterator here so that a reference to the 
			// focus iterator can be made here.
			Iterator<FOElement> it = pickables.get(0).iterator();
			pickers.set(0, it);
			//if(it.hasNext())
			//	pickings.put(vars.get(0), it.next());
			
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

			// Uncomment for debugging.
//			StringBuilder sb = new StringBuilder();
//			sb.append("picking: ");
//			for(FOVariable var : pickings.keySet())
//				sb.append("[" + var.getName() + "=" + pickings.get(var).getElement() + "]");
//			System.out.println(sb.toString());				

			return pickings;
		}

		/**
		 * This picks M^N combinations of elements for M variables from a universe set with N elements.
		 * @param <IterableFOElts>
		 * @param pickers Iterators over the sets for the variables.
		 * @param ixPick Which variable we're picking.
		 * @param vars Variables we're picking for.
		 * @param pickings
		 * @param pickables Sets for the variables.
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
				// The iterator is fresh only at the very start.
				if(!it.hasNext())
				{
					it = pickables.get(ixPick).iterator();
					pickers.set(ixPick, it);
				}
				
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
	
	boolean presentNegated()
	{
		return mNegated;
	}
	
	String stringiseAssignments(Map<FOVariable, FOElement> assignment)
	{
		StringBuffer sb = new StringBuffer();
		sb.append('{');
		for(Map.Entry<FOVariable, FOElement> entry : assignment.entrySet())
		{
			if(sb.length() > 1)
				sb.append(", ");
			
			sb.append(entry.getKey().getName());
			sb.append('|');
			sb.append(entry.getValue().getElement());
		}
		sb.append('}');
		return sb.toString();
	}
	
	@Override
	public String toString()
	{
		return "FOFormulaBRImpl [" + FOByRecursionStringiser.getDefaultStringiser().stringiseFormula(this) + "]";
	}
}
