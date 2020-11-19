package fopas;

import java.util.Iterator;

import fopas.basics.FOElement;
import fopas.basics.FOSet;
import fopas.basics.FOUniverse;

public class FOUniverseImpl implements FOUniverse
{
	FOSet<FOElement> mDefault;
	FOUniverseImpl(FOSet<FOElement> defaultSet)
	{
		mDefault = defaultSet;
	}

	@Override
	public Iterator<FOElement> iterator()
	{
		return mDefault.iterator();
	}

}
