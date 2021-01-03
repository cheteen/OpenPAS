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
		public FOSet<FOElement> tryConstrain(FOVariable var, FOSet<FOElement> universeSubset, List<FOTerm> terms, boolean isComplemented)
		{
			// TODO: Actually implement this.
			return universeSubset;
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

		@Override
		public int getPrecedence()
		{
			return 2500;
		}		

		@Override
		public FOSet<FOElement> tryConstrain(FOVariable var, FOSet<FOElement> universeSubset, List<FOTerm> terms, boolean isComplemented)
		{
			// TODO: Actually implement this.
			return universeSubset;
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

		@Override
		public int getPrecedence() throws FOConstructionException
		{
			throw new FOConstructionException("Unexpected operation found.");
		}
		
		@Override
		public FOSet<FOElement> tryConstrain(FOVariable var, FOSet<FOElement> universeSubset, List<FOTerm> terms, boolean isComplemented)
		{
			// TODO: Actually implement this.
			return universeSubset;
		}
	}
}
