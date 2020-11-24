package fopas;

import fopas.basics.FOElement;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOCombinedSet;

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
	
	static class FORelationImplEquals extends FORelationImpl<FOElement>
	{
		FORelationImplEquals()
		{
			super("Equals");
		}

		@Override
		public boolean satisfies(FOElement... args)
		{
			if(args.length != 2)
				throw new FORuntimeException("Expected 2 args, got " + args.length + ".");
			if(args[0] == null || args[1] == null)
				throw new FORuntimeException(String.format("Got null arg(s): %s/%s", args[0], args[1]));
			
			return args[0].equals(args[1]);
		}

		@Override
		public int getCardinality() { return 2; }

		@Override
		public String getInfix()
		{
			return "=";
		}
	}

	/**
	 * This is a special relation that binds to a set to return true
	 * when an element comes that is in the set.
	 */
	static class FORelationInSet extends FORelationImpl<FOElement>
	{
		protected FOSet<FOElement> mSet;
		
		FORelationInSet(FOCombinedSet universe, String setName)
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
	}
}
