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
import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.FOFormulaBRImpl.FormulaType;
import fopas.FORuntime.FOStats;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORelation;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOVariable;

class FOFormulaBROr extends FOFormulaBRImpl
{
	static enum OrSubType
	{
		OR,
		AND,
		IMP // implication
	}
	
	final protected List<FOFormula> mFormulas;
	final FOFormulaBROr.OrSubType mSubType;
	
	FOFormulaBROr(boolean isNegated, List<FOFormula> formulas, FOFormulaBROr.OrSubType type)
	{
		super(isNegated);
		mFormulas = formulas;
		mSubType = type;
	}

	FOFormulaBROr(boolean isNegated, List<FOFormula> formulas)
	{
		this(isNegated, formulas, OrSubType.OR);
	}

	@Override
	public boolean checkAssignment(int depth, FOStructure structure, Map<FOVariable, FOElement> assignment)
	{
		FORuntime settings = structure.getRuntime();
		int trace = settings.getTraceLevel();
		if(trace >= 1)
		{
			settings.getStats().numL1CheckAsgOr++;
			settings.trace(-5, depth, this, "FOFormulaBROr", hashCode(), "checkAssignment", "%s", trace >= 5 ? stringiseAssignments(assignment) : "");			
		}
		
		for(FOFormula form : mFormulas)
		{
			FOFormulaBRImpl formimpl = (FOFormulaBRImpl)form; 
			if(formimpl.checkAssignment(depth + 1, structure, assignment))
			{
				if(trace >= 5)
					settings.trace(5, depth, this, "FOFormulaBROr", hashCode(), "checkAssignment", "Satisfied (return: %s).", !mNegated);
				return !mNegated; // If we find satisfaction at any point we can quit.
			}
		}
		
		settings.trace(5, depth, this, "FOFormulaBROr", hashCode(), "checkAssignment", "Not satisfied (return: %s).", mNegated);
		
		return mNegated;
	}

	@Override
	FormulaType getType() { return FormulaType.OR; }

	@Override
	void analyseVars(Set<FOVariable> setVarsInScope, Set<FOVariable> setVarsSeenInScope,
			Set<FOVariable> setFreeVars, List<String> listWarnings) throws FOConstructionException
	{
		for(FOFormula form : mFormulas)
			((FOFormulaBRImpl) form).analyseVars(setVarsInScope, setVarsSeenInScope, setFreeVars, listWarnings);
	}

	@Override
	public FOFormula negate()
	{
		return new FOFormulaBROr(!mNegated, mFormulas, mSubType);
	}
	
	/**
	 * The type this formula wants to be when presented (e.g. turned to string) is this.
	 * Subclass this to present other types, e.g. an AND sentence or a HORN clause.
	 */
	FOFormulaBROr.OrSubType getOriginalSubType()
	{
		return mSubType;
	}
	
	/**
	 * The type the interface variables work right now is like this. All implementations are presenting as ORs
	 * in this class since OR is the only implementation defined.
	 */
	FOFormulaBROr.OrSubType presentSubType()
	{
		return OrSubType.OR;
	}
	
	Iterable<FOFormula> presentFormulas()
	{
		return mFormulas;
	}

	@Override
	public <TI extends FOElement> FOSet<? extends FOElement> tryEliminateTrue(int depth, FOStructure structure, FOSet<TI> universeSubset, FOVariable var,
			boolean complement, Map<FOVariable, FOElement> assignment, Set<FOAliasBindingByRecursionImpl.AliasEntry> aliasCalls)
	{
		FORuntime settings = structure.getRuntime();
		if(settings.getTraceLevel() >= 5)
		{
			settings.trace(-5, depth, this, "FOFormulaBROr", hashCode(), "eliminateTrue", "(partial for %s) %s", var.getName(), stringiseAssignments(assignment));
			settings.trace( 5, depth, this, "FOFormulaBROr", hashCode(), "eliminateTrue", "variable: %s, complement: %s, universe: %s", var.getName(), complement, universeSubset.getName());			
		}
		
		int elimTarget = settings.getTargetElimTrue();
		
		// There are three possible strategies that I can think of:
		// (1) Simple O(N): iteratively constrain the universe subset one by one. As long as there's one order and ordered sets this should create the ideal solution.
		//     This also relies on the type selection to be at the start. If that's failed, it can cause a runtime failure in a relation refusing to act on a universe subset.
		// (2) Greedy O(NxN): This can pick up one layer find out the best, take the best, and repeat.
		// (3) Exhaustive O(N!): Follow any possible lineage to find the absolute best combination.
		
		FOSet<? extends FOElement> fosetResultSubset;
		
		{
			// Simple strategy:
			FOSet<? extends FOElement> fosetSubset = universeSubset;
			for(FOFormula form : mFormulas)
			{
				FOFormulaBRImpl formimpl = (FOFormulaBRImpl) form; 
				FOSet<? extends FOElement> fosetSubsetNext = formimpl.tryEliminateTrue(depth + 1, structure, fosetSubset, var, false, assignment, aliasCalls);

				if(fosetSubsetNext == null)
					continue; // TODO: This needs a marker for an incomplete set.
				
				fosetSubset = fosetSubsetNext;
				
				if(fosetSubset.size() <= elimTarget)
					break;
			}
			// TODO: Add the new counts here.
			
			if(fosetSubset == universeSubset)
				fosetResultSubset = null; //failed to constrain
			else
			{ // Let's deal with the complementing
				if(complement ^ mNegated)
					fosetResultSubset = fosetSubset.complementAcross(universeSubset);
				else
					fosetResultSubset = fosetSubset;
			}			
		}
		
		settings.trace(5, depth, this, "FOFormulaBROr", hashCode(), "eliminateTrue", 
				"Elimination variable: %s, success: %s, smallest subset: %s, negate: %s, complementing: %s", var.getName(), fosetResultSubset != null,
						fosetResultSubset == null ? "<null>" : fosetResultSubset.getName(), mNegated, mNegated ^ complement);
		
		return fosetResultSubset;
	}
}
