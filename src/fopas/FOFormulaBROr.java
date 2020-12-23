package fopas;

import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.FOFormulaBRImpl.FormulaType;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
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
	public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment)
	{
		for(FOFormula form : mFormulas)
			if(form.checkAssignment(structure, assignment))
				return !mNegated; // If we find satisfaction at any point we can quit.
		
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
		return new FOFormulaBROr(!mNegated, mFormulas);
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
	public void resetAssignment()
	{
		for(FOFormula form : mFormulas)
			form.resetAssignment();
	}		
}