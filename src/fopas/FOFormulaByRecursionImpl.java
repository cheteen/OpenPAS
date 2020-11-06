package fopas;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fopas.basics.FOElement;
import fopas.basics.FOFormula;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
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
	public boolean models(FOStructure structure) throws FORuntimeException
	{
		// TODO: Find out all free variables and \forall them here.
		// TODO: Find any variable collision (illegal) - can be during execution / nice to at the start.
		// TODO: Deal with any unassigned constants - can be during execution / nice to at the start.
		// TODO: Relations / functions wrong cardinality - can be during exeuction / nice to at the start.
		
		Map<FOVariable, FOElement> assignment = new HashMap<FOVariable, FOElement>();
		return checkAssignment(structure, assignment);
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
		public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment) throws FORuntimeException
		{
			FOElement[] args = new FOElement[mTerms.size()]; 
			for(int i = 0; i < mTerms.size(); i++)
			{
				FOTerm term = mTerms.get(i);
				term.assignVariables(structure, assignment);
				FOElement asg = term.getAssignment();
				assert asg != null; // All variables should be assigned by this point.
				args[i] = asg;
			}
			
			boolean satisfied = mRel.satisfies(args);
			return satisfied;
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
		public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment) throws FORuntimeException
		{
			for(FOFormula form : mFormulas)
				if(form.checkAssignment(structure, assignment))
					return true; // If we find satisfaction at any point we can quit.
			
			return false;
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
		public boolean checkAssignment(FOStructure structure, Map<FOVariable, FOElement> assignment) throws FORuntimeException
		{
			assert !assignment.containsKey(mVar); // variable collision from earlier scope, this is illegal.
			
			boolean failed = false;
			for(FOElement elt : structure.getUniverse())
			{
				assignment.put(mVar, elt);
				failed |= !mScopeFormula.checkAssignment(structure, assignment);
				
				if(failed)
					break; // no point going further we know not all subformulas are satified.
			}
			assignment.remove(mVar); // we need to remove the variable assignment either way.
			
			return !failed;
		}
	}
}
