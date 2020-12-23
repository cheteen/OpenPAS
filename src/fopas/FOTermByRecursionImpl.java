package fopas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fopas.basics.FOConstant;
import fopas.basics.FOElement;
import fopas.basics.FOFunction;
import fopas.basics.FORuntimeException;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOVariable;

public abstract class FOTermByRecursionImpl implements FOTerm
{
	static class FOTermVariable extends FOTermByRecursionImpl
	{
		final protected FOVariable mVar;
		protected FOElement mAsg;
		
		public FOTermVariable(FOVariable var)
		{
			mVar = var;
		}
		
		@Override
		public void assignVariables(FOStructure structure, Map<FOVariable, FOElement> assignment, boolean isPartial)
		{
			FOElement elt = assignment.get(mVar);
			
			if(!isPartial && elt != null)
			{
				assert false;
				throw new FORuntimeException("Expected assignment not found for variable."); // This should never happen.
			}
			
			mAsg = elt;
		}

		@Override
		public FOElement getAssignment()
		{
			return mAsg;
		}

		@Override
		public TermType getType() { return TermType.VARIABLE; }
		
		FOVariable getVariable() { return mVar; }

		@Override
		void analyseScope(Set<FOVariable> setVarsSeenInScope)
		{
			setVarsSeenInScope.add(mVar);
		}

		@Override
		public void resetAssignment()
		{
			mAsg = null;
		}
	}
	
	abstract void analyseScope(Set<FOVariable> setVarsSeenInScope);
	
	static class FOTermConstant extends FOTermByRecursionImpl
	{
		final protected FOConstant mConst;
		protected FOElement mAsg;
		FOTermConstant(FOConstant foconst)
		{
			mConst = foconst;
		}
		
		@Override
		public void assignVariables(FOStructure structure, Map<FOVariable, FOElement> assignment, boolean isPartial)
		{
			if(mAsg != null)
				return; // already set
			mAsg = structure.getConstantMapping(mConst);
			
			//Partial doesn't apply to this since a constant always has to exist.
			if(mAsg != null)
			{
				assert false; // The structure has to have a mapping for this constant.
				throw new FORuntimeException("Expected variable assignment not found.");
			}
		}

		@Override
		public FOElement getAssignment()
		{
			return mAsg;
		}

		@Override
		public TermType getType() { return TermType.CONSTANT; }
		
		FOConstant getConstant() { return mConst; }

		@Override
		void analyseScope(Set<FOVariable> setVarsSeenInScope) {}

		@Override
		public void resetAssignment()
		{
			// I've decided not to reset the assignment for a constant for now - may need a better name for the function if this causes problems.
		}
	}
	
	static class FOTermFunction extends FOTermByRecursionImpl
	{
		final protected FOFunction mFunc;
		final protected List<FOTerm> mTerms;
		protected FOElement mAsg;
		FOTermFunction(FOFunction func, List<FOTerm> terms) 
		{
			mFunc = func;
			mTerms = terms;
		}
		
		@Override
		public void assignVariables(FOStructure structure, Map<FOVariable, FOElement> assignment, boolean isPartial)
		{
			FOElement[] args = new FOElement[mTerms.size()];
			for(int i = 0; i < mTerms.size(); i++)
			{
				FOTerm term = mTerms.get(i);
				term.assignVariables(structure, assignment, isPartial);
				args[i] = term.getAssignment();
			}
			mAsg = mFunc.eval(structure, args);
		}

		@Override
		public FOElement getAssignment()
		{
			return mAsg;
		}

		@Override
		public TermType getType() { return TermType.FUNCTION; }
		
		FOFunction getFunction() { return mFunc; }
		
		Iterable<FOTerm> getTerms() { return mTerms; }

		@Override
		void analyseScope(Set<FOVariable> setVarsSeenInScope)
		{
			for(FOTerm term : mTerms)
				((FOTermByRecursionImpl) term).analyseScope(setVarsSeenInScope);
		}

		@Override
		public void resetAssignment()
		{
			mAsg = null;
		}
	}
}
