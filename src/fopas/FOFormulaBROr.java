package fopas;

import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.FOFormulaBRImpl.FormulaType;
import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOFormula;
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
	public FOSet<FOElement> eliminateTrue(FOStructure structure, FOSet<FOElement> universe, FOVariable var,
			Map<FOVariable, FOElement> assignment)
	{
		// This can work in several ways which probably should be left as parameters to the programmer.
		// 1) Simple - find the formula that creates the smallest subset: O(N)
		// 2) Greedy - find the sequence of relations that create the smallest subset by choosing the smallest each iteration: O(N^2)
		// 3) Comprehensive - use each permutation of relations to find the best subset: O(N^2)

		// This particular method is called during runtime - so (1) and (2) are the likely best options to use here.
		// Another version of this would be good to consider during check time to look at using (3).
		
		// I'll implement only the simple option (1) at this point.
		
		// Another thing worth considering is to do another method on the relation to return the size of the subet without creating it.

		// Simple strategy:
		FOSet<FOElement> fosetSmallest = universe;
		for(FOFormula form : mFormulas)
		{
			FOSet<FOElement> subset = form.eliminateTrue(structure, universe, var, assignment);
			if(subset.size() < fosetSmallest.size())
			{
				fosetSmallest = subset;
				
				if(fosetSmallest.size() == 0)
					break;
			}
		}
		return fosetSmallest;
	}
}
