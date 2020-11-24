package fopas;

import java.util.Iterator;

import fopas.basics.KnownIterable;

public class CannedKnownIterable<T> implements KnownIterable<T>
{
	final Iterable<T> mIterable;
	final int mCannedSize;
	public CannedKnownIterable(Iterable<T> iterable, int cannedSize)
	{
		mIterable = iterable;
		mCannedSize = cannedSize;
	}
	
	@Override
	public Iterator<T> iterator()
	{
		return mIterable.iterator();
	}

	@Override
	public int size()
	{
		return mCannedSize;
	}
}
