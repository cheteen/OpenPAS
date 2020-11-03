package fopas;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fopas.basics.FOConstant;
import fopas.basics.FOElement;
import fopas.basics.FOFunction;
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
		public boolean assignVariables(FOStructure structure, Map<FOVariable, FOElement> assignment)
		{
			boolean accepted = false;

			List<FOElement> args = new ArrayList<FOElement>(mTerms.size());
			for(FOTerm term : mTerms)
			{
				accepted |= term.assignVariables(structure, assignment);
				args.add(term.getAssignment());
			}
			mAsg = mFunc.eval(structure, args);
			
			return accepted;
		}

		@Override
		public FOElement getAssignment()
		{
			return mAsg;
		}
	}
}
