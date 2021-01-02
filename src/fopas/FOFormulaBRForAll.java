package fopas;

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
	final protected FOFormula mScopeFormula;

	FOFormulaBRForAll(boolean isNegated, FOVariable var, FOFormula scopeFormula)
	{
		this(isNegated, var, scopeFormula, ForAllSubtype.FOR_ALL);
	}
	
	FOFormulaBRForAll(boolean isNegated, FOVariable var, FOFormula scopeFormula, FOFormulaBRForAll.ForAllSubtype subtype)
	{
		super(isNegated);
		mVar = var;
		mScopeFormula = scopeFormula;
		mSubtype = subtype;
	}
	
	@Override
	public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment)
	{
		if(assignment.containsKey(mVar)) // variable collision from earlier scope, this is illegal, should be caught during formula analysis.
			throw new FORuntimeException("Variable name collision for scope.");
		
		// This will try t oeliminate all known true cases:
		FOSet<FOElement> constrained = mScopeFormula.eliminateTrue(structure, structure.getUniverse(), mVar, assignment);
		
		boolean failed = false;
		for(FOElement elt : constrained)
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
	public FOSet<FOElement> eliminateTrue(FOStructure structure, FOSet<FOElement> universe, FOVariable var,
			Map<FOVariable, FOElement> assignment)
	{
		// The only thing to do is to see is if the scoped formula somehow already constrains our variable.
		return mScopeFormula.eliminateTrue(structure, universe, var, assignment);
	}
}