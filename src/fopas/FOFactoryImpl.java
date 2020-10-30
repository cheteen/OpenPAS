package fopas;

import fopas.basics.FOElement;
import fopas.basics.FOElement.FOInteger;
import fopas.basics.FOFactory;
import fopas.basics.FOSet;

class FOFactoryImpl implements FOFactory
{
	@Override
	public <T extends FOElement> FOSet<T> createSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends FOElement> FOSet<T> createSet(Iterable<T> elements) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends FOInteger> FOSet<T> createNaturals() {
		// TODO Auto-generated method stub
		return null;
	}

}
