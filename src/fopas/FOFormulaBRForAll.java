package fopas;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.FOFormulaBRImpl.FormulaType;
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
		
		FOSettings settings = structure.getSettings();
		int trace = settings.getTraceLevel();
		if(trace >= 1)
		{
			settings.getStats().numL1CheckAsgAll++;
			if(trace >= 5)
			{
				settings.trace(-5, depth, this, "FOFormulaBRForAll", hashCode(), "checkAssignment", "%s", stringiseAssignments(assignment));
				settings.trace( 5, depth, this, "FOFormulaBRForAll", hashCode(), "checkAssignment", "Start eliminateTrue for variable: %s", mVar.getName());							
			}
		}
		
		// This will try to eliminate all known true cases. Note that we shouldn't use mNegate to negate the constrain here because
		// we need to eliminate the trues as far as this forall formula is concerned, and we'll negate the result as needed in the end.
		Set<FOFormulaBRRelation.AliasEntry> aliasCalls = new HashSet<>();
		FOSet<FOElement> constrained = mScopeFormula.eliminateTrue(depth + 1, structure, structure.getUniverse(), mVar, false, assignment, aliasCalls);

		if(trace >= 1)
		{
			if(constrained != structure.getUniverse())
			{
				settings.getStats().numL1ElimTrueSuccess++;
				if(constrained.size() == 1)
					settings.getStats().numL1ElimTrueSuccess1++;
				else
					settings.getStats().numL1ElimTrueSuccess0++;
			}
		
			settings.trace(2, depth, this, "FOFormulaBRForAll", hashCode(), "checkAssignment", "variable: %s, universeSubset: %s", mVar.getName(), constrained.getName());
		}
		assert aliasCalls.size() == 0;
		
		boolean failed = false;
		for(FOElement elt : constrained)
		{
			if(trace >= 1)
			{
				settings.getStats().numL1CheckAsgAllSub++;
				settings.trace(2, depth, this, "FOFormulaBRForAll", hashCode(), "checkAssignment", "Assigned %s=%s", mVar.getName(), elt.getElement());
			}
			
			assignment.put(mVar, elt);
			failed |= !mScopeFormula.checkAssignment(depth + 1, structure, assignment);
			
			if(failed)
			{
				if(trace >= 1)
				{
					settings.getStats().numL1CheckAsgAllSubFail++;
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
	public FOSet<FOElement> eliminateTrue(int depth, FOStructure structure, FOSet<FOElement> universe, FOVariable var,
			boolean complement, Map<FOVariable, FOElement> assignment, Set<FOFormulaBRRelation.AliasEntry> aliasCalls)
	{
		// The only thing to do is to see is if the scoped formula somehow already constrains our variable.
		return mScopeFormula.eliminateTrue(depth + 1, structure, universe, var, complement ^ mNegated, assignment, aliasCalls);
	}	
}