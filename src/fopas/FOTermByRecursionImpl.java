package fopas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		public boolean assignVariables(FOStructure structure, Map<FOVariable, FOElement> assignment)
		{
			FOElement elt = assignment.get(mVar);
			assert elt != null; // by this point there can't be missing variable in the scope.
			
			mAsg = elt;
			return true;
		}

		@Override
		public FOElement getAssignment()
		{
			return mAsg;
		}

		@Override
		public TermType getType() { return TermType.VARIABLE; }
		
		FOVariable getVariable() { return mVar; }
	}
	
	static class FOTermConstant extends FOTermByRecursionImpl
	{
		final protected FOConstant mConst;
		protected FOElement mAsg;
		FOTermConstant(FOConstant foconst)
		{
			mConst = foconst;
		}
		
		@Override
		public boolean assignVariables(FOStructure structure, Map<FOVariable, FOElement> assignment)
		{
			if(mAsg != null)
				return false; // already set
			mAsg = structure.getConstantMapping(mConst);
			assert mAsg != null; // The structure has to have a mapping for this constant.
			
			return false; // this returns false for any further calls, but it does need the first call the set its first constant assignment.
		}

		@Override
		public FOElement getAssignment()
		{
			return mAsg;
		}

		@Override
		public TermType getType() { return TermType.CONSTANT; }
		
		FOConstant getConstant() { return mConst; }
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
		public boolean assignVariables(FOStructure structure, Map<FOVariable, FOElement> assignment) throws FORuntimeException
		{
			boolean accepted = false;

			FOElement[] args = new FOElement[mTerms.size()];
			for(int i = 0; i < mTerms.size(); i++)
			{
				FOTerm term = mTerms.get(i);
				accepted |= term.assignVariables(structure, assignment);
				args[i] = term.getAssignment();
			}
			mAsg = mFunc.eval(structure, args);
			
			return accepted;
		}

		@Override
		public FOElement getAssignment()
		{
			return mAsg;
		}

		@Override
		public TermType getType() { return TermType.FUNCTION; }
		
		FOFunction getFunction() { return mFunc; }
	}
}
