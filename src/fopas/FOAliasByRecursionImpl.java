package fopas;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.basics.FOAlias;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORelation;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOVariable;

/**
 * Alias allows a formula to be named, and possibly used multiple time in another formula.
 * Also some simple sanity checks for the alias to make sure the arg variables are in the formula.
 */
// Definition of an "alias" is on Friendly p.127
public class FOAliasByRecursionImpl extends FOFormulaBRImpl implements FOAlias
{
	protected FOFormulaBRImpl mScopeForm;
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
	public boolean checkAssignment(int depth, FOStructure structure, Map<FOVariable, FOElement> assignment)
	{
		// Add caching here.
		return mNegated ^ mScopeForm.checkAssignment(depth + 1, structure, assignment);
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
		// TODO: This needs to check that this formula doesn't contain undeclared variables.
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
		
		mScopeForm = (FOFormulaBRImpl) form;
	}
	
	@Override
	public String getName()
	{
		return mName;
	}
	
	@Override
	public FOFormula negate()
	{
		assert false; // should never happen.
		return null; // not possible to negate this.
	}

	@Override
	public <TI extends FOElement> FOSet<? extends TI> tryEliminateTrue(int depth, FOStructure structure, FOSet<TI> universeSubset, FOVariable var,
			boolean complement, Map<FOVariable, FOElement> assignment, Set<FOAliasBindingByRecursionImpl.AliasEntry> aliasCalls)
	{
		// The only thing at this level to do is to handover the decision to the contained formula.
		return mScopeForm.tryEliminateTrue(depth + 1, structure, universeSubset, var, complement, assignment, aliasCalls);
	}
}
