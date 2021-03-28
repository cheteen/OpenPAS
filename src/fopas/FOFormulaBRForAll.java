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

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.FOFormulaBRImpl.FormulaType;
import fopas.FORuntime.FOStats;
import fopas.basics.FOEnumerableSet;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOVariable;

class FOFormulaBRForAll extends FOFormulaBRImpl
{
	enum ForAllSubtype
	{
		FOR_ALL,
		EXISTS
	}
	final protected FOFormulaBRForAll.ForAllSubtype mSubtype;
	final protected FOVariable mVar;
	final protected FOFormulaBRImpl mScopeFormula;

	FOFormulaBRForAll(boolean isNegated, FOVariable var, FOFormulaBRImpl scopeFormula)
	{
		this(isNegated, var, scopeFormula, ForAllSubtype.FOR_ALL);
	}
	
	FOFormulaBRForAll(boolean isNegated, FOVariable var, FOFormulaBRImpl scopeFormula, FOFormulaBRForAll.ForAllSubtype subtype)
	{
		super(isNegated);
		mVar = var;
		mScopeFormula = scopeFormula;
		mSubtype = subtype;
	}
	
	@Override
	public boolean checkAssignment(int depth, FOStructure structure, Map<FOVariable, FOElement> assignment)
	{
		if(assignment.containsKey(mVar)) // variable collision from earlier scope, this is illegal, should be caught during formula analysis.
			throw new FORuntimeException("Variable name collision for scope.");
		
		//stats.incrementedStat("numL1ElimTrueRelAttempts", ++stats.numL1ElimTrueRelAttempts, settings.getTraceLevel(), this);

		FORuntime settings = structure.getRuntime();
		int trace = settings.getTraceLevel();
		FOStats stats = settings.getStats();
		if(trace >= 1)
		{
			stats.incrementedStat("numL1CheckAsgAll", ++stats.numL1CheckAsgAll, settings.getTraceLevel(), this);
			if(trace >= 5)
			{
				settings.trace(-5, depth, this, "FOFormulaBRForAll", hashCode(), "checkAssignment", "%s", stringiseAssignments(assignment));
				settings.trace( 5, depth, this, "FOFormulaBRForAll", hashCode(), "checkAssignment", "Start eliminateTrue for variable: %s", mVar.getName());							
			}
		}
		
		// This will try to eliminate all known true cases. Note that we shouldn't use mNegate to negate the constrain here because
		// we need to eliminate the trues as far as this forall formula is concerned, and we'll negate the result as needed in the end.
		Set<FOAliasBindingByRecursionImpl.AliasEntry> aliasCalls = new HashSet<>();
		FOSet<? extends FOElement> constrained = mScopeFormula.tryEliminateTrue(depth + 1, structure, structure.getUniverse(), mVar, false, assignment, aliasCalls);
		
		if(trace >= 1)
		{
			if(constrained != null)
			{
				stats.incrementedStat("numL1ElimTrueForallSuccess", ++stats.numL1ElimTrueForallSuccess, settings.getTraceLevel(), this);
				if(constrained.size() == 1)
					stats.incrementedStat("numL1ElimTrueForallSuccess1", ++stats.numL1ElimTrueForallSuccess1, settings.getTraceLevel(), this);					
				else if(constrained.size() == 0)
					stats.incrementedStat("numL1ElimTrueForallSuccess0", ++stats.numL1ElimTrueForallSuccess0, settings.getTraceLevel(), this);					

				settings.trace(2, depth, this, "FOFormulaBRForAll", hashCode(), "checkAssignment", "eliminateTrue for %s success new universeSubset: %s", mVar.getName(), constrained.getName());
			}
			else
			{
				stats.incrementedStat("numL1ElimTrueForallFail", ++stats.numL1ElimTrueForallFail, settings.getTraceLevel(), this);					

				settings.trace(5, depth, this, "FOFormulaBRForAll", hashCode(), "checkAssignment", "eliminateTrue for %s failed. Using existing universeSubset: %s", mVar.getName(), structure.getUniverse().getName());
			}
		}
		assert aliasCalls.size() == 0;
		
		if(constrained == null)
			constrained = structure.getUniverse();
		
		if(!(constrained instanceof FOEnumerableSet))
			throw new FORuntimeException("Attempting to iterate non-emumerable set."); // TODO: Unit test this.
		if(constrained.size() == Integer.MAX_VALUE)
			throw new FORuntimeException("Attempting to iterate infinite set."); // TODO: Unit test this.
		
		FOEnumerableSet<? extends FOElement> enumerableConstrained = (FOEnumerableSet<? extends FOElement>) constrained; 

		boolean failed = false;
		for(FOElement elt : enumerableConstrained)
		{
			if(trace >= 1)
			{
				stats.incrementedStat("numL1CheckAsgAllSub", ++stats.numL1CheckAsgAllSub, settings.getTraceLevel(), this);					
				settings.trace(2, depth, this, "FOFormulaBRForAll", hashCode(), "checkAssignment", "Assigned %s=%s for %s", mVar.getName(), elt.getElement(), settings.stringiseFormulaForTrace(2, this));
			}
			
			assignment.put(mVar, elt);
			failed |= !mScopeFormula.checkAssignment(depth + 1, structure, assignment);
			
			if(failed)
			{
				if(trace >= 1)
				{
					stats.incrementedStat("numL1CheckAsgAllSubFail", ++stats.numL1CheckAsgAllSubFail, settings.getTraceLevel(), this);					
					settings.trace(2, depth, this, "FOFormulaBRForAll", hashCode(), "checkAssignment", "Assignment failed for %s", elt.getElement());
				}
				break; // no point going further we know not all subformulas are satified.
			}
		}
		assignment.remove(mVar); // we need to remove the variable assignment either way.

		settings.trace(2, depth, this, "FOFormulaBRForAll", hashCode(), "checkAssignment", "satisfaction: %s (return: %s)", !failed, mNegated ^ !failed);

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
		((FOFormulaBRImpl) mScopeFormula).analyseVars(setVarsForScope, setVarsInMyScope, setFreeVars, listWarnings);
		
		if(!setVarsInMyScope.contains(mVar))
			listWarnings.add(String.format("Scope variable not used in scope: %s", mVar.getName()));			
		
		// Anything that I saw in my scope but that wasn't part of my set for scope has to be a free variable.
		setVarsInMyScope.removeAll(setVarsForScope);
		setFreeVars.addAll(setVarsInMyScope);
		
		setVarsForScope.remove(mVar);
		// End of scope
	}

	@Override
	public FOFormula negate()
	{
		return new FOFormulaBRForAll(!mNegated, mVar, mScopeFormula, mSubtype);
	}
	
	FOFormulaBRForAll.ForAllSubtype getOriginalSubtype()
	{
		return mSubtype;
	}
	
	// How formula is presented.
	FOFormulaBRForAll.ForAllSubtype presentSubtype()
	{
		return ForAllSubtype.FOR_ALL;
	}
	FOFormula presentScopeFormula()
	{
		return mScopeFormula;
	}
	@Override
	public <TI extends FOElement> FOSet<? extends TI> tryEliminateTrue(int depth, FOStructure structure, FOSet<TI> universeSubset, FOVariable var,
			boolean complement, Map<FOVariable, FOElement> assignment, Set<FOAliasBindingByRecursionImpl.AliasEntry> aliasCalls)
	{
		// The only thing to do is to see is if the scoped formula somehow already constrains our variable.
		return mScopeFormula.tryEliminateTrue(depth + 1, structure, universeSubset, var, complement ^ mNegated, assignment, aliasCalls);
	}	
}