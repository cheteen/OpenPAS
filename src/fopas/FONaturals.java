package fopas;

import java.util.Iterator;

import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOElement.Type;
import fopas.basics.FORelation;
import fopas.basics.FORuntimeException;
import fopas.basics.FOSet;

// Infinite set for \mathbb{N}.
public class FONaturals implements FOSet<FOInteger> {

	@Override
	public Iterator<FOInteger> iterator() {
		// TODO Do this with Guava? Even streams?
		return null;
	}

	/***
	 * Returns -1 to signify infinity.
	 */
	@Override
	public int size() {
		return -1; // Ok this is weird, but why else use a negative number, I think I can keep the consistency here.
	}

	@Override
	public String getName()
	{
		return "N";
	}

	@Override
	public boolean contains(Object o)
	{
		if(o == null || !o.getClass().isInstance(FOInteger.class))
			throw new FORuntimeException("Unexpected object: " + o);
		
		return ((FOInteger)o).getInteger() >= 0;
	}

	@Override
	public FOSet<FOInteger> createSubset(FORelation<FOInteger> rel)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSubsetSize(FORelation<FOInteger> rel)
	{
		// TODO Auto-generated method stub
		return 0;
	}

}
