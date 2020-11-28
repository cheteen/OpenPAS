package fopas;

import java.util.function.Predicate;

import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;
import fopas.basics.FOStructure;
import fopas.basics.FOUnionSet;

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
	}
	
	static class FORelationCompare<T extends FOInteger> extends FORelationImpl<T>
	{
		protected final boolean mEqual;
		protected final boolean mGreater;
		FORelationCompare(boolean greater, boolean equal)
		{
			super(greater ? (equal ? "GreaterOrEqual" : "Greater") : (equal ? "LessThanOrEqual" : "LessThan"));
			mEqual = equal;
			mGreater = greater;
		}

		@Override
		public boolean satisfies(FOElement... args) 
		{
			FOInteger int1 = (FOInteger) args[0];
			FOInteger int2 = (FOInteger) args[1];
			
			if(mGreater)
				if(mEqual)
					return int2.getInteger() >= int1.getInteger();
				else
					return int2.getInteger() > int1.getInteger();
			else
				if(mEqual)
					return int2.getInteger() <= int1.getInteger();
				else
					return int2.getInteger() < int1.getInteger();
		}

		@Override
		public String getInfix()
		{
			return (mGreater ? (mEqual ? "GreaterOrEqual" : "Greater") : (mEqual ? "LessThanOrEqual" : "LessThan"));
		}

		@Override
		public int getCardinality()
		{
			return 2;
		}		
	}
	
	static class FORelationAnchoredCompare<T extends FOInteger> extends FORelationImpl<T>
	{
		protected final FORelationCompare<T> mRelCompare;
		protected final FOInteger mInt1;
		protected final FOInteger mInt2;
		/**
		 * One of int1 or int should normally be null.
		 * @param name
		 * @param relCompare
		 * @param int1
		 * @param int2
		 */
		FORelationAnchoredCompare(String name, FORelationCompare<T> relCompare, FOInteger int1, FOInteger int2)
		{
			super(name);
			mRelCompare = relCompare;
			mInt1 = int1;
			mInt2 = int2;
		}

		@Override
		public boolean satisfies(FOElement... args)
		{
			if(mInt1 == null)
				return mRelCompare.satisfies(args[0], mInt2);
			else if(mInt2 == null)
				return mRelCompare.satisfies(mInt1, args[0]);
			else // double anchored
				return mRelCompare.satisfies(mInt1, mInt2);
		}

		@Override
		public String getInfix() { return null; }

		@Override
		public int getCardinality()
		{
			if(mInt1 == null)
				return 1;
			else if(mInt2 == null)
				return 1;
			else // double anchored
				return 0;
		}
		
		public FORelationCompare<T> getInnerRelation()
		{
			return mRelCompare;
		}
		
		public FOInteger getAnchor1()
		{
			return mInt1;
		}
		public FOInteger getAnchor2()
		{
			return mInt2;
		}
	}
}
