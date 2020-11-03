package fopas;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORelation;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOVariable;

abstract class FOFormulaByRecursionImpl implements FOFormula {
	
	final protected boolean mNegated;
	
	FOFormulaByRecursionImpl(boolean isNegated)
	{
		mNegated = isNegated;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterator iterator() {
		// TODO Auto-generated method stub
		return null;
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
		public boolean assignVariables(FOStructure structure, Map<FOVariable, FOElement> assignment)
		{
			boolean accepted = false;
			
			for(FOTerm term : mTerms)
				accepted |= term.assignVariables(structure, assignment);
			
			return accepted;
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
		public boolean assignVariables(FOStructure structure, Map<FOVariable, FOElement> assignment)
		{
			boolean accepted = false;
			
			for(FOFormula form : mFormulas)
				accepted |= form.assignVariables(structure, assignment);
			
			return accepted;
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
		public boolean assignVariables(FOStructure structure, Map<FOVariable, FOElement> assignment)
		{
			boolean accepted = false;
			assert !assignment.containsKey(mVar); // variable collision from earlier scope, this is illegal.
			
			for(FOElement elt : structure.getUniverse())
			{
				assignment.put(mVar, elt);
				accepted |= mScopeFormula.assignVariables(structure, assignment);
				
				if(!accepted)
					break; // no point going further, the subformula doesn't use these variables.
			}
			assignment.remove(mVar);
			
			return accepted;
		}
	}
}
