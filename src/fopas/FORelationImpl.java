package fopas;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import fopas.basics.FOConstructionException;
import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOTerm;
import fopas.basics.FOUnionSet;
import fopas.basics.FOVariable;

abstract class FORelationImpl<T extends FOElement> implements FORelation<T>
{
	final String mName;
	
	FORelationImpl(String name)
	{
		//TODO: Validate name here.
		mName = name;
	}
	
	@Override
	public String getName()
	{
		return mName;
	}
	
	/**
	 * This is a special relation that binds to a set to return true
	 * when an element comes that is in the set.
	 */
	static class FORelationInSet extends FORelationImpl<FOElement>
	{
		//TODO: Actually implement this and with generic types.
		protected FOSet<FOElement> mSet;
		
		FORelationInSet(FOUnionSet universe, String setName)
		{
			super("InSet@" + setName);
			if(universe.getOriginalSubset(setName) == null)
				throw new FORuntimeException("Expected subset not found in univese: " + setName);
			mSet = universe.getOriginalSubset(setName);
		}

		@Override
		public boolean satisfies(FOElement... args)
		{
			if(args.length != 1)
				throw new FORuntimeException("Unexpected number of args.");
			return mSet.contains(args[0]);
		}

		@Override
		public String getInfix()
		{
			return null;
		}

		@Override
		public int getCardinality()
		{
			return 0;
		}

		FOSet<FOElement> getSet()
		{
			return mSet;
		}

		@Override
		public int getPrecedence()
		{
			return 2250;
		}

		@Override
		public <TI extends FOElement> FOSet<? extends TI> tryConstrain(FOVariable var, FOSet<TI> universeSubset,
				List<FOTerm> terms, boolean isComplemented)
		{
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Class<FOElement> getType() { return FOElement.class; }
	}
}
