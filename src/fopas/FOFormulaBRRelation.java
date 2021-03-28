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

import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.FOFormulaBRImpl.FormulaType;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORelation;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOVariable;

class FOFormulaBRRelation extends FOFormulaBRImpl
{
	final protected FORelation<? extends FOElement> mRel;
	final protected List<FOTerm> mTerms;
	FOFormulaBRRelation(boolean isNegated, FORelation<? extends FOElement> rel, List<FOTerm> terms)
	{
		super(isNegated);
		mRel = rel;
		mTerms = terms;
	}
	
	@Override
	public boolean checkAssignment(int depth, FOStructure structure, Map<FOVariable, FOElement> assignment)
	{
		FORuntime settings = structure.getRuntime();
		int trace = settings.getTraceLevel();
		if(trace >= 1)
		{
			settings.getStats().numL1CheckAsgRel++;
			settings.trace(-5, depth, this, "FOFormulaBRRelation", hashCode(), "checkAssignment", "%s", trace >= 5 ? stringiseAssignments(assignment) : "");
		}

		FOElement[] args = new FOElement[mTerms.size()]; 
		for(int i = 0; i < mTerms.size(); i++)
		{
			FOTerm term = mTerms.get(i);
			term.assignVariables(structure, assignment, false);
			FOElement asg = term.getAssignment();
			assert asg != null; // All variables should be assigned by this point.
			args[i] = asg;
		}
		
		boolean satisfied = mRel.satisfies(args);
		settings.trace(5, depth, this, "FOFormulaBRRelation", hashCode(), "checkAssignment", "satisfaction: %s (return: %s)", satisfied, mNegated ^ satisfied);
		
		return mNegated ^ satisfied;
	}

	@Override
	FormulaType getType() { return FormulaType.RELATION; }
	
	FORelation<? extends FOElement> getRelation()
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

	@Override
	public FOFormula negate()
	{
		return new FOFormulaBRRelation(!mNegated, mRel, mTerms);
	}

	@Override
	public <TI extends FOElement> FOSet<? extends TI> tryEliminateTrue(int depth, FOStructure structure, FOSet<TI> universeSubset, FOVariable var,
			boolean complement, Map<FOVariable, FOElement> assignment, Set<FOAliasBindingByRecursionImpl.AliasEntry> aliasCalls)
	{
		FORuntime settings = structure.getRuntime();
		if(settings.getTraceLevel() >= 1)
		{
			// This is the only place that truly does elimination.
			settings.getStats().incrementedStat("numL1ElimTrueRelAttempts", ++settings.getStats().numL1ElimTrueRelAttempts, settings.getTraceLevel(), this);
			if(settings.getTraceLevel() >= 5)
			{
				settings.trace(-5, depth, this, "FOFormulaBRRelation", hashCode(), "eliminateTrue", "(partial for %s) %s", var.getName(), stringiseAssignments(assignment));
				settings.trace( 5, depth, this, "FOFormulaBRRelation", hashCode(), "eliminateTrue", "variable: %s, complement: %s, universe: %s", var.getName(), complement, universeSubset.getName());				
			}
		}
		
		// Let's deal with type issues.
		
		// First off, we want to be able to pass an incompatible type to mRel to this method (ie. we don't want this stopped in compile time),
		// because we want the OPAS runtime to deal with it (as immediately below) by returning the appropriate (universe/empty) set instead.
		// This is why we aren't properly generic typing this class (but instead using an anonymous type capture) for mRel.
		
		// If the universeSubset is not of a type that the rel can supply, then we can't do elimTrue here (even though there may be true members).
		// We _could_ try and do it, but it would be too complex to implement for anyone, and would be impossible to understand the code or use type checks.
		// Instead we rely on re-ordering the elimTrues in FOFormulaBROr to resolve this problem.
		if(!mRel.getType().isAssignableFrom(universeSubset.getType()))
			return null; // TODO: Unit test this.
		// From this point on we know the universeSubset is of a type that the rel supports.

		// Do a partial assignment to the terms.
		for(FOTerm term : mTerms)
			term.assignVariables(structure, assignment, true);
		
		// Let's see if the relation can constrain its universe from here.
		// Constrain tries to return elements of the universe where the relation is true.
		// We complement this set to eliminate elements that are known to be true.
		// We don't need to complement it if this formula has a negation of course.
		//
		// As discussed above, we deliberately erase the type here from compiler checks because we have already established above (in runtime)
		// that the universe contains a type that's a descendant (assignable) of the type the rel needs. This is per the tryConstrain interface
		// which says <TI extends T> FOSet<TI> where T is the type for the relation and TI is the type of the universeSubset.
		@SuppressWarnings({ "unchecked", "rawtypes" })
		FOSet<? extends TI> constrained = mRel.tryConstrain(var, (FOSet) universeSubset, mTerms, complement ^ !mNegated);

		settings.trace(5, depth, this, "FOFormulaBRRelation", hashCode(), "eliminateTrue", 
				"Elimination variable: %s, success: %s, subset: %s", var.getName(), constrained != null,
				constrained == null ? "<null>" : constrained.getName());

		return constrained;
	}
}
