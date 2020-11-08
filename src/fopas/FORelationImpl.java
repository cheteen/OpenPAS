package fopas;

import fopas.basics.FOElement;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;

abstract class FORelationImpl<T extends FOElement> implements FORelation<T>
{
	final String mName;
	
	FORelationImpl(String name)
	{
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
		public boolean satisfies(FOElement... args) throws FORuntimeException
		{
			if(args.length != 2)
				throw new FORuntimeException("Expected 2 args, got " + args.length + ".");
			if(args[0] == null || args[1] == null)
				throw new FORuntimeException(String.format("Got null arg(s): %s/%s", args[0], args[1]));
			
			return args[0].equals(args[1]);
		}

		@Override
		public int getCardinality() { return 2; }
	}
}
